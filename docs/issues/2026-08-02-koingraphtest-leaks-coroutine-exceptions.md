# 2026-08-02 — KoinGraphTest leaks throwing coroutines that fail unrelated tests

**Status:** Done
**Link:** none — found locally while running `./gradlew jvmTest` for
[testing improvement-plan](../testing/improvement-plan.md) task 3
**Updated:** 2026-08-02

## Report

Not a user report. `./gradlew jvmTest` failed on a working branch whose diff touches no code
in `composeApp`:

```
MainViewModelTest[jvm] > initialNavState - logged in no project - startDestination is ProjectSelector[jvm] FAILED
java.lang.IllegalStateException: The query result was empty, but expected a single row to return
a NON-NULL object of type 'com.grappim.taigamobile.core.storage.db.entities.ProjectEntity'.
    at com.grappim.taigamobile.core.storage.db.dao.ProjectDao_Impl.getProjectById$lambda$0(ProjectDao_Impl.kt:166)
    at com.grappim.taigamobile.feature.projects.data.ProjectsRepositoryImpl.getCurrentProjectSimple(ProjectsRepositoryImpl.kt:79)
    at com.grappim.taigamobile.feature.projects.data.ProjectsRepositoryImpl.getPermissions(ProjectsRepositoryImpl.kt:92)
    at com.grappim.taigamobile.feature.scrum.ui.backlog.ScrumBacklogViewModel$getPermissions$1.invokeSuspend(ScrumBacklogViewModel.kt:75)
```

**Environment:** JVM (`jvmTest`), Gradle 9.6.1, kotlinx-coroutines 1.11.0, Room + BundledSQLiteDriver.

**What the failure does not say:** which test *caused* it. The named test is not the culprit —
`MainViewModelTest` is built entirely from fakes (`FakeProjectsRepository`, `FakeDatabaseWrapper`)
and never reaches Room or `ProjectDao_Impl`. Nothing in its own stack appears in the trace.

## Findings

**1. The exceptions originate in `KoinGraphTest`, not in the test that fails.**
The frames name `ScrumBacklogViewModel`, `EpicsViewModel` and `WikiPagesViewModel` — classes
instantiated nowhere in the suite except `KoinGraphTest`, which resolves *every* definition in the
graph (`composeApp/src/jvmTest/kotlin/com/grappim/taigamobile/di/KoinGraphTest.kt:76`).

**2. Those ViewModels launch real work from `init`.** Ten ViewModels call `loadData()` or
`getPermissions()` in an `init` block, e.g.
`feature/wiki/ui/.../WikiPagesViewModel.kt:46-54`:

```kotlin
init { loadData() }

private fun loadData() {
    viewModelScope.launch {
        getPermissions()
        fetchPages()
    }
}
```

`getPermissions()` reaches `ProjectsRepositoryImpl.getCurrentProjectSimple()`
(`feature/projects/data/.../ProjectsRepositoryImpl.kt:77-81`), which queries a real, empty Room
database and throws — Room's generated `getProjectById` requires a non-null row.

**3. The coroutines actually execute on JVM.** `configureKmp()` puts
`kotlinx-coroutines-swing` on every KMP module's `jvmMain`
(`build-logic/.../KmpConfiguration.kt:51`), so `Dispatchers.Main` resolves to the Swing EDT instead
of failing to initialise. `viewModelScope` therefore dispatches to a live background thread, and the
launched bodies run — asynchronously, at a time `KoinGraphTest` does not control and does not wait
for.

**4. `kotlinx-coroutines-test` installs a JVM-global uncaught-exception handler.** Verified in the
artifact on the classpath:

```
$ unzip -p kotlinx-coroutines-test-jvm-1.11.0.jar META-INF/services/kotlinx.coroutines.CoroutineExceptionHandler
kotlinx.coroutines.test.internal.ExceptionCollectorAsService
```

Registered via `ServiceLoader`, this handler is process-wide. It routes an uncaught exception from
**any** coroutine in the JVM to whichever `TestScope` is currently registered — that is, to whatever
`runTest` happens to be executing at that instant. It has no way to know which test spawned the
coroutine.

**5. Nothing suppresses the leak.** `KoinGraphTest` catches only what the constructor throws
synchronously (`KoinGraphTest.kt:81-92`). A coroutine launched from `init` escapes that `try` by
construction. The test's own KDoc already predicted the behaviour but judged it harmless
(`KoinGraphTest.kt:53-54`):

> some launch loads in `init` that hit Room and log a stack trace on a background thread. Both are
> expected noise.

That is correct only when no `runTest` is active. When one is, the exception is not logged — it
fails that test.

**6. Reproduced, and confirmed pre-existing.** A/B on the full suite, `--rerun-tasks` each time:

| Tree | `./gradlew jvmTest` |
|---|---|
| clean (`b5ff6186`) | BUILD SUCCESSFUL |
| + task-3 wiki changes | BUILD FAILED |
| + task-3 wiki changes, re-run | BUILD FAILED — **but on two different tests** |

The third run failed `isOffline - reflects networkMonitor isOnline` and
`initialNavState - logged in with project`, not the test that failed the first time. A defect whose
victim changes between identical runs is a race, not a dependency. `:composeApp:jvmTest` run alone
passes on both trees; only the full-suite run fails.

**7. The task-3 change is the trigger, not the cause** — *(inference, but well-supported)*. Adding
`api(projects.feature.wiki.data)` to `:testing` alters `composeApp`'s jvmTest classpath, which
changes test-class discovery order, which changes whether a `runTest` is in flight when the leaked
coroutine lands. Any classpath or timing change can flip it either way. The clean tree passing is
luck, not correctness.

## Root cause

`KoinGraphTest` instantiates every ViewModel in the graph for real. Ten of them start a
`viewModelScope.launch` from their `init` block, which on JVM dispatches to a live Swing EDT thread
and queries an empty Room database, throwing `IllegalStateException`. Because these coroutines have
no exception handler, `kotlinx-coroutines-test`'s `ServiceLoader`-registered
`ExceptionCollectorAsService` catches them process-wide and attributes them to whichever `runTest`
is active when they land. The test that gets blamed is chosen by thread timing.

## Impact

- **Who:** every `./gradlew jvmTest` run — CI and local. Not branch-specific; `dev` is exposed.
- **How often:** non-deterministic. `dev` passes today by ordering luck; any classpath change,
  added test, or CI-machine timing difference can flip it.
- **How badly:** a red CI run naming an innocent, all-fakes test, with a Room stack trace pointing
  at code that test never calls. The cost is the debugging hour, and the worse outcome is the
  habit — a suite that fails randomly trains people to re-run rather than read.
- **Workaround:** re-run the build. Which is exactly the problem.

Both tests that landed in the last three commits are affected: this makes `KoinGraphTest`
(task 2) a liability for every test task that follows it in the improvement plan.

## Open questions

- **Are non-ViewModel beans also leaking?** Only ViewModel frames appeared in the traces observed,
  but `KoinGraphTest` constructs ~147 definitions and anything holding an application-scoped
  `CoroutineScope` could leak the same way. Not blocking — Option A below covers `Dispatchers.Main`
  regardless of the bean type, and an `applicationScope` leak would need separate handling if it
  ever appears.
- **Should ten ViewModels be doing I/O in `init` at all?** A real design question, out of scope
  here. Noted, not pursued.

## Options

### A. Install a non-executing `Main` dispatcher in `KoinGraphTest` — **recommended**

`Dispatchers.setMain(StandardTestDispatcher())` in `@BeforeTest`, `Dispatchers.resetMain()` in
`@AfterTest`. `coroutines-test`'s `MainDispatcherFactory` outranks the Swing one, so
`viewModelScope.launch` bodies are queued on a scheduler that is never advanced. They are never
executed, so nothing throws and nothing leaks.

- **Verified applicable:** `grep` finds **zero** occurrences of `viewModelScope.launch(<dispatcher>)`
  anywhere in `feature/` or `composeApp/` — every launch in the codebase is plain, so all of it goes
  through `Dispatchers.Main`. There is no ViewModel this misses.
- **Pros:** ~4 lines, confined to one test file. Deterministic. The graph check stays whole — all 147
  definitions are still resolved, and wiring is proven at construction time, which is unaffected.
- **Cons — real ones:** it makes `KoinGraphTest` silently dependent on every ViewModel launching on
  `Main`; the day someone writes `viewModelScope.launch(Dispatchers.IO)` in an `init`, the flake
  returns with no signal pointing here. Mitigate with a comment in the test stating the assumption.
  It also means `init` blocks genuinely never run under this test — acceptable, since Koin resolves
  all constructor arguments before invoking the constructor, so wiring is already proven by then.
- **Blast radius:** `KoinGraphTest` only. `setMain`/`resetMain` are per-JVM global, so the
  `@AfterTest` reset is not optional.

### B. Exclude `ViewModel` definitions from the check

Filter out anything assignable to `ViewModel` before resolving.

- **Pros:** trivially removes the leak.
- **Cons:** guts the test. ViewModels are the most wiring-fragile definitions in the graph — the
  most constructor parameters, the most cross-module dependencies — and the "no definition found"
  crashes `CLAUDE.md` documents are overwhelmingly ViewModel-shaped. This trades the test's main
  value for its convenience. **Recommend against.**

### C. Fork a JVM for `KoinGraphTest`

Isolate it via a separate Gradle test task or `forkEvery`.

- **Pros:** no change to the test's logic.
- **Cons:** does not fix the leak, only relocates it — the exceptions still fire, and if any other
  test ever shares that JVM the race returns. Adds a build-config concept and a second JVM start to
  every CI run. Solves a code problem with build configuration.

### D. Do nothing

- **Pros:** zero effort; `dev` is green today.
- **Cons:** a known race left in the suite, which will fail CI on an unrelated PR and cost someone
  the same hour this cost. **Recommend against**, but it is a legitimate choice if the priority is
  landing task 3 untouched.

**Recommendation: A.** It is the only option that removes the cause rather than the symptom, it is
small enough to review at a glance, and its one genuine weakness (an unstated assumption about
`Dispatchers.Main`) is fixable with a comment.

## Decision

**Option A, fixed immediately rather than deferred.** 2026-08-02.

Root cause and option recommended by Claude; gregory accepted A and delegated the timing
("no preference"). The argument for doing it before improvement-plan task 4 rather than queueing it:
every remaining task in that plan ends by running `./gradlew jvmTest` and confirming green, so a
race that reddens a random unrelated test attacks the verification step of each one. Task 5 in
particular adds `FakeSwimlanesRepository` to `:testing` — the same kind of classpath change that
triggered this. The likeliest cost of deferring was a future session mis-attributing the failure to
its own new test.

## What landed

`composeApp/src/jvmTest/.../KoinGraphTest.kt` only:

- `MainDispatcherRule(StandardTestDispatcher())` driven from `@BeforeTest` / `@AfterTest`. Reuses the
  existing `:testing` helper rather than calling `setMain` directly — note its default is
  `UnconfinedTestDispatcher`, which would **execute** the launches eagerly and make things worse;
  `StandardTestDispatcher` is load-bearing here, not a style choice.
- The "expected noise" KDoc paragraph replaced with one explaining the leak, the
  `ExceptionCollectorAsService` mechanism, and the undispatched-`launch` assumption the fix rests on.

**Verified:**

- `./gradlew jvmTest --rerun-tasks` × 3 — all BUILD SUCCESSFUL. Before the fix, two of three
  full-suite runs failed, each blaming a different test; a single green run would not have been
  evidence.
- `KoinGraphTest` still reports **147 definitions checked** — the graph check was not narrowed.
- Room-related construction failures in its output: **0** (was 3+ per run). The 14 remaining are the
  expected `MissingFieldException`s from `savedStateHandle.toRoute<T>()`.
- `./gradlew detekt` green.

**Deliberately not done:** the two open questions above stay open. No audit of non-ViewModel beans
for application-scoped leaks (none observed; the fix covers `Dispatchers.Main` regardless of bean
type), and no change to the ten ViewModels that do I/O in `init` — that is a design question, not
this defect.
