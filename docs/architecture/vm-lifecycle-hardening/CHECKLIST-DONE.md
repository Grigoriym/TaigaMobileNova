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
