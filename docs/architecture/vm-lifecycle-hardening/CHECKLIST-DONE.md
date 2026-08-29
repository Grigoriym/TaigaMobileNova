# ViewModel Lifecycle & Error-Handling Hardening — Done Steps

Archive of ticked steps from [CHECKLIST.md](CHECKLIST.md), kept for precedent when a later step
cites one by number. Not a place to look for open work.

## Step 1: Add a `CoroutineExceptionHandler` to `provideApplicationScope` — ✅ done 2026-08-29

Added a top-level `applicationScopeExceptionHandler` (`CoroutineExceptionHandler`) in
`core/async-kmp/src/commonMain/kotlin/com/grappim/taigamobile/core/asynckmp/KmpCorotuinesModule.kt`
that logs via `logcat(LogPriority.ERROR, throwable = throwable) { "Unhandled exception on
ApplicationScope" }`, and added it to `provideApplicationScope`'s `CoroutineScope(...)` context
alongside the existing `SupervisorJob() + defaultDispatcher`. No change to the function's
signature or call sites (`AuthStateManager.logout()`, `TaigaApp.onCreate()`).

Added `core/async-kmp/src/commonTest/kotlin/com/grappim/taigamobile/core/asynckmp/KmpCoroutinesModuleTest.kt`
(new `commonTest` source set for this module — none existed before) — installs a recording
`TaigaLogger` fake, launches a coroutine on the produced scope that throws, and asserts the
throwable was logged at `ERROR` with the original throwable instance, and that the exception did
not propagate out of `launch`/fail the test. Follows the file's `@file:OptIn(ExperimentalCoroutinesApi::class)`
convention (for `testScheduler`) already used elsewhere (e.g. `UserStoryDetailsViewModelTest.kt`).

**Verify:** `./gradlew jvmTest` — full suite green, including the new test. `./gradlew
:core:async-kmp:ktlintCheck` — green.

**Next:** step 2 is gated — do not start without asking gregory to decide process-death UI-state
restoration scope. Step 3 (OTOS investigation) is not gated and can be picked up instead.

## Step 2: Process-death back-stack restoration gap — ✅ done 2026-08-29

Root cause was neither of the two candidates the checklist named (Nav3's own restoration
mechanism was never broken). `MainNavHost`'s top-level `LaunchedEffect(initialNavState.isReady)`
was unconditionally calling `navigator.navigateToDashboardAsTopDestination()` — a
`resetTo(DashboardNavDestination)` that wipes every nav section — on every cold app start where
the user is logged in with a project selected, including a process-death relaunch after Nav3 had
already restored a deeper back stack (e.g. `Dashboard` → `CreateTask`). That reset stomped the
correctly-restored stack down to bare `Dashboard` every time.

Fix (`composeApp/src/commonMain/kotlin/com/grappim/taigamobile/main/MainNavHost.kt`): gated the
reset on `navigationState.currentKey == LoginNavDestination` (the seed value — true only when
nothing has diverged the stack yet). Verifying the fix on the emulator surfaced a second bug in
the same code path: with the reset skipped, the restored screen rendered correctly underneath but
the splash screen never dismissed, because `ScreenReadySignalController.signalReady()` was only
called from the `Login`/`ProjectSelector`/`Dashboard` `entry<>` blocks (see that class's doc
comment) — a restored deeper screen never composes any of those three. Fixed by calling
`screenReadySignal.signalReady()` directly in the gated `LaunchedEffect`'s `else` branch. Full
mechanism, both bugs, and the emulator verification steps are in IMPLEMENTATION_PLAN.md's
"Process-death UI-state restoration" section.

**Verify:** `./gradlew jvmTest` and `./gradlew ktlintCheck` both green. Emulator-verified on
`Medium_Phone_API_36.1` (fdroid debug): filled the Create Task form, backgrounded, `am kill`
(confirmed dead via `ps`), relaunched with `am start -n` (`sz=1`, genuine task resume) — landed
directly back on Create Task with both fields intact, no stuck splash. Re-verified the untouched
fresh-start path (force-stop, relaunch with nothing to restore) still lands cleanly on Dashboard.

**Next:** step 3 (OTOS investigation) is not gated and is ready to start. `RestorableState` can now
also be rolled out to further form screens if wanted — that rollout was parked pending this fix and
is not itself scoped as a checklist step yet.

## Step 3: Investigate constructor-injected `initialState` (OTOS) — ✅ done 2026-08-29

Grepped every `*ViewModelTest.kt` for the longest single-test chain of `onXChange`/`setX` calls to
find the concrete candidate the step asked for; `ProjectDetailsViewModelTest`'s `onSaveClick -
success` test was the worst offender (6 chained setters). Prototyped removing that chain two ways,
without any production-code change:

- `ProjectDetailsViewModelTest` (repo-loaded form): `save()` reads `_state.value`, which `init`
  populates straight from the fake's `getProjectDetailsResult`. Setting the fake's result to the
  *target* state directly reaches it in one step — the fake's return value already is the
  constructor-time seam OTOS wants a new `initialState` param for.
- `CreateTaskViewModelTest` (route/`SavedStateHandle`-driven form, no repo load in `init`):
  pre-seeding the constructor's `SavedStateHandle` with the target title/description reaches the
  same state in one step, reusing the `restorableState.restore(...)` path step 2 already built.

**Declined the convention.** No VM examined lacked an existing collaborator-based seam (fake repo
result, `SavedStateHandle` initial map, route nav-args) sufficient to reach any target state in one
step — a dedicated `initialState` constructor param would duplicate that path, not remove setup
cost. Both simplified tests are kept as the actual fix for the two worst chains found. Full
reasoning in IMPLEMENTATION_PLAN.md's "Constructor-injected initial state (OTOS)" section.

**Verify:** `./gradlew jvmTest` — full suite green. `./gradlew :feature:settings:ui:ktlintCommonTestSourceSetCheck
:composeApp:ktlintCommonTestSourceSetCheck` — green.

**Next:** steps 4-7 are all ungated and available; step 4 (watch/unwatch race fix) is the only one
with a concrete implementation already scoped rather than being an investigation.

## Step 4: Gate the watch/unwatch button on `areWatchersLoading` — ✅ done 2026-08-29

Applied exactly the fix the checklist scoped: `WatchersWidget.kt`'s watch/unwatch
`TaigaTextButtonWidget` now passes `isOffline = isOffline || watchersState.areWatchersLoading`
instead of bare `isOffline`. One-line change, reuses the existing disabled-button visual (same
pattern the widget already uses for `isOffline`) — no new prop, no state-shape change.

Did not additionally pursue the "worth weighing" optimistic-update alternative noted in the
checklist entry — the step scoped only the disable-button fix, and the note explicitly flagged
that path as undecided/not scoped.

**Verify:** `./gradlew jvmTest` — full suite green. `./gradlew :feature:workitem:ui:ktlintCheck` —
green. Live GUI verification on the desktop app (`:composeApp:run`) was attempted but
inconclusive: `xdotool` clicks against the app window registered only intermittently (see
`docs/frictions.md`'s 2026-08-29 entry) — same coordinates sometimes toggled the UI, sometimes did
nothing, with no error, across ~10 attempts on three different buttons including plain
back-navigation. Not caused by this change (the pre-existing "Watch" button was equally
unclickable). Verified the fix by code read instead: `areWatchersLoading` is set true for the
duration of `handleAddMeToWatchers`/`handleRemoveMeFromWatchers` in
`WorkItemWatchersDelegateImpl.kt` and `TaigaTextButtonWidget`'s `isOffline` param already disables
the button and suppresses its `onClick` (same mechanism the widget's other `isOffline`-gated
buttons rely on) — confirmed by reading `TaigaTextButtonWidget`'s implementation.

**Next:** steps 5-7 are all ungated investigation steps and available in any order; none is more
scoped than the others. Step 5 (redundant init-time re-fetches) is next in checklist order.

## Step 5: Investigate redundant `init`-time re-fetches — ✅ done 2026-08-29

Static-grep pass corrected the checklist's own candidate list: `getPermissions()` is called
unconditionally in 8 ViewModels, not the 5 originally listed, and `KanbanViewModel` — named as a
candidate — doesn't call it at all (that came from a different, conflated grep). Traced
`getPermissions()` to a **local Room read** (`projectDao.getProjectById`), not network — the
article's "transient network blip" risk doesn't apply to it, so it's not actionable.
`loadFiltersData()`/`getFiltersData()` (Epics, Issues, Kanban, ScrumBacklog list screens) **is** a
genuine unconditional network re-fetch matching the article's claim.

Live-verified on `IssuesViewModel`/`IssuesScreen` (`Medium_Phone_API_36.1`, fdroid debug): process
death + airplane mode + relaunch reproduced the predicted failure, but the dominant visible effect
was bigger than expected — the entire issues list vanished behind a full-screen "Connection error",
not just a filters warning badge. Traced that to a separate mechanism, which turned out to already be
a known, deferred gap: `docs/architecture/offline-support.md`'s Phase 4 already flags "WorkItem
[Paging] deferred" — `IssuesRepositoryImpl.getIssuesPaging()` bypasses the cache-first
`WorkItemRepositoryImpl.getWorkItems()` entirely, so no RemoteMediator/Room backs list screens' Paging
sources. Added a dated confirmation note to that doc's "WorkItem RemoteMediator (Complex)" section
rather than opening a new, duplicate tracking item. Full evidence and reasoning in
IMPLEMENTATION_PLAN.md finding 9.

**No fix applied — this was investigate-only per its scope.** `getPermissions()` — declined, doesn't
match the source article's risk model (it's a local Room read, not network). `loadFiltersData()` —
confirmed real but secondary; a fix there wouldn't have prevented the actual UX regression observed,
which traces to the pre-existing, already-tracked Paging-cache gap instead.

**Verify:** live reproduction on device/emulator as described above (no code changed, so no
`jvmTest`/`ktlintCheck` run).

**Next:** steps 6 and 7 are ungated investigation steps and available in either order.

## Step 6: Investigate whether the watch/unwatch race generalizes — ✅ done 2026-08-29

Yes — the same shape (an `areXxxLoading` flag tracked in state and shown as a spinner, but not used
to gate the button/icon that fires the write) recurs in three more delegates:
`WorkItemWatchersDelegateImpl.handleRemoveWatcher` (the per-watcher remove icon — step 4's fix only
covered the toggle button, not this, even though it's the same `_watchersState`),
`WorkItemSingleAssigneeDelegateImpl`/`WorkItemMultipleAssigneesDelegateImpl` (Assign-to-me/Unassign
toggle + per-assignee remove), and `WorkItemTagsDelegateImpl.handleTagRemove` (per-tag remove chip).
The three remove-actions are worse than a UI glitch — each computes its patch payload from a stale
`_xxxState.value` snapshot taken at call time, so two rapid removes can silently undo each other's
result. `WorkItemSprintDelegateImpl`/`WorkItemDueDateDelegateImpl` checked and set aside — both are
dialog-gated single-confirm actions, not a reachable button pair. Full evidence in
IMPLEMENTATION_PLAN.md finding 10.

**No fix applied — investigate-only per its scope**, despite the fix pattern being obvious (identical
to step 4's, at three more sites) — added as new ungated checklist step 8 rather than applied inline,
consistent with how step 5 stayed investigate-only. No live reproduction attempted for these three;
the concurrency proof is direct from the code (shared mutable state, no ordering guard, ungated
trigger, stale-snapshot read) and didn't need a click sequence to establish, unlike step 5's Paging
finding which only became visible by actually running the app.

**Verify:** code-read only, no production code changed this step (`jvmTest`/`ktlintCheck` not run —
nothing to verify).

**Next:** step 7 is ungated and available. Step 8 (new) is ungated too — same fix pattern as step 4,
just at three more call sites.

## Step 7: Investigate UiState-leak and derived-property convention — ✅ done 2026-08-29

Static-grep pass over all 37 `*State.kt` files under `feature/*/ui` found no violation of either
sub-claim. No `isXxx`-shaped field encodes a derived rendering decision anywhere — the only
`*Visible`-shaped fields are dialog-open flags (raw user-toggled state, not a leak), and no list
screen stores an `isEmpty`-style field at all. The article's `get()`-property convention already
exists in the one place it's earned: `CustomFieldItemState.isModified`, used at 5+ call sites
(delegate, three widget spots, a test) — genuinely avoiding duplicated `originalValue != currentValue`
logic, consistent with CLAUDE.md's own "no abstraction for single-use code" rule. Everywhere else,
the same de-duplication goal is met a different way this project already relies on more heavily: a
shared widget (`WikiListContentWidget`) or shared extension functions
(`utils/ui/.../PagingUtils.kt`'s `hasError()`/`isNotLoading()`/`hasCompletedLoad()`) computing the
render-branch decision once, called from every screen that needs it, rather than a `get()` per State
class. Full evidence in IMPLEMENTATION_PLAN.md finding 11.

**No action — the convention is already followed in substance**, just via composition (shared
widgets/extensions) more often than via `get()` properties. Not written up as a new CLAUDE.md rule
since nothing here is being missed; it's already how the code is structured.

**Verify:** static analysis only, no code changed (no `jvmTest`/`ktlintCheck` to run).

**Next:** step 8 (extend step 4's fix to three more delegates) is the only step left — ungated, ready
to start.

## Step 8: Extend step 4's button-gating fix to the delegates step 6 found — ✅ done 2026-08-29

Applied the same one-line pattern as step 4 (`isOffline = isOffline || <state>.areXxxLoading`) at the
three sites step 6 found:
- `WatchersWidget.kt`'s per-watcher remove icon (`TeamUserWithActionWidget` call) — now
  `isOffline || watchersState.areWatchersLoading`.
- `AssignedToWidget.kt`'s per-assignee remove icon and the Assign-to-me/Unassign toggle — both now
  `isOffline || isAssigneesLoading` (shared by `SingleAssignedToWidget`/`MultipleAssignedToWidget`,
  so both delegates are covered by the one widget change).
- `WorkItemTagsWidget.kt`'s per-tag remove click (`TagItemWidget`) — now
  `isOffline || tagsState.areTagsLoading`.

Did not touch "Add assignee"/"Add watcher"/"Edit tags" — those open a separate screen/dialog rather
than directly writing to the racing state, so they're not part of the confirmed race.

**Verify:** `./gradlew jvmTest` — full suite green. `./gradlew :feature:workitem:ui:ktlintCheck` —
green. GUI-verified on `Medium_Phone_API_36.1` (fdroid debug, adb-driven — this session's `xdotool`
flakiness was specific to the desktop build, not the emulator): installed the rebuilt APK, opened
Issue #82's detail screen, confirmed Tags/Assigned-to/Watchers all render correctly and their
buttons stay enabled in the idle (not-loading) state — no regression. Did not attempt to force and
screenshot the actual in-flight-disabled state (would need airplane-mode timing per the
emulator-testing skill); the fix is mechanically identical to step 4's, which was accepted on code
+ test evidence alone.

**Next:** queue is empty — all 8 checklist steps are done. The initiative's remaining open item is
IMPLEMENTATION_PLAN.md finding 9's Paging-cache gap, which was deliberately routed to
`docs/architecture/offline-support.md` instead of tracked here (see step 5's note above).
