# ViewModel Lifecycle & Error-Handling Hardening — Implementation Plan

## Background

Gregory forwarded 8 articles from the "Android Lead" newsletter (Mykhailo Vasylenko), covering VM
`init`, process-death state restoration, testability, State design, startup-task DI, debug-code
isolation, and silent coroutine exceptions. A 9th, *Why your MVI can't handle two Intents at once*
(2026-07-05), was added later. Every post ends
with the same pitch for a paid "Production-Ready MVI" course — the technical claims were checked
against this codebase rather than taken at face value. Most don't map cleanly onto this project's
architecture (KMP + Koin + hand-rolled MVVM per CLAUDE.md, not single-platform Android + Hilt +
MVI), but three produced confirmed, concrete findings worth fixing.

## Findings

### 1. Silent exceptions on `ApplicationScope` (actionable — checklist step 1)

Source: *"No crashes" doesn't mean your Android app is healthy* (2026-06-14).

`core/async-kmp/src/commonMain/kotlin/com/grappim/taigamobile/core/asynckmp/KmpCorotuinesModule.kt:46-47`:

```kotlin
@[Single ApplicationScope]
fun provideApplicationScope(@DefaultDispatcher defaultDispatcher: CoroutineDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + defaultDispatcher)
```

No `CoroutineExceptionHandler` in the context. Two production call sites launch on this scope with
nothing catching failures:

- `AuthStateManager.logout()` (`core/storage/src/commonMain/kotlin/com/grappim/taigamobile/core/storage/auth/AuthStateManager.kt:34-38`)
  — if `clearAllTables()` or any storage-clear step throws, the user believes they logged out but
  data wasn't cleared and `_logoutEvents` never fires. No log, no crash.
- `TaigaApp.onCreate()` (`androidApp/src/main/kotlin/com/grappim/taigamobile/TaigaApp.kt:55-61`) —
  `cacheManager.cleanExpiredCache()` and the crash-reporting-toggle flow
  (`taigaSessionStorage.crashReportingEnabled.onEach { ... }.launchIn(applicationScope)`) both run
  unguarded.

This is a direct violation of CLAUDE.md's Error Handling rule ("never swallow exceptions
silently"). The fix is small and self-contained: add a handler to the scope provider — no call-site
changes needed, since `SupervisorJob` already isolates failures from each other; the handler just
needs to make them visible via `logcat`.

### 2. Process-death UI-state restoration (gated — checklist step 2)

Source: *How to avoid losing UI State after process death* (2026-06-21), reinforced by *Why your
ViewModel is untestable* (2026-06-28), which builds its `initialState`-via-constructor-injection fix
on the same `UiStateMachine`/`SavedStateHandle` wrapper.

Grepped: `SavedStateHandle` appears nowhere in production code
(`grep -rl "SavedStateHandle" --include="*.kt" feature core composeApp` returns one hit — a test
file's own filename, not a real usage). Every VM's `MutableStateFlow` resets to its hardcoded
default on process death; nothing restores it.

This is real, but scope matters before doing anything:

- Not every screen needs it — a list/detail screen that just re-fetches from the server on
  restoration loses nothing a user would notice. The article's own examples (onboarding, checkout,
  multi-step forms) are screens where a user has *entered* data a re-fetch can't recover.
- The proposed mechanism (`Parcelable` state class + a `UiStateMachine` wrapper around
  `SavedStateHandle`, exposing `isStateRestored` so `init` can skip a reload) is a new shared
  abstraction this project doesn't have. Introducing it project-wide up front vs. adding it
  ad hoc to specific screens as they're touched is a real design choice, not a one-line fix.
- Route params are already passed via Koin `@InjectedParam` (see CLAUDE.md's Navigation Pattern),
  not `SavedStateHandle.toRoute()` — a `UiStateMachine` wrapper would need its own
  `SavedStateHandle` injection path alongside that, which is a KMP-compatibility question worth
  checking early (does `SavedStateHandle` exist as a meaningful concept outside Android?) before
  committing to the pattern as described in the article.

**Gated on gregory** picking: (a) which screens/flows actually warrant this, (b) whether to build
the shared wrapper now for future use or add it per screen as needed, (c) whether it's even
meaningful on iOS/Desktop targets or Android-only.

**Resolved 2026-08-29:** (a) forms/entry-flow screens only, not list/detail; (b) build a shared
reusable helper up front, not ad hoc per screen — `RestorableState` (`utils/ui`), a small
`restore(key, default)` / `save(key, value)` wrapper around `SavedStateHandle`, kept deliberately
thin rather than a `UiStateMachine`-style base class, since it composes with the existing
`FeatureState` data-class + `_state.update { }` convention instead of replacing it; (c) not
resolved — see the blocking finding below.

**Technical groundwork done (pilot: `CreateTaskViewModel`):**
- `SavedStateHandle` is KMP-ready via the already-pinned `jetbrainsAndroidxLifecycle` (2.11.0)
  train — `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-savedstate` (new catalog entry
  `jetbrains-lifecycle-viewmodel-savedstate`, added to `KmpCompose.kt` alongside the other two
  lifecycle/savedstate deps it already applies project-wide). Real per-target implementation ships
  under the plain `androidx.lifecycle` group coordinates with per-target classifiers (`-desktop`,
  `-iosarm64`, …) — the `org.jetbrains.androidx.lifecycle` artifact is a thin Gradle Module
  Metadata redirect to those, the same pattern already seen for `androidx.navigation3`.
- Koin's `koinViewModel()` (`org.koin.compose.viewmodel`, via `koin-core-viewmodel`'s
  `AndroidParametersHolder`) auto-resolves a plain `SavedStateHandle` constructor parameter on any
  `@KoinViewModel` — no `single { }` registration, no `@InjectedParam` needed. Confirmed by reading
  `AndroidParametersHolder.elementAt`/`getOrNull`, which special-case the `SavedStateHandle` type
  and call `extras.createSavedStateHandle()`. This requires the resolved `CreationExtras` to carry
  a `SavedStateRegistryOwner`.
- This project's Nav3 decorator wiring (`NavigationState.kt`'s `toEntries()`) already satisfies
  that: `rememberSaveableStateHolderNavEntryDecorator()` runs before
  `rememberViewModelStoreNavEntryDecorator<NavKey>()`, which is exactly the order
  `ViewModelStoreNavEntryDecorator`'s own doc comment requires ("This requires the usage of
  SaveableStateHolderNavEntryDecorator to ensure that the NavEntry scoped ViewModels can properly
  provide access to SavedStateHandles").
- `CreateTaskViewModel` now takes a `savedStateHandle: SavedStateHandle` param, wraps it in
  `RestorableState`, and restores/persists `title`/`description` through it. Compiles clean;
  `RestorableStateTest` (`utils/ui`) and new `CreateTaskViewModelTest` cases (restore-from-prior-handle,
  save-persists-to-handle) pass; full `jvmTest` and `ktlintCheck` are green; `KoinGraphTest` shows
  no new `NoDefinitionFoundException` (though that test can't fully prove the constructor-param
  resolution path — its `route`-first `DefinitionParameterException` short-circuits before
  `savedStateHandle` would ever be evaluated, since Kotlin evaluates constructor args left to right).

**Blocking finding (2026-08-29, emulator-verified — see `docs/EMULATOR_TESTING.md`):** the Nav3
back stack itself does not survive a real process kill in this app. Reproduced: logged in,
navigated to the Create Task screen, typed distinct title/description, backgrounded
(`KEYCODE_HOME`), killed the process (`am kill`, confirmed dead via `ps`), relaunched onto the same
task (`am start -n`, confirmed a genuine restore via `dumpsys activity activities`'s `sz=1`, not a
fresh activity). The app landed on **Dashboard** — its post-login start destination — not back on
Create Task, despite the login session itself surviving. **This means the
`CreateTaskViewModel`/`RestorableState` wiring above is currently inert in practice**: a user killed
mid-form never gets back to the form to see the restored fields at all. The per-ViewModel
`SavedStateHandle` piece was necessary but is not sufficient — the back stack has to survive the
same process death first, or this delivers no observable benefit regardless of how many screens it's
added to.

Root cause not yet investigated. Candidates: `MainActivity`'s `setContent` call not receiving/using
`savedInstanceState`, or `rememberNavigationState`'s `SavedStateConfiguration` param not actually
being wired to real Activity-level bundle persistence (it may only be surviving
recomposition/configuration change via `rememberSaveable`, not a true process kill). Needs its own
investigation before this checklist step can be called done — (c) above (iOS/Desktop meaningfulness)
is moot until this is fixed, since Android is the only platform where "process death" is even a
distinct event to test against.

**Decided 2026-08-29:** option 1 — investigate and fix the back-stack restoration gap first, as its
own task, in a new session, before anything else on this initiative. The `CreateTaskViewModel`/
`RestorableState` change already built lands now as correct, tested infrastructure with the caveat
above documented; it becomes real end-to-end value once the back-stack fix lands, with no further
VM-level work needed for that one screen. Rolling the same pattern out to other form screens stays
parked until the back-stack gap is fixed — no further screens should pick this up in the meantime.

**Root cause found and fixed 2026-08-29 (step done).** Neither of the two candidates named above was
it — Nav3's `rememberNavBackStack(configuration, ...)` restoration was never broken. The real cause:
`MainNavHost`'s top-level `LaunchedEffect(initialNavState.isReady)` unconditionally called
`navigator.navigateToDashboardAsTopDestination()` (a `resetTo(DashboardNavDestination)`, which wipes
every nav section per `Navigator`'s own doc comment) on **every** cold app start where the user is
logged in with a project selected — including a process-death relaunch, even when Nav3 had already
restored a deeper back stack. This fired on every real cold start (first-ever launch and
process-death relaunch look identical to this code), stomping the correctly-restored `CreateTask`
entry down to bare `Dashboard` every time.

Fix (`composeApp/.../main/MainNavHost.kt`): gate that reset on `navigationState.currentKey ==
LoginNavDestination` (the seed value for `topLevelStack`/each sub-stack when nothing has diverged
from it yet). A truly fresh app start (or first-ever login) is still sitting on the seeded `Login`
entry when this effect runs, so the reset still fires and the skip-login-screen behavior is
unchanged; a process-death relaunch that already restored a deeper entry has a different
`currentKey`, so the reset is skipped and the restored stack survives.

**Second bug found while verifying the fix on the emulator (fixed in the same change):** with the
reset skipped, the app relaunched onto a **blank screen forever** — the restored `CreateTask` screen
was actually rendering correctly underneath (confirmed via `uiautomator dump`, which found the
restored title/description text in the view tree), but `installSplashScreen()`'s
`keepOnScreenCondition` never released because `ScreenReadySignalController.signalReady()` is only
called from the `Login`/`ProjectSelector`/`Dashboard` `entry<>` blocks in `MainNavHost.kt` — see its
doc comment, which explicitly assumes the app always lands on one of those three. A restored deeper
screen never composes any of those three entries, so the signal never fires. Fixed by calling
`screenReadySignal.signalReady()` directly in the top-level `LaunchedEffect`'s `else` branch (the
"already on the correct restored screen, no Login-flash risk to wait out" case) — `LocalScreenReadySignal.current`
is read once at the top of `MainNavHost` for this.

**Verified on the `Medium_Phone_API_36.1` AVD** (fdroid debug build): logged in with a project
selected, navigated Dashboard → Backlog → new user story (Create Task) form, typed a distinct
title/description, backgrounded (`KEYCODE_HOME`), killed the process for real (`am kill`, confirmed
dead via `ps`), relaunched (`am start -n`, `sz=1` confirming a genuine task resume not a fresh
activity). App landed directly back on the Create Task form with both fields intact and no stuck
splash. Also re-verified the untouched fresh-start path (force-stop, relaunch with no deeper
navigation) still lands cleanly on Dashboard with the splash dismissing normally. `jvmTest` and
`ktlintCheck` both green.

No `RestorableState` rollout to further screens was done as part of this step — that was already
out of scope (see "Resolved 2026-08-29" above, parked pending this fix). It can now be picked up as
its own task if wanted, since the back-stack restoration this depends on is confirmed working
end-to-end.

### 3. Concurrent independent loads in `init` (declined — no action)

Source: *How to load ViewModel's data without using 'init'* (2026-07-19), specifically its Issue 1
(flaky Turbine tests from nondeterministic coroutine ordering when `init` fires more than one
concurrent load).

Searched for ViewModels whose `init` block calls 2+ independently-launching functions:

```
feature/epics/.../EpicsViewModel.kt              -> loadFiltersData(), getPermissions()
feature/issues/.../IssuesViewModel.kt            -> loadFiltersData(), getPermissions()
feature/kanban/.../KanbanViewModel.kt             -> getKanbanData(), loadFiltersData()
feature/scrum/.../ScrumBacklogViewModel.kt        -> loadFiltersData(), getPermissions()
feature/settings/.../ProjectValuesViewModel.kt    -> loadItems(), loadPresetColors()
feature/settings/.../TagsScreenViewModel.kt       -> fetchTagsColors(), initDialogTags()
feature/workitem/.../WorkItemEditTagsViewModel.kt -> fetchTags(), initDialogTags()
feature/workitem/.../EditSprintViewModel.kt       -> getPermissions(), getSprints()
```

Each fires two independent `viewModelScope.launch` blocks from `init`, updating disjoint state
fields — exactly the shape the article warns about (confirmed by reading `EpicsViewModel.kt` in
full: `loadFiltersData()` and `getPermissions()` are each their own `launch`, no ordering between
them).

Checked `EpicsViewModelTest.kt` as the representative case — tests assert `sut.state.value` (a
snapshot taken after `createViewModel()` returns), not an ordered sequence of Turbine-collected
emissions. Combined with `MainDispatcherRule`'s unconfined test dispatcher (both `init`-launched
coroutines run to completion before `createViewModel()` returns, since the fakes they call resolve
synchronously), the nondeterministic-ordering failure mode the article describes doesn't arise
here. The one Turbine usage in that test file (`snackBarMessage.test { awaitItem() }`) asserts a
single one-off event, not multiple ordered state emissions.

That covers why the *tests* don't flake — a separate question is whether the concurrency itself is
still a production correctness risk even though the tests can't see one. It isn't, for two
structural reasons, both confirmed against `EpicsViewModel.kt`:

- **The two launches write disjoint state fields.** `loadFiltersData()` only touches
  `isFiltersLoading`/`filtersError`/`filters`; `getPermissions()` only touches `canAddEpic`. Each
  write goes through `StateFlow.update {}`, which is atomic. With no shared field between them,
  whichever coroutine finishes first, the final state converges to the same value regardless of
  interleaving — there is nothing for "order" to corrupt. The other 7 VMs in the list above were
  found by this same shape (`init` firing two independent `launch` blocks onto separate fields), so
  this generalizes by construction, though only Epics was opened to confirm it directly.
- **`viewModelScope` is a `SupervisorJob`, so the two launches fail independently.** Confirmed from
  `androidx.lifecycle`'s own `commonMain` source
  (`androidx/lifecycle/viewmodel/internal/CloseableCoroutineScope.kt`, shared across
  Android/iOS/JVM): `CloseableCoroutineScope(coroutineContext = dispatcher + SupervisorJob())`. An
  unhandled exception in one `init`-launched coroutine cannot cancel its sibling — each is already
  wrapped in its own `resultOf`/try-catch regardless.

**No action** — confirmed as a non-issue on both axes: the test-flakiness question the article
raises, and the underlying production-correctness question it doesn't directly ask but which the
"is this actually broken?" framing implies. Worth remembering if a future test on one of these VMs
switches to asserting an ordered sequence of `state.test { awaitItem() }` calls instead of a final
`.value` snapshot — that would reintroduce the exact flakiness this article describes. The fix in
that case is to assert the final state instead, not to restructure the VM's `init`. Revisit the
production-correctness argument specifically if a future VM in this shape ever writes the *same*
field from two independent `init` launches — disjoint fields is what makes ordering irrelevant
here, and that would no longer hold.

### 4. Constructor-injected initial state (OTOS) (actionable — checklist step 3, investigate first)

Source: *Why your ViewModel is untestable* (2026-06-28). It coins its own rule, "OTOS — one test,
one state" (a corollary to the existing, non-newsletter-original "OTOA — one test, one assertion"
principle it cites first): a test should reach the state it's checking in one step, not by chaining
several setter calls. Its fix is to inject `initialState` via the ViewModel's constructor (defaulted
so existing call sites don't break), so a test can do
`createObjectUnderTest(initialState = RegisterUiState(email = "...", password = "...", ...))` in one
line instead of four sequential `viewModel.onXChanged(...)` calls.

That article itself cites a second source worth reading alongside it: *The Importance of One Test
One Assertion (OTOA) in Unit Testing* (DaniG, Treatwell Product Engineering Blog, 2025-03-02). It
refines OTOA more precisely than the newsletter's shorthand: the rule is one test asserts one
*behaviour*, not literally one assertion call — a single `assertThat(padawan).extracting("lightsaberColor", "dexterity", "name", "planetOfOrigin")`
still violates OTOA even though it's syntactically one statement, because it checks several
unrelated behaviours at once. Worth keeping that distinction in mind, since it's easy to satisfy
OTOA's letter (one `assertX` call) while missing its point (one behaviour under test).

Assessed 2026-08-29: mixed value for this codebase, not a uniform win.

- **Doesn't help** this project's common load-and-display VMs (`SettingsUserScreenViewModel`-shaped
  screens) — tests already reach the state under test in one line by configuring a fake's return
  value before calling `createViewModel()`. Injecting `initialState` would just relocate that one
  line, not remove setup cost.
- **Would genuinely help** multi-field form/edit screens (create task, edit sprint, and similar)
  where a test currently chains several `viewModel.onXChanged(...)` calls to reach the one state
  combination it's checking — the exact shape the article's four-setter register-form example
  complains about.
- **Not a uniform one-line change** the way the article's toy example implies: several of this
  project's VMs compute part of their default state from injected repos/storage at construction
  time (e.g. `SettingsUserScreenViewModel` derives `isUnencryptedConnection` from
  `serverStorage.server`) rather than a bare `RegisterUiState()`. Adding `initialState` injection to
  those would need per-VM thought about how the derived fields and the injected default interact,
  not a blanket constructor-signature change.

Checklist step 3 scopes this as an investigation: prototype the pattern on one concrete multi-field
form VM (pick the one with the most setter-chaining in its existing tests) before deciding whether
to generalize it as a project convention.

**Resolved 2026-08-29: declined — no constructor change needed.** Grepped every `*ViewModelTest.kt`
for the longest single-test chain of `onXChange`/`setX` calls; `ProjectDetailsViewModelTest`'s
`onSaveClick - success` test had the worst offender in the codebase (6 chained setters to build one
target `ProjectDetailsState` before asserting the save). Prototyped fixing it two ways, without
touching any production code:

- **`ProjectDetailsViewModelTest`** (repo-loaded form): `save()` reads `_state.value`, and `init`
  copies the fake's `getProjectDetailsResult` straight into that state. Configuring the fake to
  return the *target* state directly (`details(name = "edited", ...)`) reaches it in one step —
  the fake's return value already **is** the constructor-time seam OTOS wants `initialState` for.
  Deleted the six `onXChange`/`onIsXChange` calls entirely; test still passes
  (`./gradlew :feature:settings:ui:jvmTest --tests "*ProjectDetailsViewModelTest*"`).
- **`CreateTaskViewModelTest`** (route/`SavedStateHandle`-driven form, no repo load in `init`):
  `onCreateTask - success` used to chain `setTitle`+`setDescription`. Pre-seeding the constructor's
  `SavedStateHandle` with `mapOf("title" to "...", "description" to "...")` reaches the same target
  state in one step, since state-init already calls `restorableState.restore(KEY, "")` against it —
  the same trick checklist step 2's process-death test already exercised, just not yet reused here.

Both are shapes the plan above called out as *not* needing `initialState` injection
("load-and-display VMs... tests already reach the state under test in one line by configuring a
fake's return value") and one it called out as a genuine form VM. The form-VM case turned out to
resolve the same way once its actual state-construction path (repo fake / `SavedStateHandle`) was
traced, rather than assuming `onXChange` calls were the only way in. **No VM examined lacked an
existing collaborator-based seam** (fake repository result, `SavedStateHandle` initial map, route
nav-args) sufficient to reach any target state in one step. Adding a dedicated `initialState`
constructor parameter would duplicate that path, not remove setup cost — it's a second, competing
way to set values the VM can already receive at construction time. Declining to adopt it as a
convention; both simplified tests are kept as the fix for the two worst chains found, without any
production-code change.

### 5. `AppInfoProvider.isDebug()` runtime facade (declined — no action)

Source: *How to keep debug code out of release builds* (2026-08-23).

`androidApp/src/main/kotlin/com/grappim/taigamobile/data/AppInfoProviderImpl.kt:13` wraps
`BuildConfig.DEBUG` behind a runtime-injected `AppInfoProvider` interface, used in `TaigaApp.kt`
(Timber tree selection, StrictMode setup) and `ImageLoaderProvider.kt` — exactly the anti-pattern
the article flags (R8 can't dead-code-eliminate a value that comes from the runtime DI graph the
way it can a true compile-time constant).

Declined: the article's fix is Android build-type source sets (separate `debug`/`release` DI
modules providing different implementations). That doesn't generalize to this project's iOS and
JVM/desktop targets — those platforms have no equivalent of an Android build type, so the facade
exists *because* this is KMP, not despite it (see `AppInfoProviderImpl.jvm.kt` and
`AppInfoProviderImpl.ios.kt`, each with their own platform-appropriate debug check). Revisit only if
this ever becomes a measured R8-size or test-coverage problem in practice on the Android target
specifically.

### 6. State design (validated — no action)

Source: *Sealed State vs. Data State* (2026-08-09). This project's `FeatureState` data-class
convention (CLAUDE.md's "ViewModel + State Pattern") already matches the article's recommendation
over a sealed-hierarchy state design. Nothing to change; kept here only so a future session doesn't
re-derive the same check.

### 7. Not applicable to this architecture

- *How to load ViewModel's data without using 'init'* (2026-07-19, the Startup-Intent pattern
  itself) and *How to remove 90% of Android app initialization logic and follow SRP principle*
  (2026-07-26, the Startup Task multibinding pattern) both presuppose an MVI Intent/reducer pipeline
  or Hilt-style multibinding this project doesn't have (Koin `@ComponentScan`, plain MVVM per
  CLAUDE.md). Neither is a drop-in fix without first adopting MVI as an architecture — a much larger
  decision than either article implies.

  **Queued (not started):** the second article's "the module graph pays for it" claim hasn't been
  checked against this codebase's actual Gradle graph yet — only the architectural-mismatch argument
  above has been assessed. Worth checking whether `composeApp` (the entry-point-equivalent module)
  shows the same ":app depends on everything" shape, and if so, whether it comes from the same cause
  the article describes (ad hoc per-feature startup-preload dependencies piling up in one class) or
  from something else (e.g. Koin's DI-module aggregation, or Nav3 screen-graph wiring) that the
  article's `StartupTask` fix wouldn't touch either way.
- *MVP vs MVVM vs MVI* (2026-08-02) is purely conceptual/historical — no codebase claim to check.
- The custom FIFO `flatMapConcurrently` operator, `scan`-based reducer, and abstract `MviViewModel`
  base class from *Why your MVI can't handle two Intents at once* (2026-07-05) — same reason as
  above, presupposes an Intent/reducer pipeline this project doesn't have. But unlike the other two,
  this article's underlying claim (two independent async writes to the same `StateFlow` can land
  out of submission order, "last write wins") isn't itself MVI-specific, and checking it against the
  codebase turned up a real instance — see finding 8 / checklist step 4.

### 8. Watch/unwatch last-write-wins race (done — checklist step 4, see CHECKLIST-DONE.md)

Source: *Why your MVI can't handle two Intents at once* (2026-07-05). Its running example: a
banking screen where "pay" and "cancel" are two independent async actions on the same resource: the
last network response to land wins, regardless of which the user tapped last or which the user
tapped at all after the first.

Checked whether this project has any comparable pair of independent, overlapping async writes to
the same state field. `WorkItemWatchersDelegateImpl`
(`feature/workitem/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/workitem/ui/delegates/watchers/WorkItemWatchersDelegateImpl.kt`)
has exactly this shape:

- `handleAddMeToWatchers` (watch) and `handleRemoveMeFromWatchers` (unwatch) are each `suspend`
  functions called from their own independent `viewModelScope.launch` block
  (`TaskDetailsViewModel.onAddMeToWatchersClick` / `onRemoveMeFromWatchersClick:658-670`, and the
  same pair in `UserStoryDetailsViewModel`, `EpicDetailsViewModel`, `IssueDetailsViewModel` — all
  four detail screens share this delegate).
- Each does a multi-step async chain (watch/unwatch API call → `getUpdateWorkItem` →
  `getUsersList`) before writing `_watchersState.update { it.copy(..., isWatchedByMe = result.isWatchedByMe) }`.
  No versioning or cancellation-of-prior-request guards the write.
- The watch/unwatch button
  (`feature/workitem/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/workitem/ui/widgets/WatchersWidget.kt:92-103`)
  is never disabled while `watchersState.areWatchersLoading` is true — it only checks `isOffline`
  (`TaigaTextButtonWidget`'s `enabled = !isOffline`, no loading param). So the double-tap that
  creates the race is directly reachable: watch, then unwatch before the first request returns —
  whichever response lands second overwrites `isWatchedByMe` with its own (possibly stale) result.

**Confirmed real, contradicts the initial read that this article had nothing useful.** The fix does
not require adopting the article's own machinery (custom `flatMapConcurrently`, `scan` reducer,
abstract `MviViewModel` base class, `UiStateMachine`) — this project's plain MVVM pattern can close
the race the simpler way: prevent the overlapping requests from firing at all. Pass
`isOffline = isOffline || watchersState.areWatchersLoading` to the watch/unwatch
`TaigaTextButtonWidget` call in `WatchersWidget.kt` — reuses the existing disabled-button visual,
one line, no new prop.

**Secondary source (2026-08-29):** *What Are Optimistic Updates?* (Kyle DeGuzman, Medium,
2022-11-16) — cited by the SRP-init article ("Authorized + non-blocking... can run optimistically"),
generic front-end concept, not Android-specific: update the UI immediately assuming success, revert
and show an error if the server rejects it; its own guidance is to use this for binary,
low-consequence actions (its worked example is literally Like/Unlike) and to avoid it where a revert
would cascade elsewhere.

Watch/unwatch is exactly that shape, and this codebase already has a full implementation of the
pattern elsewhere: `KanbanViewModel.moveStory()`
(`feature/kanban/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/kanban/ui/KanbanViewModel.kt:190-236`)
updates `_state` immediately on drag-and-drop reorder, fires the network call, and reverts to
`previousStoriesByStatus` plus surfaces an error on failure (`computeOptimisticUpdate` at line 238).
So watch/unwatch's current blocking implementation (spinner, wait for the full round trip) is
inconsistent with a convention this project already uses for a comparable low-consequence toggle —
worth considering as a *further* option for checklist step 4, beyond just disabling the button:
flip `isWatchedByMe` immediately and revert on failure, the same shape as `moveStory()`. Not
folded into step 4's fix directly — the disable-button fix alone already closes the race with a
one-line change; going optimistic is a separate, larger step (still needs the same overlap guard,
or a rethink of it, to avoid two optimistic toggles racing the same way) and hasn't been scoped.
Noting it here as an option to weigh when step 4 is picked up, not committing to it.

### 9. Redundant `init`-time re-fetches of already-known/rarely-changing data (done — checklist step 5, see CHECKLIST-DONE.md)

Source: *How to load ViewModel's data without using 'init'* (2026-07-19), its Issue 3 ("Customer
that never returns") — distinct from finding 3 above, which only checked that article's Issue 1
(test flakiness). Issue 3's argument: unconditionally re-running an `init`-time load on every VM
reconstruction, even for data that already loaded successfully and rarely changes, creates an
avoidable failure window — a transient network blip on the re-fetch turns a screen that *was* fine
into an error, for data that didn't need re-fetching at all.

This is a different root cause from checklist step 2 (process-death loses *user-entered* input) —
Issue 3 is about read-only/rarely-changing data getting needlessly re-risked on *any* VM
reconstruction, not specifically process death.

**Static-grep pass (corrects the checklist's own candidate list).** `getPermissions()` is called
unconditionally from `init`/its `loadData()` in 8 ViewModels, not the 5 originally listed:
`WikiPagesViewModel`, `WikiBookmarksViewModel`, `ScrumBacklogViewModel`, `ScrumOpenSprintsViewModel`,
`EditSprintViewModel`, `EpicsViewModel`, `SprintViewModel`, `IssuesViewModel`. `KanbanViewModel` —
named in the checklist as a candidate — does **not** call `getPermissions()` at all; it was carried
over from finding 3's *different* grep (2+ independent `init`-time launches, where Kanban's second
launch is `loadFiltersData()`, not permissions) and the two lists got conflated when step 5 was
scoped.

**`getPermissions()` turned out not to match the article's actual concern.** Traced the call:
`ProjectsRepositoryImpl.getPermissions()` → `getCurrentProjectSimple()` →
`projectDao.getProjectById(currentProjectId)` — a **local Room read**, not a network call (Room
data is written by whatever last synced the project, e.g. `fetchAndSaveProjectInfo()` on Dashboard
load). Issue 3's "transient network blip" risk doesn't apply to it — a local DB read essentially
can't fail under normal operation. Re-running it on every `init` is a wasted read, not a
revenue-risking failure window. **Not actionable under this article's argument.**

**`loadFiltersData()` (→ `getFiltersData()`) is the real match.** Called unconditionally from
`init` in `EpicsViewModel`, `IssuesViewModel`, `KanbanViewModel`, `ScrumBacklogViewModel` — traced to
`FiltersRepositoryImpl.getFiltersData()` → `filtersApi.getCommonTaskFiltersData(...)`, a genuine Ktor
network call for a task type's filter options (statuses/tags/priorities/assignees), re-issued on
every re-entry into these four list screens even though a project's filter options rarely change
mid-session.

**Live-verified on `IssuesViewModel`/`IssuesScreen`** (`Medium_Phone_API_36.1`, fdroid debug,
2026-08-29): loaded the Issues screen online (list + filters both fine) → backgrounded, `am kill`
(confirmed dead via `pidof`) → `adb shell cmd connectivity airplane-mode enable` → relaunched with
`am start -n` (`Warning: Activity not started, its current task has been brought to the front`,
confirming genuine task resume) — Nav3 restored directly to Issues (step 2's fix holding), but with
a **fresh** `IssuesViewModel`, whose `init` re-ran `getPermissions()` and `loadFiltersData()` against
no network. Result matched Issue 3's prediction and then some: "Show filters" got a red warning
badge (`filtersError` set) as expected, but the *dominant* effect was the entire issues list being
replaced by a full-screen "Connection error" + Retry — hiding the exact same list the user had been
looking at seconds earlier. Re-enabling airplane mode + tapping Retry (`issues.refresh()` +
`state.retryLoadFilters()`, `IssuesScreen.kt:157-161`) fully recovered both.

**The full-screen wipeout is a separate, bigger mechanism than either scoped candidate — and turns
out to be an already-tracked, already-deferred gap, not a new one.** `IssuesScreen.kt`'s
`issues.hasError() && issues.isEmpty()` branch (line 152) is what renders it —
`IssuesRepositoryImpl.getIssuesPaging()` builds a plain `Pager`/`IssuesPagingSource` straight from
`WorkItemApi`, bypassing `WorkItemRepositoryImpl`'s cache-first `getWorkItems()` entirely, so none of
`docs/architecture/offline-support.md`'s Phase 3 caching applies to list screens. That doc already
names this precisely: Phase 4 is "⚠️ PARTIAL — Sprint RemoteMediator done; WorkItem deferred", and its
"WorkItem RemoteMediator (Complex)" future-work section already explains why (complex server-side
filters). This session's contribution is confirming *live* that the deferred gap produces a real,
visible UX regression (not just a theoretical one) — added as a dated note there rather than a new
checklist item here, to avoid tracking the same gap in two places. Same structural shape likely
affects Epics, Kanban, and ScrumBacklog (same `getXxxPaging()` pattern) — not individually confirmed
live.

**Conclusion:** `getPermissions()` — no action, doesn't match the article's risk model.
`loadFiltersData()` — confirmed real, but a fix there (e.g. skip re-fetch if already loaded, keep
last-known filters on failure) wouldn't fix the actual dominant symptom users would hit, since that's
driven by the pre-existing, already-deferred Paging-cache gap, not the filters call. See
`docs/architecture/offline-support.md`'s "WorkItem RemoteMediator (Complex)" section for that gap's
existing tracking — not duplicated as a new checklist step here.

### 10. Does the watch/unwatch race generalize? (done — checklist step 6, see CHECKLIST-DONE.md)

Prompted by gregory re-reading finding 8 (2026-08-29): that finding confirmed one instance of
"nothing defines update order, any method mutates any field from any coroutine" (the *Why your MVI
can't handle two Intents at once* claim) — `WorkItemWatchersDelegateImpl`'s watch/unwatch race — but
never swept the rest of the codebase for the same shape. It only checked "any comparable pair" that
turned up while investigating watchers specifically, not every ViewModel/delegate.

Claim to check: are there other places with two-or-more independent `viewModelScope.launch` blocks
(or delegate-launched coroutines) that can write to overlapping state fields with no
cancellation/versioning/ordering guard, reachable by a user firing both before the first resolves —
the same last-write-wins shape as watch/unwatch?

**Yes — confirmed the same shape in three more delegates, plus a gap step 4 itself left open.**
Static-grep pass over `feature/workitem/ui/.../delegates/*` for an `areXxxLoading`/`isXxxLoading`
flag that's tracked in state and shown as a spinner, but not used to gate the button(s) that fire the
write:

- **`WorkItemWatchersDelegateImpl.handleRemoveWatcher`** (the per-watcher remove icon in
  `WatchersWidget`, via the shared `TeamUserWithActionWidget`) writes to the *same* `_watchersState`
  step 4 already fixed the toggle button for — but the remove icon
  (`TeamUserWidget.kt:99-107`, `enabled = !isOffline`) was never included in step 4's fix (which was
  scoped only to "the watch/unwatch button"). Worse than a UI toggle glitch: `handleRemoveWatcher`
  computes `newWatchers`/`watchersToSave` by filtering `_watchersState.value.watchers` — a **stale
  snapshot** taken at call time — so two rapid removes (or a remove racing the toggle button) can
  silently undo each other's result, not just show a wrong loading state.
- **`WorkItemSingleAssigneeDelegateImpl` / `WorkItemMultipleAssigneesDelegateImpl`** — identical
  shape to watchers pre-fix: `isAssigneesLoading` is tracked and shown via `DotsLoaderWidget`
  (`AssignedToWidget.kt:128`), but the Assign-to-me/Unassign toggle
  (`AssignedToWidget.kt:160-171`, `isOffline = isOffline`) and the per-assignee remove icon (same
  `TeamUserWithActionWidget` as watchers) are both ungated. `handleRemoveAssignee` (multiple) has the
  same stale-snapshot problem as `handleRemoveWatcher` above — computes `newAssignees` by filtering
  `_multipleAssigneesState.value.assignees` at call time.
- **`WorkItemTagsDelegateImpl.handleTagRemove`** — same shape again: `areTagsLoading` shown as a
  `CircularProgressIndicator` (`WorkItemTagsWidget.kt:62-67`), but each tag chip's remove click
  (`TagItemWidget`, gated only by `isOffline`) is not gated by it, and `handleTagRemove` computes
  `newTags` from a stale `_tagsState.value.tags` snapshot too — reachable any time 2+ tags are shown,
  which is common.

**Checked and set aside as lower-risk:** `WorkItemSprintDelegateImpl` (edit/create sprint) and
`WorkItemDueDateDelegateImpl` — both are dialog-gated single-confirm actions, not a pair of
independently-clickable buttons/icons sitting on the screen at once, so the "user fires both before
the first resolves" reachability that makes watchers/assignees/tags real doesn't apply the same way.
Not dug into further, per the shortlist approach.

**Root cause is identical across all four (three new + the original) — the same fix pattern applies.**
Every case is: an `areXxxLoading`/`isXxxLoading` flag already exists in state and is already rendered
as a loading spinner, but the actionable button(s)/icon(s) are gated only by `isOffline`, not by that
loading flag too. Step 4's fix (`isOffline = isOffline || state.areXxxLoading`) is a direct, one-line
template at each site — no new investigation needed to know *what* the fix looks like, just where to
apply it. Not applied here (step 6 is investigate-only) — applied as checklist step 8.

No live reproduction done for these three (unlike step 4's original, which also wasn't live-verified
before its fix — see finding 8 — and unlike step 5, where live reproduction was the only way to
surface the Paging-cache mechanism). The concurrency proof here is direct from the code: shared
mutable state, no cancellation/ordering guard, a UI trigger not gated by the in-flight flag, and (for
the three remove-actions) a stale-snapshot read that would produce visibly wrong data — no live click
sequence needed to establish this is real, and this session's earlier click-flakiness with the
desktop build (`docs/frictions.md`, 2026-08-29) made another live-repro attempt low-value.

### 11. UiState-leak and derived-property convention (done — checklist step 7, see CHECKLIST-DONE.md)

Source: *Sealed State vs. Data State* (2026-08-09), re-read 2026-08-29. Finding 6 already validated
this article's headline claim (data class over sealed hierarchy — this project already does that),
but two more specific sub-claims from the same article weren't checked yet:

1. **UiState ≠ State ("UI-decision leak").** The article's point survives the sealed-vs-data choice:
   a field is a leak if it encodes a *rendering* decision (e.g. a raw `isEmpty: Boolean` the
   ViewModel computed) rather than raw data the UI itself should decide how to interpret.
2. **Derived `get()` properties for UI-only booleans.** The article's fix keeps rendering-decision
   booleans as computed `get()` properties on the State class (not stored fields, not recomputed
   inline in a Composable/mapper) — removing one shouldn't force a ViewModel rebuild or test rewrite.

**Static-grep pass over all 37 `*State.kt` files under `feature/*/ui`.** No violation of sub-claim 1
found. Grepped for `isXxxEmpty`/`showXxx`/`isXxxVisible`-shaped fields: the only matches are
`isXxxDialogVisible`/`isXxxAlertVisible` (SettingsState, SprintState, IssueDetailsState,
TrustedCertificatesState, WikiBookmarksState, TagsScreenState, ProjectValuesState, WikiPagesState,
LoginState, EpicDetailsState, and the `EditXxxState`/`SprintDialogState` family) — these are genuine
raw UI state (a user directly opens/closes a dialog; nothing else in the same State class determines
that), not a derived rendering decision duplicating another field. No `isEmpty`-shaped field exists
anywhere, stored or derived — list screens (`WikiPagesState.allPages`, `KanbanState.stories`, etc.)
don't store an emptiness flag at all.

**Sub-claim 2's convention already exists — in exactly the one place it's warranted, and via two
different mechanisms this project already uses.** Only one `*State.kt` has a `get()` property:
`CustomFieldItemState.isModified` (`originalValue != currentValue`). Checked its usages: referenced
in `WorkItemCustomFieldsDelegateImpl`, three separate spots in `CustomFieldsWidget.kt` (indication
color, save-button `enabled`, focus state), and a test assertion — five-plus call sites, genuinely
avoiding a duplicated `originalValue != currentValue` computation. That matches both the article's
convention **and** this project's own "no abstraction for single-use code" rule (CLAUDE.md, Coding
Guidelines) — it earns the `get()` because it's actually multi-use, not by default.

The other mechanism, used more often here than `get()` properties: **a shared widget that takes raw
fields and computes the render-branch decision once, internally.** `WikiPagesScreen.kt` doesn't
inline `allPages.isEmpty() && !isLoading`-shaped logic at all — it hands `items`/`isLoading`/`error`
straight to `WikiListContentWidget`, which owns the branching. For the four Paging-backed list
screens (Issues, Epics, Kanban, ScrumBacklog), the same de-duplication happens via extension
functions in `utils/ui/.../PagingUtils.kt` — `LazyPagingItems<*>.hasError()`, `.isNotLoading()`,
`.hasCompletedLoad()` — each defined once, called from every screen's `when` branch instead of each
screen re-deriving the same `loadState` logic inline. This achieves the article's actual goal (don't
duplicate a derived rendering decision in more than one place) through composition across screens
rather than a computed property per State class — arguably a better fit for this project's
widget-heavy architecture than adding a `get()` to every list `*State.kt`, since the derived logic
is genuinely shared *across* screens, not just across call sites within one screen.

**Conclusion: no violation, no action.** The convention this article argues for is already followed
in substance — a `get()` property where reuse actually justifies it, a shared widget/extension where
the same derived logic is needed by more than one screen, and no stored field anywhere that
duplicates a UI-rendering decision the raw data already encodes. Not worth writing up as a new
CLAUDE.md rule since it isn't a rule being missed — it's already how the code is structured, just not
under this vocabulary.

## Candidate for agentic-grappim (project-agnostic, not TaigaMobileNova-specific)

While digging into finding 1 (2026-08-29 discussion), worked out the actual platform-dependent
behavior of an unhandled root-coroutine exception with no `CoroutineExceptionHandler` — this is
general Kotlin/coroutines knowledge, not specific to this codebase, so it belongs in a shared
skill/reference in `agentic-grappim` rather than only living here:

- **JVM/Desktop**: falls through to the thread's default uncaught-exception handler, which just
  prints the stack trace to `stderr` and lets that one pool thread die; the pool replaces it. Truly
  silent in production — nobody's watching stderr.
- **Android**: the OS installs a process-wide default `Thread.UncaughtExceptionHandler` at startup
  that logs a "FATAL EXCEPTION" and kills the process. If a crash-reporting SDK (Crashlytics,
  Bugsnag, etc.) is present, it chains onto that same handler — reports the exception as a
  **fatal** crash, then still kills the process. So on Android this is generally *not* silent: it's
  either a reported crash (SDK present) or a visible, unreported "app has stopped" (SDK absent) —
  contrary to how some blog posts frame this as universally invisible. `SupervisorJob` itself has no
  bearing on any of this — it only stops sibling coroutines/parent from being cancelled; it says
  nothing about logging or crashing.
- **iOS/Kotlin-Native**: an uncaught exception in a worker/coroutine similarly terminates the
  process by default.

Net: "add a `CoroutineExceptionHandler`" is universally good advice, but *why* differs by platform
— on JVM it turns an invisible failure into a visible one; on Android/iOS it turns an app crash
into a contained, logged failure. Worth a short shared note (or an addition to an existing
coroutines-related skill) next time `agentic-grappim` gets touched, rather than only living in this
project's plan doc.

**Second candidate (2026-08-29, discussing finding 2):** the `UiStateMachine` pattern itself
(`SavedStateHandle`-backed state holder exposing `isStateRestored`, so `init` can skip a reload
that would otherwise stomp on just-restored state) is a reusable, project-agnostic
MVVM/Compose best practice — not specific to TaigaMobileNova's architecture, just not yet adopted
here. Worth writing up as a general pattern in `agentic-grappim` (what problem it solves, the
shape of the wrapper, the `isStateRestored` gate) independent of whether/where this project ends
up adopting it. Two caveats to carry into that write-up, both surfaced while assessing it for this
project and probably relevant anywhere it's proposed:
- The pattern as commonly described assumes single-platform Android + Hilt
  (`@HiltViewModel constructor(savedStateHandle: SavedStateHandle)`) — worth checking case by case
  whether `SavedStateHandle` is actually usable the same way in a KMP or non-Hilt-DI project before
  presenting it as a drop-in recipe.
- `@Parcelize` is the natural serialization choice for the state class, but doesn't play well with
  `kotlinx-collections-immutable` types (`ImmutableList`, etc.) out of the box — worth noting
  `@Serializable` + a JSON blob in the Bundle as the alternative for projects already on
  kotlinx-serialization.
