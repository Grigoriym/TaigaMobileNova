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
