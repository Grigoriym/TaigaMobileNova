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

**No action.** Worth remembering if a future test on one of these VMs switches to asserting an
ordered sequence of `state.test { awaitItem() }` calls instead of a final `.value` snapshot — that
would reintroduce the exact flakiness this article describes. The fix in that case is to assert the
final state instead, not to restructure the VM's `init`.

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

### 8. Watch/unwatch last-write-wins race (actionable — checklist step 4)

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

### 9. Redundant `init`-time re-fetches of already-known/rarely-changing data (actionable — checklist step 5, investigate first)

Source: *How to load ViewModel's data without using 'init'* (2026-07-19), its Issue 3 ("Customer
that never returns") — distinct from finding 3 above, which only checked that article's Issue 1
(test flakiness). Issue 3's argument: unconditionally re-running an `init`-time load on every VM
reconstruction, even for data that already loaded successfully and rarely changes, creates an
avoidable failure window — a transient network blip on the re-fetch turns a screen that *was* fine
into an error, for data that didn't need re-fetching at all. The article frames this as a direct
revenue-loss argument ("Any error, any additional click, any lost state - it's an additional user
action, which always leads to revenue loss. → Don't rely on perfect network conditions. Avoid
requests as much as you can.").

This is a different root cause from checklist step 2 (process-death loses *user-entered* input) —
Issue 3 is about read-only/rarely-changing data getting needlessly re-risked on *any* VM
reconstruction, not specifically process death.

Candidates already surfaced by finding 3's grep (same VMs, different reason this time):
`EpicsViewModel`, `IssuesViewModel`, `KanbanViewModel`, `ScrumBacklogViewModel`,
`EditSprintViewModel` all unconditionally call `getPermissions()` from `init` — permissions rarely
change mid-session, so every re-entry into these screens re-risks a network call that already
succeeded once, for data that's very unlikely to have changed.

Its implementation guideline (`MviConfig`, startup-vs-user Intent naming convention,
`@Restartable`-annotation filtering, abstract `MviViewModel` base class) is not applicable here —
same reason as findings 7/8: presupposes the MVI Intent/reducer pipeline this project doesn't have.

**Approach agreed with gregory (2026-08-29):** finding candidates is a static-grep pass — does the
`init`-time load fetch data that's already known or unlikely to change (permissions, nav-arg data,
an already-cached repository read)? That doesn't need live reproduction to enumerate. Only reproduce
live (emulator, airplane-mode toggle around a re-navigation) on the one or two candidates actually
picked, to confirm the UX regression is real — not as a blanket sweep across every VM.

Checklist step 5 scopes this as an investigation, not a commitment to change anything project-wide.

### 10. Does the watch/unwatch race generalize? (actionable — checklist step 6, investigate first)

Prompted by gregory re-reading finding 8 (2026-08-29): that finding confirmed one instance of
"nothing defines update order, any method mutates any field from any coroutine" (the *Why your MVI
can't handle two Intents at once* claim) — `WorkItemWatchersDelegateImpl`'s watch/unwatch race — but
never swept the rest of the codebase for the same shape. It only checked "any comparable pair" that
turned up while investigating watchers specifically, not every ViewModel/delegate.

Claim to check: are there other places with two-or-more independent `viewModelScope.launch` blocks
(or delegate-launched coroutines) that can write to overlapping state fields with no
cancellation/versioning/ordering guard, reachable by a user firing both before the first resolves —
the same last-write-wins shape as watch/unwatch?

Approach (same static-first pattern as findings 3/9): grep for state classes/delegates with more
than one independent `launch` site writing into the same `MutableStateFlow`/`_xxxState`, shortlist
candidates by whether the UI actually exposes two overlapping triggers (a button pair, a
toggle a user can double-tap, two rapid-fire actions on the same field) the way `WatchersWidget`
does — not every concurrent write is reachable by a real double-action. Only reproduce live on
whichever candidates survive the shortlist, not as a blanket sweep.

Checklist step 6 scopes this as an investigation, not a commitment to fix anything beyond what step
4 already covers.

### 11. UiState-leak and derived-property convention (actionable — checklist step 7, investigate first)

Source: *Sealed State vs. Data State* (2026-08-09), re-read 2026-08-29. Finding 6 already validated
this article's headline claim (data class over sealed hierarchy — this project already does that),
but two more specific sub-claims from the same article weren't checked yet:

1. **UiState ≠ State ("UI-decision leak").** The article's point survives the sealed-vs-data choice:
   a field is a leak if it encodes a *rendering* decision (e.g. a raw `isEmpty: Boolean` the
   ViewModel computed) rather than raw data the UI itself should decide how to interpret. Not yet
   checked whether any `FeatureState` in this codebase has a field like that, versus deriving such
   booleans from raw fields.
2. **Derived `get()` properties for UI-only booleans.** The article's fix keeps rendering-decision
   booleans as computed `get()` properties on the State class (not stored fields, not recomputed
   inline in a Composable/mapper) — removing one shouldn't force a ViewModel rebuild or test rewrite.
   Not yet checked whether this convention is actually followed anywhere in this codebase, or
   whether equivalent logic instead lives inline in Composables or in separate mapper functions.

Approach: static grep pass over `*State.kt` classes for (a) boolean/enum fields whose name reads as
a rendering decision rather than raw data (`isEmpty`, `showX`, `xVisible`-shaped names that aren't
already `get()` properties) and (b) existing `get()` properties on State classes, to see whether the
convention already exists in places and is just inconsistent, or is absent entirely. Only look at
live behavior/tests for whichever candidates the grep surfaces, not a blanket sweep.

Checklist step 7 scopes this as an investigation, not a commitment to change anything project-wide.

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
