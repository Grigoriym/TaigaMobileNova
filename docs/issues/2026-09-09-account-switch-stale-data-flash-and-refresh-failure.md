# Account-switch stale data flash + refresh failure

**Status:** Done — all three mechanisms fixed and verified
**Link:** none (reported directly by gregory)   **Updated:** 2026-09-09

## Report

Reported symptom, in gregory's words: "If I login with account A, then log out, login
with account B I will see the stuff from account A for a moment, and wht i refresh it
won't load the data."

Two distinct observations:
1. Right after logging into account B, account A's data briefly shows before B's
   loads.
2. If the user manually refreshes during that window, the refresh doesn't load B's
   data either.

No repro steps, platform, or version given beyond that. No diagnosis offered — the
report is symptom-only, which is the good case (nothing to un-anchor from).

## Findings

All confirmed by direct code reading (file:line), not inferred, unless marked
otherwise.

**Logout** (`core/storage/.../auth/AuthStateManager.kt:26-32`) does, in order:
`filtersStorage.resetFilters()`, `taigaSessionStorage.clearData()` (DataStore —
wipes `current_project_id` back to its unset/`-1` state, `user_id`, theme, etc.),
`authStorage.clear()` (token/refresh token), `databaseWrapper.clearAllTables()`
(Room — deletes `project`/`sprint`/`workItem` rows on all three platform actuals;
this was previously broken on iOS only, fixed and closed in
`docs/archive/revisit-resolved.md` §37, 2026-08-10 — confirmed not a factor here).

**Login** (`feature/login/data/.../AuthRepositoryImpl.kt:26-43`) only writes the new
token (`authStorage.setAuthCredentials`) and `taigaSessionStorage.setUserId`. It does
not touch `current_project_id` — that stays at whatever logout left it at until the
user picks a project.

**`MainViewModel` is an app-lifetime singleton in practice**, not a per-session
object. `TaigaAppContent(...)` — which resolves `koinViewModel<MainViewModel>()` at
`composeApp/.../main/TaigaAppContent.kt:16` — is called exactly once per process,
from each platform's entry point (`MainActivity.kt:40`, `TaigaMobileDesktop.kt:77`,
`main.ios.kt:30`), above `MainNavHost`. It is never re-created across
logout→Login→ProjectSelector→Dashboard; only the nav-hosted children swap.

**`MainViewModel.currentProject`** (`composeApp/.../main/MainViewModel.kt:74-79`) is
a hot `StateFlow` built from `projectsRepository.getCurrentProjectFlow()` and
`stateIn`'d with `WhileSubscribed(5_000)` — so once collected it persists its last
value across the whole logout/login transition described above.

**The stale-flash mechanism**: `getCurrentProjectFlow()`
(`feature/projects/data/.../ProjectsRepositoryImpl.kt:83-90`):
```kotlin
override fun getCurrentProjectFlow(): Flow<ProjectSimple> = taigaSessionStorage.currentProjectIdFlow
    .flatMapLatest { projectId -> projectDao.getProjectByIdFlow(projectId) }
    .filterNotNull()
    .map { entity -> projectMapper.toProjectSimple(entity) }
```
`.filterNotNull()` means: whenever `currentProjectIdFlow` switches to an id with no
matching Room row — which is exactly what happens on logout (id resets, row deleted)
and again right after login while B's project hasn't been persisted yet — the flow
emits **nothing**, not `null`. Because `MainViewModel.currentProject` is a
long-lived `stateIn`'d `StateFlow` (previous point), its `.value` simply keeps
account A's last-known `ProjectSimple` through logout, the Login screen, and the
Project Selector screen, until account B's project row is actually inserted. Any UI
reading `MainViewModel.currentProject`/`drawerItems` during that window renders A's
data. This is the "see account A's stuff for a moment."

**The refresh-failure mechanism** — a genuine, code-confirmed race, not timing
speculation:

- `ProjectSelectorViewModel.selectProject()`
  (`feature/projectselector/ui/.../ProjectSelectorViewModel.kt:69-75`):
  ```kotlin
  private fun selectProject(project: Project) {
      viewModelScope.launch {
          taigaSessionStorage.setCurrentProjectId(projectId = project.id)
          projectsRepository.saveProject(project)
          session.resetFilters()
      }
  }
  ```
  is fire-and-forget — `viewModelScope.launch` is not awaited by the caller.
- The call site, `ProjectSelectorScreen.kt:113-116`:
  ```kotlin
  selectProject = {
      state.setProject(it)   // fires the launch{} above
      onProjectSelect()       // navigates immediately, synchronously
  }
  ```
  navigates to Dashboard (`onProjectSelect` → `navigator.navigateToDashboardAsTopDestination()`,
  wired in `composeApp/.../nav/MainNavHost.kt:165-166`) in the same tap handler,
  before the coroutine that persists `current_project_id` (DataStore) and the
  project row (Room) has necessarily completed.
- `DashboardViewModel.init { loadAll() }` (`feature/dashboard/ui/.../DashboardViewModel.kt:47-49`)
  runs immediately on Dashboard's composition. Each of its four load functions calls
  `taigaSessionStorage.getCurrentProjectId()` fresh (confirmed this reads DataStore
  live each call, not a cache). If that read lands before `setCurrentProjectId`'s
  write, it still returns `-1` (logout's cleared value) — every use case is called
  with a project id that belongs to no account, and the request fails. Each section
  has its own `.onFailure` → visible per-section error/retry state
  (`DashboardViewModel.kt` load functions).
- A second, separate manifestation of the same root cause:
  `DashboardScreen.kt`'s `LaunchedEffect(Unit) { viewModel.updateInternalProjectData() }`
  → `DashboardViewModel.kt:51-61` → `ProjectsRepositoryImpl.fetchAndSaveProjectInfo()`
  (`feature/projects/data/.../ProjectsRepositoryImpl.kt:64-70`):
  ```kotlin
  override suspend fun fetchAndSaveProjectInfo() {
      val response = projectsApi.getProjects(memberId = taigaSessionStorage.requireUserId())
          .find { it.id == taigaSessionStorage.getCurrentProjectId() } ?: error("Something is not right")
      ...
  }
  ```
  If `getCurrentProjectId()` still returns `-1` here, `.find` never matches and this
  throws `error("Something is not right")`. It's caught by
  `resultOf { }.onFailure { logcat(...) }` (`DashboardViewModel.kt:55-59`) and only
  logged — **never surfaced to the user**. Depending on exact timing this is a
  second, silent way "refresh right at that moment" produces no data, with no
  visible error at all (vs. the four section-level failures above, which do show a
  retry/error state).

Manual pull-to-refresh (`DashboardScreen.kt:109-118`, `retry = ::loadAll`) re-enters
the same `loadAll()` path — so refreshing during the race window hits the same
`-1`-project-id failure, matching "and if I refresh it won't load the data."

**Prior related fix**: `docs/archive/revisit-resolved.md` §37 covered account-A data
surviving into account B's session, but scoped only to iOS's Room-clear being a
no-op — that's fixed on all platforms and is not this bug. This investigation's two
mechanisms (the `filterNotNull()` stale `StateFlow` and the unawaited
`selectProject()` race) are new findings, not covered by that prior fix.

**Team awareness of the general race class**: `SettingsViewModel.kt:47-49` already
carries a comment/`cancel()` guarding a *different* call site
(`getCurrentProjectSimple()` on Settings init) against racing
`authStateManager.logoutSuspend()`'s table clear — confirms this general shape of
bug (an in-flight project read racing a project-id/table reset) is already known to
recur in this codebase, just not previously found in the login→ProjectSelector→Dashboard
path.

## Root cause

Two independent, compounding gaps:

1. `ProjectsRepositoryImpl.getCurrentProjectFlow()`'s `.filterNotNull()`
   (`ProjectsRepositoryImpl.kt:87`) suppresses the "no project for this id" state
   instead of propagating it as `null`, and `MainViewModel.currentProject`
   (`MainViewModel.kt:74-79`) is a `stateIn`'d `StateFlow` on a de-facto app-lifetime
   ViewModel (`TaigaAppContent.kt:16`, created once per process). The combination
   means account A's project-derived UI state is never actively cleared on logout —
   it just holds its last value until account B's project row is written to Room.
2. `ProjectSelectorViewModel.selectProject()`'s persistence coroutine
   (`ProjectSelectorViewModel.kt:69-75`) is fire-and-forget, and
   `ProjectSelectorScreen.kt:113-116` navigates to Dashboard synchronously in the
   same tap handler with no wait for that coroutine. If Dashboard's `init` load or a
   user-triggered refresh runs before `setCurrentProjectId`'s DataStore write lands,
   every fetch reads the stale `current_project_id = -1` and fails — either visibly
   (per-section Dashboard errors) or silently (`fetchAndSaveProjectInfo()`'s
   swallowed `error("Something is not right")`).

## Impact

Every account switch on a device that's already logged into a different account hits
mechanism 1 (guaranteed — logout always clears the project id/row, login never
restores it before Dashboard first composes). Mechanism 2 is timing-dependent: it
only manifests if Dashboard's `init` load or a manual refresh executes inside the
narrow window before the `viewModelScope.launch` in `selectProject()` completes
(two DataStore/Room writes) — likely on slower devices/storage, or if the user is
fast enough to swipe-refresh immediately after tapping a project. No workaround
found in the traced code other than waiting (killing/reopening the app forces a
fresh read once the writes have landed, or simply waiting a moment before
refreshing).

## Open questions

- Not reproduced live (this was a read-only, code-only investigation — no
  emulator/device run). The exact timing window and which of the two Dashboard
  failure shapes (visible per-section errors vs. the silent `fetchAndSaveProjectInfo`
  swallow) the user actually hit is inferred from code, not observed.
- iOS/desktop entry points were confirmed to call `TaigaAppContent(...)` once each
  (same as Android), but their surrounding lifecycle code wasn't read in detail
  beyond that confirmation — no reason to expect divergence since this is all
  shared `commonMain` code, but not independently verified per platform.

## Options

**A. Fix both root causes (recommended).**
- For (1): stop swallowing the "no current project" state. Either drop
  `.filterNotNull()` and make `getCurrentProjectFlow()` return
  `Flow<ProjectSimple?>` (propagating the type change through `MainViewModel` and
  its consumers), or explicitly reset `MainViewModel`'s cached project state on
  `AuthStateManager`'s logout event (`MainViewModel` could collect
  `_logoutEvents` and null out `currentProject` immediately instead of waiting on
  the DB-backed flow). The DB-backed-flow fix is more correct (single source of
  truth, no dual bookkeeping) but touches every consumer of `getCurrentProjectFlow()`.
- For (2): make `ProjectSelectorScreen`'s tap handler await the persistence
  coroutine before navigating (`suspend fun selectProject` + `rememberCoroutineScope`
  or drive navigation from a one-off event `Channel` emitted after the `launch{}`
  body completes, matching this repo's existing One-off Events pattern from
  CLAUDE.md) rather than firing-and-forgetting it.
- Pros: closes both the visible flash and the refresh failure at their source; each
  fix is small and independently testable.
- Cons: the `Flow<ProjectSimple?>` type change ripples through call sites (blast
  radius needs a grep for `getCurrentProjectFlow`/`MainViewModel.currentProject`
  consumers); awaiting navigation changes UX slightly (a beat of latency between tap
  and screen transition, likely imperceptible given these are local DataStore/Room
  writes).

**B. Fix only the refresh failure (2), leave the flash (1) alone.**
- Pros: smaller, avoids the `Flow` signature change.
- Cons: doesn't address the actual reported symptom #1 (the stale flash), which
  gregory explicitly called out as the first half of the bug — half-fixes the report.

**C. Won't fix / defer.**
- Not recommended: this is a real, always-reproducible (mechanism 1) data-leak-style
  UX bug — account A's project name/data briefly appearing under account B's login is
  the kind of thing that's actively confusing to a user, not just cosmetic.

**Recommendation: Option A**, both parts. They're independently testable (see next
section once approved) and mechanism 1 is guaranteed to reproduce on every account
switch, so it's worth fixing regardless of how rare mechanism 2's timing window is
in practice.

## Decision

gregory approved Option A (2026-09-09): fix both mechanisms.

## What landed

**Mechanism 1 (stale flash).** Changed `ProjectsRepository.getCurrentProjectFlow()`'s
signature from `Flow<ProjectSimple>` to `Flow<ProjectSimple?>` and dropped
`.filterNotNull()` in `ProjectsRepositoryImpl` (`feature/projects/data/.../ProjectsRepositoryImpl.kt:83-89`)
so the flow now actively emits `null` — instead of nothing — when the current
project id has no matching Room row. `MainViewModel.drawerItems`
(`composeApp/.../main/MainViewModel.kt`) now maps `currentProject` to an empty list
on `null` instead of `.filterNotNull()`-ing past it, so the drawer resets the moment
logout clears the project id rather than holding the previous account's items.

**Mechanism 2 (refresh failure).** `ProjectSelectorViewModel.selectProject()`
(`feature/projectselector/ui/.../ProjectSelectorViewModel.kt`) now sends a
`_projectSelected` one-off event (rendezvous `Channel`, this repo's standard
One-off Events pattern) after the `setCurrentProjectId`/`saveProject`/`resetFilters`
writes complete, instead of the screen navigating synchronously on tap.
`ProjectSelectorScreen.kt` now calls `onProjectSelect()` from
`ObserveAsEvents(viewModel.projectSelected)` rather than inline in the `selectProject`
lambda, so Dashboard's `init { loadAll() }` can no longer run before the new
project id is actually persisted.

**Tests added/updated:**
- `ProjectsRepositoryImplTest.kt`: replaced the now-inaccurate "skips null entities"
  test with one asserting `null` then the mapped project as the dao resolves the row,
  and added a case for "no matching row for the current id → emits null".
- `MainViewModelTest.kt`: new `drawerItems - resets to empty when current project
  becomes null` test (uses a directly-controlled `MutableStateFlow` rather than a
  two-value `flowOf`, since a two-value cold flow raced across the two nested
  `stateIn`/`WhileSubscribed` layers and got conflated — confirmed by a real test
  failure before switching to the explicit-set approach).
- `ProjectSelectorViewModelTest.kt`: new `setProject emits projectSelected after
  persisting the choice` test, using the same rendezvous-channel-inside-`turbine`
  pattern already established in `ProjectDetailsViewModelTest.kt`.
- `FakeProjectDao.kt`'s doc comment and `FakeProjectsRepository.kt`'s `projectFlow`
  type updated to match the new nullable signature.

**Verification:**
- `./gradlew jvmTest` (full suite, all modules): green.
- `./gradlew ktlintCheck`: green.
- `./gradlew koverXmlReport :koverVerify`: floor (line ≥ 92%, branch ≥ 78%) held.
- GUI walkthrough on `Medium_Phone_API_36.1` (fdroid debug build, local Taiga
  instance): logged out of the previously-logged-in account, logged in as `user1`,
  selected "Main project (main-2)", landed on Dashboard with real data loaded (no
  error/empty state) and a correctly-populated drawer. This confirms the normal
  (non-race) path still works end-to-end; the race window itself is
  milliseconds-scale and not independently reproducible by hand — the fix's
  correctness under the race rests on the code fix (navigation now waits on the
  persistence coroutine) plus the unit tests above, not a live capture of the race.

**Left out:** no changes to `fetchAndSaveProjectInfo()`'s swallowed
`error("Something is not right")` — that silent-failure path only fires if the race
window is still hit despite the ordering fix (e.g. some other future caller
introduces the same unawaited-navigation shape), and swallowing it more loudly was
out of scope for this fix. Noted here rather than in `docs/revisit.md` since it's a
residual of the same investigation, not an unrelated finding.

**Verification gap, found the hard way:** the GUI walkthrough above force-stopped and
relaunched the app (`am force-stop` + `am start`) before logging in as `user1`. That
gives a brand-new process, and — per mechanism 3 below — a brand-new process is
exactly the one case that does *not* exhibit the bug gregory reported (every
top-level ViewModel is created fresh regardless). The walkthrough proved the normal
path still works; it did not, and could not, prove the actual reported symptom fixed,
because it never exercised two logins inside one continuous process. Flagging this
plainly rather than quietly redoing the check, since it means the earlier "Done"
status was reported on the strength of a check that couldn't have caught this.

## Mechanism 3 (reopened 2026-09-09): top-level ViewModels are never disposed within a process

**Report, corrected:** gregory: "For sure not solved, for example Dashboard still
shows the old data, only after i refresh it, it will update the data." This is
inside a single continuous app run (no process restart) — log out, log back in as a
different account, land on Dashboard: it shows the *previous* account's Watching/My
Work/Recent Activity/Completed data until a manual pull-to-refresh.

**Root cause, traced through the actual library sources** (Nav3 is real
`androidx.navigation3` + `lifecycle-viewmodel-navigation3` here — this repo's
`Navigator`/`NavigationState` in `grappim-kit-navigation` only drives the backstack
data feeding it, per CLAUDE.md's Navigation Pattern section):

- `MainAppState.kt:33-46`'s `TOP_LEVEL_KEYS` — every drawer section
  (`DashboardNavDestination`, `EpicsNavDestination`, `IssuesNavDestination`,
  `KanbanNavDestination`, `TeamNavDestination`, `WikiPagesNavDestination`,
  `WikiLinksNavDestination`, `SettingsNavDestination`, and all three Scrum
  destinations) is a `data object` — a Kotlin singleton, exactly one instance for
  the life of the process. Only `ProjectSelectorNavDestination(isFromLogin: Boolean)`
  carries a payload.
- `Navigator.resetTo(key)` (`grappim-kit/navigation/.../Navigator.kt:66-77`):
  ```kotlin
  fun resetTo(key: NavKey) {
      state.subStacks.forEach { (keyClass, stack) ->
          if (stack.size > 1) stack.subList(1, stack.size).clear()
          if (keyClass == key::class) {
              stack[0] = key
          }
      }
      state.topLevelStack.apply { clear(); add(key) }
  }
  ```
  Only the *one* sub-stack whose class matches `key` gets its root entry
  (`stack[0]`) reassigned. `navigateToLoginAsTopDestination()` (logout) calls
  `resetTo(LoginNavDestination)` — Dashboard's own sub-stack root is untouched
  (`keyClass == Dashboard::class` is false for that iteration; only trimmed if it had
  grown past size 1). `navigateToDashboardAsTopDestination()` (landing on Dashboard
  after project selection) calls `resetTo(DashboardNavDestination)` — *this* time
  `stack[0] = DashboardNavDestination` runs, but since it's a singleton, the value
  being written is `===` identical to what was already there. Structurally, nothing
  changed either time.
- `NavigationState.toEntries()` (`NavigationState.kt:76-92`) calls
  `rememberDecoratedNavEntries(backStack = stack, ...)` for **every** sub-stack, every
  composition, regardless of whether that section is currently active in
  `topLevelStack` (its own doc comment: "Every sub-stack is decorated on every
  composition — a decorator dropped and recreated loses its state"). So Dashboard's
  own `rememberDecoratedNavEntries` call keeps running continuously even while Login
  is on screen.
- Read directly from the cached sources jars (`androidx.navigation3:navigation3-runtime:1.1.1`,
  `androidx.lifecycle:lifecycle-viewmodel-navigation3:2.11.0`) — not assumed:
  - `rememberDecoratedNavEntries`'s first overload (`DecoratedNavEntries.kt`) does
    `val entries = remember(backStack.toList()) { backStack.fastMapOrMap { ... } }`.
    `remember` compares its key via `equals()`; `List.equals()` is element-wise, so
    `[DashboardNavDestination].equals([DashboardNavDestination])` is `true` (same
    singleton) every single time this recomposes. `entries` is never recomputed after
    the first composition.
  - `PrepareBackStack` (`DecoratedNavEntries.kt`) tracks disposal via
    `DisposableEffect(contentKey, entries.toList())` per entry; its `onDispose` is what
    calls `decorator.onPop(contentKey)` when `contentKey` is no longer in the latest
    backstack. Since `entries` is never recomputed (previous point), this effect never
    re-keys, so `onDispose`/`onPop` is **never invoked for `DashboardNavDestination`'s
    contentKey, for the entire lifetime of the process.**
  - `ViewModelStoreNavEntryDecorator` (`ViewModelStoreNavEntryDecorator.kt:113-125`)
    only clears its `ViewModelStore` for a key `onPop`: `onPop = { key ->
    viewModelStoreProvider.clearKey(key) }`. Never called ⇒ never cleared.
- `DashboardScreen.kt:75`'s `viewModel: DashboardViewModel = koinViewModel()` resolves
  via `LocalViewModelStoreOwner.current`, which `ViewModelStoreNavEntryDecorator`
  provides as `rememberViewModelStoreOwner(entry.contentKey, viewModelStoreProvider, ...)`
  — looked up by that same never-cleared `contentKey`. Koin's `koinViewModel()` then
  finds the **existing** `DashboardViewModel` already registered in that
  `ViewModelStore` and returns it, rather than constructing a new one via the Koin
  factory. `DashboardViewModel.kt:47-49`'s `init { loadAll() }` therefore never runs
  again — the screen just re-subscribes to the same, already-populated (stale)
  `MutableStateFlow` from whenever the instance was first created. Confirmed
  `DashboardViewModel` is `@KoinViewModel` (`DashboardViewModel.kt:22`), not `@Single`
  — ruled out as the simpler explanation.
- Manual pull-to-refresh (`DashboardScreen.kt`, `retry = ::loadAll`) calls `loadAll()`
  directly on that same surviving instance, which is why it "fixes" the display —
  matching gregory's exact description.

**Scope: this is not Dashboard-specific.** Every `TOP_LEVEL_KEYS` entry is a
singleton `data object` reached via `resetTo`/`goToTopLevel`
(`Navigator.kt:96-105`'s `goToTopLevel` has the identical `topLevelStack[idx] = key`
shape), so Epics, Issues, Kanban, Team, both Wiki roots, Settings, and all three
Scrum destinations are equally never disposed. It doesn't surface as visibly on those
screens today only because within one account, "doesn't auto-refresh a tab you
already visited" reads as normal/acceptable UX — switching *accounts* is what makes
stale data glaringly obviously wrong, since the two accounts' data differs a lot.

**Relationship to the two fixes already shipped this session:** those are still
correct and still needed — `MainViewModel` is a *literal* singleton (constructed once
in `TaigaAppContent`, above the whole nav tree, never subject to Nav3's
entry-disposal machinery at all), so its `filterNotNull()` fix stands on its own. The
`ProjectSelectorViewModel` navigation-race fix also stands — it closes a real,
separate race. Neither touches per-screen ViewModel lifecycle, which is what this
mechanism is about.

## Options (mechanism 3)

**D. Force the whole nav tree to be torn down and rebuilt on logout (recommended).**
Wrap the call site of `rememberMainAppState()` (`MainScreen.kt` — currently `val
appState = rememberMainAppState()`) in `key(sessionGeneration) { ... }`, where
`sessionGeneration` is an `Int`/similar bumped once per `AuthStateManager` logout
event. A `key()` change forces Compose to fully dispose the previous composition
subtree — including every `remember`'d `ViewModelStoreProvider` inside every
section's `rememberViewModelStoreNavEntryDecorator()` — and rebuild
`NavigationState`/`Navigator`/`NavDisplay` from scratch. This is a Compose-level
teardown, not a Nav3 pop-detection dependency, so it isn't defeated by singleton
keys. Matches `resetTo()`'s own doc comment ("wipes every section's history... for
'forget everything, start fresh'") — that promise just isn't actually kept for
ViewModels today.
- Pros: one change, in this repo only (no `grappim-kit-navigation` edit, so no
  version bump / other-consumer blast radius); fixes every top-level screen at once,
  not just Dashboard; matches the code's own stated intent.
- Cons: needs care around exactly what state should *not* be wiped (e.g. `MainViewModel`
  lives above this boundary already, so it's unaffected either way; the
  `NavSavedStateConfiguration`/process-death restoration path needs checking so a
  generation bump doesn't fight it); a full subtree teardown likely costs a visible
  frame/flicker on the Login screen right after logout — needs a quick GUI check to
  confirm.

**E. Fix it in `grappim-kit-navigation` itself** — e.g. make `resetTo`/`goToTopLevel`
force real pop detection (a per-section "epoch" wrapping each `NavKey`, or reordering
so Nav3 sees a genuine remove-then-add). Touches the shared library other apps
(wallosmobile, wayprint, HateItOrRateIt) consume — CLAUDE.md's own guidance says
these swaps need care and gregory's own go-ahead per swap. Not recommended as the
*first* fix: higher blast radius, and it's not yet established whether those other
apps rely on today's (arguably buggy) persistence behavior anywhere.
- Pros: fixes the root mechanism at its actual source; other grappim-kit consumers
  benefit too, if they have the same latent bug.
- Cons: much bigger blast radius; needs its own investigation into whether other
  consuming apps depend on current behavior; a version bump + re-consume cycle.

**F. Patch each top-level ViewModel individually** — have `DashboardViewModel` (and
every other `TOP_LEVEL_KEYS` screen's ViewModel) collect `AuthStateManager.logoutEvents`
(same pattern `MainViewModel` already uses) and explicitly reset/reload its own
state.
- Pros: no navigation-library change at all.
- Cons: repetitive — needs the same boilerplate added to ~10 ViewModels today, and to
  every new top-level screen going forward; easy to add an 11th top-level screen later
  and forget it; treats the symptom at every call site instead of the actual shared
  cause.

**Recommendation: Option D.** Smallest, most local fix that actually matches what
`resetTo()` already claims to do, fixes every top-level screen at once instead of
just Dashboard, and doesn't touch the shared library. Option E is worth raising with
gregory as a separate, deliberate follow-up for `grappim-kit` itself (other consuming
apps may have the same bug silently) — not blocking this fix.

## Decision

gregory approved Option D (2026-09-09): "ok, let's try".

## What landed (mechanism 3)

`composeApp/.../main/MainScreen.kt`'s `MainScreenContent`:
- Added `var sessionGeneration by rememberSaveable { mutableIntStateOf(0) }` (outside
  the keyed subtree, `rememberSaveable` so it survives process death consistently with
  whatever composite key its backstack was saved under).
- The existing `LaunchedEffect(Unit) { viewModel.logoutEvent.onEach { ... } }` (also
  outside the keyed subtree, so it keeps listening across generations) now does
  `sessionGeneration++` instead of calling `appState.navigator.navigateToLoginAsTopDestination()`.
- Wrapped everything from `val appState = rememberMainAppState()` through the end of
  the function body (the Scaffold/drawer/`MainNavHost` tree) in `key(sessionGeneration) { ... }`.
  A `key()` value change fully disposes the previous composition subtree — including
  every `remember`'d `ViewModelStoreProvider` inside each section's
  `rememberViewModelStoreNavEntryDecorator()` and the `rememberSerializable`-backed
  back stacks — and rebuilds it from scratch, so this is a Compose-level teardown that
  doesn't depend on Nav3's per-entry pop detection (which never fires for a singleton
  `data object` key, per mechanism 3's root cause above).
- Removed `Navigator.navigateToLoginAsTopDestination()`
  (`feature/login/ui/.../LoginNavDestination.kt`) — its only call site is gone, and a
  fresh `rememberMainAppState()` already seeds `startKey = LoginNavDestination`, so
  the explicit `resetTo()` call is redundant once the whole tree is rebuilt.

**Verification:**
- `./gradlew :androidApp:assembleFdroidDebug`: compiles clean.
- `./gradlew jvmTest`: full suite green (no test changes needed for this mechanism —
  it's pure Compose composition lifecycle, not something the existing ViewModel-level
  test suite exercises).
- `./gradlew ktlintCheck`, `koverXmlReport`/`:koverVerify`: green, floor held.
- **GUI verification, this time in a single continuous process** (the earlier
  mechanism 1/2 verification's mistake — force-stopping between logins — was
  deliberately not repeated): installed the build, logged in as `user1` (persisted
  from a prior session), confirmed via Settings → User. Logged out (Settings → Log
  out → Yes), landed cleanly on Login with no force-stop. Logged in as `user2`,
  selected "Main project (main-2)", landed on Dashboard automatically showing **MY
  WORK (1)** — genuinely different from `user1`'s **MY WORK (2)** seen earlier in the
  same investigation — with no manual refresh. `adb logcat` confirmed a fresh network
  fetch fired automatically: `GET .../issues?project=5&assigned_to=7&...` where `7` is
  `user2`'s own user id (`user2_extra_info` confirms `id: 7` in a sibling response
  body), not a cached/stale response. This is direct evidence `DashboardViewModel` was
  recreated (its `init { loadAll() }` ran again) rather than reused.

**Scope note:** the fix is general (`key(sessionGeneration)` wraps the whole nav tree,
not just Dashboard's slot), so it should equally fix the same latent staleness on
every other `TOP_LEVEL_KEYS` screen (Epics, Issues, Kanban, Team, Wiki roots,
Settings, Scrum roots) described in the root-cause section — only Dashboard was
independently GUI-verified this session, since it's what the report named and what's
reachable immediately after login.

**Left for a separate, deliberate follow-up (not this fix):** Option E — whether
`grappim-kit-navigation`'s `resetTo()`/`goToTopLevel()` should be fixed at the source
so every consuming app (wallosmobile, wayprint, HateItOrRateIt) gets this for free,
instead of each app needing its own `key(sessionGeneration)` workaround. Flagged to
gregory in the options above; not actioned here per CLAUDE.md's grappim-kit swap
guidance (needs its own investigation into whether other consumers rely on today's
behavior, and its own go-ahead).

**Follow-up closed 2026-09-09:** Option E landed upstream — `grappim-kit-navigation` 0.1.3 fixes
`resetTo()` itself via a `resetGeneration` counter (`goToTopLevel()` confirmed never part of the
bug). This app's `key(sessionGeneration)` workaround was removed the same day once the app bumped
onto 0.1.3; `MainScreen.kt`'s logout handler now calls `navigator.resetTo(LoginNavDestination)`
directly. See CLAUDE.md's Navigation Pattern section for the current mechanism.
