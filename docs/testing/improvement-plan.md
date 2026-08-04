# Testing suite: improvement plan

**Created:** 2026-08-02
**Baseline:** [survey.md](survey.md) — what the suite looked like before this plan started.

A sequence of small, independent tasks. Each one is sized to fit in a **single clean context**: a
session picks exactly one task, does it, runs `finalize`, and stops. Nothing here requires holding
two tasks in your head at once.

## How to run a task

1. Read the status table below and take the task marked **NEXT**. (If none is marked, take the
   first `todo`.) Never take a `deferred` task without asking.
2. Read only that task's section, plus [survey.md](survey.md) if you need the wider picture.
3. Do it. Verify with the task's own `Done when` commands — not by eyeballing.
4. **Update the status table**: set this task to `✅ done — <date>`, and move the `⬅ NEXT` marker
   to the task that follows. Add a `**Result (<date>):**` note to the task's own section saying
   what actually happened — especially anything that differed from the description. Append the note
   *above* the next task's `---` separator and re-read the boundary afterwards — a result note that
   swallows the following `## Task N` heading makes step 2 impossible to follow (this happened to
   task 4).
5. Run the **`finalize` skill**. Each task lists a *Finalize focus* — the thing most likely worth
   capturing — but that is a hint, not a substitute for the skill's own harvest step.
6. **Commit and push.** Standing authorization (gregory, 2026-08-03) — do not ask, and do not stop
   after finalize waiting to be told. Finalize runs *before* the commit so its CLAUDE.md/doc edits
   land alongside the work that taught them. Three limits: never commit a red build (step 3 is a
   precondition, not a parallel task), never push to `dev`, and ask before anything beyond
   commit+push — PRs, force-pushes, rebases.

Step 4 is what makes the next cold start work. A session that does the work and skips the table
leaves the plan lying about where things stand.

**Standing rule for every task:** if the task adds a fake, a model factory, or a test utility to
`:testing`, updating the **fake inventory in `.claude/agents/testing.md`** is part of the task, not
an optional extra. That agent is how future sessions discover what already exists; a stale inventory
causes duplicate fakes. This is an acceptance criterion, not a nice-to-have.

**Do not batch tasks.** The point of the split is that each one lands verified and finalized on its
own. If a task turns out to be bigger than described, split it further and update this file rather
than pushing through.

## Status

| # | Task | Size | Status |
|---|---|---|---|
| 0 | Correct the survey doc | XS | ✅ done — 2026-08-02 |
| 1 | Close the "tests that never run in CI" trap | S | ✅ done — 2026-08-02 |
| 2 | Koin DI graph test | M | ✅ done — 2026-08-02 |
| 3 | `WikiRepositoryImpl` + `FakeWikiApi` | S | ✅ done — 2026-08-02 |
| 4 | `GetProfileDataUseCase` | XS | ✅ done — 2026-08-03 |
| 5 | `GetKanbanDataUseCase` + `FakeSwimlanesRepository` | M | ✅ done — 2026-08-03 |
| 6 | `DateTimeUtilsImpl` + `KotlinxDateTimeFormatter` | M | ✅ done — 2026-08-03 |
| 7 | `TeamViewModel` | S | ✅ done — 2026-08-03 |
| 8 | Coverage floor in CI (`koverVerify`) | S | ✅ done — 2026-08-03 |
| 9 | Error-path convention + first sweep | M | ✅ done — 2026-08-03 |
| 9a | Missed-branch sweep, one module per session | M each | 🔁 in progress — `core/api` ✅ 2026-08-03, `feature/projects/data` + `mapper` ✅ 2026-08-03, `feature/kanban/ui` ✅ 2026-08-03, `utils/ui` ✅ 2026-08-03, `main` ⛔ closed-as-blocked 2026-08-03, `feature/workitem/ui/delegates/customfields` ✅ 2026-08-03, `feature/workitem/ui/delegates/badge` ✅ 2026-08-04, `feature/settings/ui/attributes/projectvalues` ✅ 2026-08-04, `feature/workitem/ui/screens/edittags` ✅ 2026-08-04, `feature/workitem/ui/screens/sprint` ✅ 2026-08-04, `feature/settings/ui/modules` ✅ 2026-08-04, `createtask` ✅ 2026-08-04, `feature/settings/ui/user` ✅ 2026-08-04, `feature/settings/ui` ⛔ closed-as-blocked 2026-08-04, `core/storage` ✅ 2026-08-04; ⬅ **NEXT** module: `feature/userstories/ui` (27 missed branches — `UserStoryDetailsViewModel`, LINE 263/528; verify and expect to split it, see the table below) |
| 9b | `WorkItemRemoteMediator` | M | todo |
| 10 | Compose UI test spike (one uikit widget) | M | ⛔ deferred — do not start |

**Scope decision (2026-08-02, extended 2026-08-03):** tasks 0–9 — the unit / non-instrumented work —
are in scope and should be worked straight through; 9a and 9b were added by task 9 as its own
follow-ups and inherit that scope. **Task 10 is deferred pending a decision**, along with
everything in [Considered and deferred](#considered-and-deferred). Do not start it without asking;
the other test *types* get decided once the unit-test work has landed.

Sizes: XS = minutes, S = under an hour, M = a focused session.

Tasks 0–2 are ordered deliberately: 0 makes the reference doc trustworthy, 1 guarantees that
anything later tasks write actually runs in CI, 2 is the highest-value single test in the plan.
Tasks 3–7 are independent of each other and can be reordered freely. Task 8 depends on 3–7 having
landed (the floor should be set from the improved numbers). Tasks 9, 9a and 10 are the open-ended
ones — 9a is explicitly repeatable, one module per session.

---

## Task 0 — Correct the survey doc

**Why:** three claims in [survey.md](survey.md) are already false, and it links twice to a file that
does not exist. Later tasks quote this doc; leaving it wrong propagates the errors.

**Scope:** `docs/testing/survey.md` only. No code.

**Corrections to make:**

- **Detekt is not commented out.** `.github/workflows/code_analysis.yml` runs `./gradlew detekt` as
  its first step (landed in `af9731df` / `b7e43b57`). Remove the "Issues found" item #3 and fix the
  CI section.
- **The orphaned mapper tests are fixed.** `ProjectMapperTest` and `TaskMapperTest` now live in
  `src/commonTest/kotlin/…` in `feature/projects/mapper` and `feature/tasks/mapper`. The only
  remaining `src/test/java` is `androidApp`'s, which is legitimate. Remove "Issues found" item #1
  and the 957/99 correction that follows from it — the executing suite is the full count.
- **Counts:** re-derive rather than trusting the doc. At the time of writing: 984 `@Test` across 101
  files.
- **`feature/wiki/data` gap row:** `WikiRepositoryImpl` is correctly listed. Leave it.
- **`feature/filters/ui` gap row is wrong.** That module contains no ViewModel — only
  `FilterModalBottomSheetWidget.kt` and `TaskFiltersWidget.kt`. It belongs under the "nothing tests
  Compose output" structural gap, not the untested-ViewModel row. `feature/teams/ui` *does* have a
  `TeamViewModel` and stays.
- **Dead links:** both references to `orphaned-mapper-tests.md` must go — the file was never
  committed.
- **`koin-test` location:** the doc says it is on the `:testing` classpath. Be precise — it is in
  the **`androidMain`** source set (`testing/build.gradle.kts:9-14`), which is why nothing common
  can reach it. Task 2 depends on this detail being right.
- Add a link to this plan.

**Done when:** `grep -rn "orphaned-mapper-tests\|commented out" docs/testing/survey.md` returns
nothing, and every remaining claim in the doc has been spot-checked against the repo.

**Finalize focus:** the process lesson — a survey doc went stale within a day because the fixes it
described landed immediately after. Worth a note about dating findings and re-verifying before
citing.

**Result (2026-08-02):** done. Root cause of the staleness identified: commit `af9731df` landed the
same evening the survey was written and fixed three of its findings at once (mapper-test move,
Detekt re-enable, Detekt KMP source-set wiring). All figures re-derived; coverage re-measured at
`21bcb6ad` (branch 47.9 %, line 67.4 % — within a point of the original, so the orphaned tests were
never the cause of the branch gap). Added a *Survey drift* section, corrected the
`feature/filters/ui` row (no ViewModel exists there), made the `koin-test`-is-in-`androidMain`
detail explicit for Task 2, and folded the CI Kover-aggregation trap into the CI section so Task 1
has a written rationale.

---

## Task 1 — Close the "tests that never run in CI" trap

**Why:** CI's only test execution is `./gradlew koverXmlReport`, which runs `jvmTest` **only for
Kover-aggregated modules**. `:testing`, `:uikit`, `:tools:seed`, `:tools:utils` and `:androidApp` are
excluded from aggregation in the root `build.gradle.kts`, so tests added there would silently never
run. That is the exact failure mode the orphaned mapper tests had — invisible, green, useless. No
tests are being lost *today*, which is precisely why this is cheap to fix now.

**Scope:**

- `.github/workflows/code_analysis.yml` — add a `./gradlew jvmTest` step before the Kover step.
- `androidApp/src/test/java/com/grappim/taigamobile/ExampleUnitTest.kt` — delete. It asserts
  `2 + 2 == 4` and is the sole inhabitant of the repo's only Android unit-test source set.

**Watch for:** deleting `ExampleUnitTest` may leave `androidApp` with an empty `src/test` tree and
unused test dependencies in `androidApp/build.gradle.kts`. Remove the dependencies **only** if they
become genuinely unused — check before deleting, and do not touch `:testing`'s `androidMain` JUnit4
deps, which Task 2 needs.

**Done when:** `./gradlew jvmTest` passes locally, the workflow has a dedicated test step, and
`find . -path "*/src/test/*" -name "*.kt" -not -path "*/build/*"` returns nothing.

**Finalize focus:** the Kover-aggregation trap itself belongs in `CLAUDE.md` under Testing — "a test
in a non-aggregated module does not run in CI" is a rule, not a one-off.

**Result (2026-08-02):** done. The workflow's single "Run tests and generate Kover XML report" step
was split into `./gradlew jvmTest` followed by `./gradlew koverXmlReport`, with a comment stating
why. The Kover step reuses the already-executed test results, so the split costs nothing.
`androidApp/src/test` was deleted entirely (`git rm -r`), removing `ExampleUnitTest`.

Verified `:testing:jvmTest` and `:uikit:jvmTest` are in the root `jvmTest` task graph (77 `jvmTest`
tasks total), so the previously-invisible modules are now covered.

Two decisions worth knowing:

- **The `testImplementation` deps stay.** They are not in `androidApp/build.gradle.kts` — they come
  from `configureKotlinAndroid()` in `build-logic/.../KotlinConfiguration.kt:82-83`, whose only
  caller is `AndroidApplicationConventionPlugin`. Leaving them means the next Android unit test
  someone writes needs no build change; removing them would edit shared convention code for no gain.
- **Residual gap: `tools/seed` and `tools/utils` are still uncovered.** They are `kotlin("jvm")`
  modules, so their test task is `test`, not `jvmTest`. Neither has tests today. Running root
  `./gradlew test` would drag in `androidApp`'s per-flavor Android unit-test variants, which is a
  much slower step for zero current benefit — so this was left alone deliberately. If a test is ever
  added under `tools/`, add `./gradlew :tools:seed:test` (or equivalent) to the workflow.

---

## Task 2 — Koin DI graph test

**Why:** the highest-value single test in this plan. `CLAUDE.md` documents "no definition found"
crashes as a recurring failure mode, and the DI graph is currently validated only by the app
actually starting on a device. `koin-test` and `koin-test-junit4` are already declared — but in
`testing/build.gradle.kts` **`androidMain`**, unreachable from `commonTest`/`jvmTest`, so they are
dead weight today. The `koin-expert` agent already refers to a `KoinGraphTest` that does not exist;
this task makes that reference true.

**Scope:**

- Make `koin-test` reachable from a JVM-executable source set. Prefer adding it to `:testing`'s
  `jvmMain` (or a `jvmTest` in `composeApp`) over moving the `androidMain` block — Android unit
  tests are disabled for Kover, so `androidMain` stays the wrong home regardless.
- Add a test that builds `AppModule` (`composeApp/src/commonMain/kotlin/com/grappim/taigamobile/di/Koin.kt`)
  and asserts every definition resolves.

**Approach notes — read before starting:**

- `checkModules()` is the koin-test entry point. Definitions taking runtime parameters
  (`SavedStateHandle`, injected params) need explicit providers in the check DSL; expect to spend
  most of the task on those, not on the wiring.
- Platform-specific beans (Android `Context`, iOS, JVM actuals) will not resolve on the JVM. Decide
  early whether to stub them or scope the check to platform-independent modules, and **write the
  decision into the test file as a comment** — the next person will hit the same wall.
- `compileSafety = false` is set via reflection in `KmpDiConventionPlugin`; the Koin compiler plugin
  is not doing this validation for us.
- Consult the **koin-expert** agent before improvising. It lives in `agentic-grappim`, symlinked
  into `~/.claude/agents/`.

**Done when:** the test fails if a `@Single`/`@Factory` binding is removed from a module's includes
list (verify by temporarily breaking one), and passes on a clean tree.

**Finalize focus:** high. Whatever the resolution story turns out to be for platform-specific beans
is exactly the kind of thing that costs an hour to rediscover — route it to `docs/koin/` and tell
the `koin-expert` agent the test now exists and how to run it.

**Result (2026-08-02):** done. `KoinGraphTest` lives in `composeApp/src/jvmTest/`; full write-up in
[docs/koin/koin-graph-test.md](../koin/koin-graph-test.md). 147 definitions checked in ~1.2 s, and
it runs in CI via the `jvmTest` step added in task 1. `koin-test` was added to composeApp's `jvmTest`
(with an explicit `platform(libs.koin.bom)` — the BOM applied by `KmpDiConventionPlugin` only reaches
`commonMain`). The `androidMain` `koin-test` block in `testing/build.gradle.kts` was left alone; it
is still dead weight, but removing it is not this task.

Four things differed from the task description:

- **No platform stubbing was needed at all.** The task expected to decide between stubbing platform
  beans and narrowing the check. JVM has a complete set of actuals — desktop calls
  `startKoin<KoinApp>` for real — so the JVM graph is a whole graph. Room, DataStore and Ktor all
  construct against real temp-dir / OkHttp implementations.
- **`checkModules()` was not usable.** It throws on the *first* failure of any kind, and ~14
  ViewModels throw `MissingFieldException` from `savedStateHandle.toRoute<T>()` against the blank
  `SavedStateHandle` the check must declare. The test walks `koin.instanceRegistry.instances`
  directly instead, failing only on `NoDefinitionFoundException` and reporting every one at once.
  This is sound because Koin resolves all constructor arguments before invoking the constructor.
- **The `Done when` criterion turned out to be unsatisfiable as written.** Removing a module from
  `AppModule.includes` changes nothing on JVM: `@ComponentScan("com.grappim.taigamobile")` on
  `AppModule` re-discovers the beans across module boundaries. Verified by deleting
  `UsersDataModule::class` — 147 definitions either way. The includes list is load-bearing only on
  iOS Native, the one platform this test cannot see. Verified the test instead by commenting out
  `@Single` on `UsersRepositoryImpl`: it failed and named all 27 affected consumers.
- **Added a `MIN_EXPECTED_DEFINITIONS = 147` floor**, asserted after the missing-binding report, as
  the backstop for a leaf definition disappearing with no consumer to notice.

**Follow-up (2026-08-02, during task 3):** this test shipped with a race that randomly failed
*unrelated* tests — the ViewModels it constructs launch real work from `init`, and the escaping
exceptions were attributed to whichever `runTest` was live. Fixed; see
[docs/issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md](../issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md).
The lesson generalises to any test that instantiates real ViewModels: **a full-suite `jvmTest` run is
part of a test task's verification, not just the module's own task** — this was invisible to
`:composeApp:jvmTest` run alone.

---

## Task 3 — `WikiRepositoryImpl` + `FakeWikiApi`

**Why:** the only repository implementation in the project without a test.

**Scope:** `feature/wiki/data/src/commonTest/…/WikiRepositoryImplTest.kt`, plus a new `FakeWikiApi`
in `:testing`.

**What the SUT does** (`feature/wiki/data/src/commonMain/…/WikiRepositoryImpl.kt`): seven suspend
methods over `WikiApi`, `TaigaSessionStorage`, `WikiPageMapper`, `WikiLinkMapper`. The behaviour
worth asserting is that each method (a) reads `getCurrentProjectId()` from the session storage and
passes it through, (b) passes the right request body — `NewWikiLinkRequestDTO` and
`CreateWikiPageRequestDTO` are built inside the repository, so assert their fields — and (c) maps
the response through the right mapper.

**Existing pieces:** `FakeTaigaSessionStorage` and `FakeWikiRepository` already exist. `FakeWikiApi`
does **not** — create it, following the shape of the other 12 fake APIs.

**Done when:** `./gradlew :feature:wiki:data:jvmTest` is green, all seven methods covered, and
`.claude/agents/testing.md` lists `FakeWikiApi`.

**Finalize focus:** low unless creating the fake surfaced something new about the fake-API pattern.

**Result (2026-08-02):** done. 17 tests in `WikiRepositoryImplTest`, `:feature:wiki:data:jvmTest`
green, `detekt` and `KoinGraphTest` still green.

One thing the task description did not anticipate: **`WikiApi` was a concrete final class**, so it
could not be faked. Every other API in the repo (12 of them) is already `interface XApi` +
`@Single(binds = [XApi::class]) class XApiImpl` — `WikiApi` was the last hold-out. It was split to
match, which is a production change but the strictly conventional one; `WikiApi` is referenced
nowhere outside `feature/wiki/data`, and the Koin definition count is unchanged (`KoinGraphTest`
still sees 147). **If a future task needs to fake an API, check first whether it is an interface.**

Also added, beyond the stated scope: `WikiFakes.kt` (`getWikiPageDTO`, `getWikiLinkDTO`) in
`:testing/models`, because the DTO builders were previously duplicated as private helpers inside
`WikiPageMapperTest` and `WikiLinkMapperTest`. Those two tests were left alone (surgical-changes
rule); they can adopt the shared factories whenever they are next touched. `:testing` gained
`api(projects.feature.wiki.data)`.

`FakeWikiApi` carries an `errorToThrow` hook and per-method call recorders, so all seven methods have
both a happy path and a propagate-the-error test — this is the Task 9 convention applied early, on a
file small enough that it cost nothing.

---

## Task 4 — `GetProfileDataUseCase`

**Why:** smallest remaining gap; a good warm-up task.

**Scope:** `feature/profile/domain/src/commonTest/…/GetProfileDataUseCaseTest.kt`.

**What the SUT does:** wraps three parallel `async` calls (`getUser`, `getUserStats`,
`getUserProjects`) in `resultOf`, returning `Result<ProfileData>`. Two behaviours matter: the
success path assembles `ProfileData` from all three, and a throw from **any one** of the three
surfaces as `Result.failure` — that second one is the branch the suite habitually misses.

**Existing pieces:** `FakeUsersRepository` and `FakeProjectsRepository` both exist. No new fakes
needed; this task should not touch `:testing` at all.

**Done when:** `./gradlew :feature:profile:domain:jvmTest` is green with both success and
per-dependency failure cases.

**Finalize focus:** low. Likely an empty finalize — that is a valid outcome, say so and stop.

**Result (2026-08-03):** done. 5 tests in `GetProfileDataUseCaseTest` — success assembly, the
empty-projects case, and one failure test per dependency (`getUser`, `getUserStats`,
`getUserProjects`). `:feature:profile:domain:jvmTest`, the full `jvmTest` and `detekt` are all green.
`:testing` was untouched as the task specified; both fakes already carried the `…Throws` hooks.

Also fixed while here: this section had **no `## Task 4 —` heading at all** — Task 3's result ran
straight into Task 4's `**Why:**`, so "read only that task's section" was impossible to follow.

One thing worth carrying forward, and the reason this XS task was not a no-op finalize:
**`assertEquals(testException, result.exceptionOrNull())` does not hold when the throw happens inside
an `async` child.** On JVM, kotlinx-coroutines' stack-trace recovery rethrows a *copy* of the
exception with the original as its `cause`, so identity and equality both fail while the failure
message reads confusingly as `expected: …<IllegalStateException: error> but was:
…<IllegalStateException: error>`. Assert by type + message instead (`assertIs<T>` +
`assertEquals(testException.message, …)`) — that is also the portable choice, since Native does not
do the recovery. `assertTrue(result.isFailure)`, which the dashboard use-case tests use, sidesteps
the issue but proves less.

---

## Task 5 — `GetKanbanDataUseCase` + `FakeSwimlanesRepository`

**Why:** the most logic-dense untested unit in the repo, and the only one on this list where the
tests are likely to find a real bug.

**Scope:** `feature/kanban/domain/src/commonTest/…/GetKanbanDataUseCaseTest.kt`, plus a new
`FakeSwimlanesRepository` in `:testing`.

**What the SUT does** (`GetKanbanDataUseCaseImpl`): four parallel `async` repository calls plus a
sequential swimlanes fetch, then three pieces of real logic worth testing independently:

- `buildSwimlanesWithUnclassified` — prepends `Swimlane.unclassified()` **only** when raw swimlanes
  are non-empty *and* at least one story has `swimlane == null`. Three branches.
- default swimlane selection — `storageSwimlane` wins if it matches; otherwise the project's
  `defaultSwimlane`; otherwise `firstOrNull()`. Four paths, including both fallbacks.
- `computeStoriesByStatus` / `filterStoriesBySwimlane` — null swimlane means all stories,
  `isUnclassified` means stories with `swimlane == null`, otherwise match by id. Also asserts
  assignee resolution drops unknown user ids (`mapNotNull`).

`computeStoriesByStatus` is `public` on the interface and pure — test it directly rather than only
through `getData()`.

**Existing pieces:** `FakeUsersRepository`, `FakeFiltersRepository`, `FakeUserStoriesRepository`,
`FakeProjectsRepository` and `SwimlaneFakes.kt` all exist. **`FakeSwimlanesRepository` does not** —
only `FakeSwimlanesApi`. Create it.

**Note:** `computeStoriesByStatus` uses `withContext(Dispatchers.Default)`. `MainDispatcherRule`
does not cover that; the test must not assume it runs on the test scheduler.

**Done when:** `./gradlew :feature:kanban:domain:jvmTest` is green with every branch above covered,
and `.claude/agents/testing.md` lists `FakeSwimlanesRepository`.

**Finalize focus:** high if the tests find a behaviour bug — that goes to `docs/issues/` via the
`investigate-issue` skill, not into a silent fix. `docs/architecture/kanban-filters.md` already
covers the three swimlane modes; check it against what the tests prove and correct it if they
disagree.

**Result (2026-08-03):** done. 21 tests in `GetKanbanDataUseCaseTest`, covering all three
`buildSwimlanesWithUnclassified` branches, all four default-swimlane paths plus the no-swimlanes
case, `getData` assembly (kanban-order sort, permission flags, pass-through of statuses/members,
grouping through the selected swimlane), a failure test per repository (5), and
`computeStoriesByStatus` driven directly for the three swimlane modes, status keying and assignee
resolution. `:feature:kanban:domain:jvmTest`, the full `jvmTest` and `detekt` are all green.

**No behaviour bug was found** — the task expected one. `docs/architecture/kanban-filters.md` matches
what the tests prove; no correction needed. The one real problem found is an efficiency issue, filed
as [revisit #6](../revisit.md#6-getkanbandatausecase-reads-the-current-project-three-times): `getData`
reads the current project three times (once as `async`, twice more via `getPermissions()`, which is
just `getCurrentProjectSimple().myPermissions`).

Two things differed from the task description:

- **The model factories did not need widening.** `getUserStory()` takes only `id`/`version`, but
  `UserStory` and `Swimlane` are data classes, so the test uses `.copy(swimlane = …, kanbanOrder = …,
  status = …, assignedUserIds = …)` via a local `story(...)` helper — the same style
  `KanbanViewModelTest` already uses. Keeping `:testing`'s factories untouched was the smaller diff.
- **Three fakes needed hooks, not just the one new fake.** Besides `FakeSwimlanesRepository`,
  `FakeUserStoriesRepository.getUserStories`, `FakeProjectsRepository.getCurrentProjectSimple`
  (both were `error("not used in this test")`) and `FakeFiltersRepository.getStatuses` (no throw
  hook) had to gain result/throws fields. **Budget for this on any use-case task**: a use case fans
  out across 4-5 repositories, and the odds that every one of them is already faked to the depth you
  need are low.

The `withContext(Dispatchers.Default)` warning in the task turned out to be a non-issue: `runTest`
awaits the suspend call on the real dispatcher without any special handling.

---

## Task 6 — `DateTimeUtilsImpl` + `KotlinxDateTimeFormatter`

**Why:** date formatting is used across every screen and has three platform actuals that nothing
verifies.

**Scope:** `utils/formatter/datetime/src/commonTest/…`. Possibly `jvmTest` too — see below.

**What the SUT is:** `DateTimeUtilsImpl` and `KotlinxDateTimeFormatter` in `commonMain`, with
`KotlinxDateTimeFormatter.jvm.kt`, `.ios.kt` and `PlatformDateTimeFormat.android.kt` actuals.

**The real decision in this task:** platform actuals cannot be covered by JVM tests. Test the
`commonMain` logic in `commonTest`, and put anything that asserts JVM-specific formatting in
`jvmTest` — following the precedent already set by `core/api` and `core/storage`, which are the only
modules using `jvmTest` today. Do not try to make one test cover all platforms; **write down in the
test file which platform's behaviour is actually being asserted.**

**Watch for:** locale and timezone dependence. A formatting test that passes only in one TZ is worse
than no test. Pin explicitly rather than relying on the machine default.

**Done when:** `./gradlew :utils:formatter:datetime:jvmTest` is green and passes under a different
`TZ` (`TZ=Asia/Tokyo ./gradlew :utils:formatter:datetime:jvmTest --rerun-tasks`).

**Finalize focus:** the platform-actual testing decision generalises — it is the same question Task
2 hits and the same one any future `expect/actual` test will hit. Route it to `CLAUDE.md` or the
`testing` agent, not just to this plan.

**Result (2026-08-03):** done. 29 tests across three files —
`DateTimeUtilsImplTest` (16) and `KotlinxDateTimeFormatterTest` (7) in `commonTest`,
`KotlinxDateTimeFormatterJvmTest` (6) in `jvmTest`. `:utils:formatter:datetime:jvmTest`, the full
`jvmTest` and `detekt` are all green, and the module's tests also pass under
`TZ=Asia/Tokyo … --rerun-tasks`. No new fakes, so `:testing` and `.claude/agents/testing.md` were
untouched. No bug found — the code is correct as written.

**The split that resolved the platform question:** `DateTimeUtilsImpl`'s epoch/ISO half is pure
`commonMain`, so it is tested in `commonTest`; the medium-format half is only a delegation to the
`expect` functions, and `commonTest` asserts nothing more than *that the delegation happens*
(`sut.formatToMediumFormat(d) == KotlinxDateTimeFormatter().formatMediumDate(d)`). Everything about
what the medium format actually *looks* like lives in `jvmTest`, whose KDoc states plainly that it
covers the JVM actual, covers Android's by proxy (byte-for-byte the same `java.time` code) and
**does not cover iOS's `NSDateFormatter` actual at all**.

Two things worth carrying forward:

- **TZ-independence is asserted by hard-coded UTC epoch constants, not by manipulating the zone.**
  `retrieveEpochMillisAtStartOfDay(2024-01-15) == 1705276800000` only holds if the SUT uses
  `TimeZone.UTC`; run the suite under any `TZ` and it stays true. Two `fromMillisToLocalDate` cases
  pick instants near the ends of the UTC day (23:30Z and 00:30Z) so that a switch to the system zone
  would land on a different calendar date in Tokyo / Honolulu respectively. That is cheaper and more
  honest than `TimeZone.setDefault`.
- **Locale is the harder axis, and it cannot be pinned from a test.** The JVM/Android actuals hold
  `DateTimeFormatter.ofLocalizedDate(MEDIUM)` in a *private top-level `val`*, which bakes in
  `Locale.getDefault(FORMAT)` at class-initialisation time — before any `@BeforeTest` can run, and
  in an order no test controls. So the JVM test asserts only locale-independent properties (day,
  month and year each change the output; time-of-day never does; the default zone never does) plus
  equality with a JDK formatter built from the *same* default locale. Those properties were verified
  by hand across en-US, de-DE, ja-JP, sv-SE, th-TH, ar-EG and hu-HU before being relied on.

Note for anyone trying to vary the locale of a Gradle test run: **`LANG` / `LC_ALL` /
`JAVA_TOOL_OPTIONS` / `-Dorg.gradle.jvmargs` all failed to move it** on this machine — the workers
stayed `en_US` even with a fresh daemon. `TZ`, by contrast, propagates to the forked test JVM
correctly (verified with a throwaway probe test). Do not assume a locale override took effect
without probing it.

---

## Task 7 — `TeamViewModel`

**Why:** the last untested ViewModel; every sibling `ui` module has one.

**Scope:** `feature/teams/ui/src/commonTest/…/TeamViewModelTest.kt`.

**Existing pieces:** follow the house shape exactly — `sut` field, fakes as plain fields,
`createViewModel()` helper, `MainDispatcherRule` driven from `@BeforeTest`/`@AfterTest`, backtick
names, `internal class`. `WikiCreatePageViewModelTest` is the canonical example.

**Done when:** `./gradlew :feature:teams:ui:jvmTest` is green, covering initial state, successful
load, and the error path.

**Finalize focus:** low.

**Result (2026-08-03):** done. 7 tests in `TeamViewModelTest` — initial state, the init-load success
and failure paths, the `generateMemberStats = true` argument, and three refresh cases (reload,
error-cleared-by-a-later-success, failure-keeps-previously-loaded-members).
`:feature:teams:ui:jvmTest`, the full `jvmTest` and `detekt` are all green. No build-file change was
needed — the convention plugin already puts `:testing` on every module's `commonTest` classpath.

`FakeUsersRepository` gained `getTeamMembersCallCount` and `getTeamMembersGenerateMemberStats`
recorders (`.claude/agents/testing.md` updated); nothing else in `:testing` was touched.

**The task's premise was wrong: `TeamViewModel` was not "the last untested ViewModel" — 12 others
have no test.** The original survey walked only the modules it had already flagged rather than
enumerating every `*ViewModel.kt`. The full list, with the one-liner that re-derives it, is now in
[survey.md](survey.md#gaps); 7 of the 12 are in `feature/settings/ui`. **No task in this
plan covers them** — that is an open decision, not an oversight to fix inside task 8.

Two notes for anyone testing an init-loading ViewModel:

- `MainDispatcherRule` uses `UnconfinedTestDispatcher`, so the `init` block's `viewModelScope.launch`
  has already completed by the time `createViewModel()` returns. There is no way to observe
  `isLoading = true` from the outside, and an "initial state" test really asserts the *post-load*
  state. The test file says so in a comment rather than implying otherwise.
- Unlike most ViewModels here, `TeamViewModel` has no `SnackbarDelegate` — the failure path only
  writes `error` into state, so no turbine `snackBarMessage.test { }` dance is needed to let the
  coroutine finish (contrast `WikiPagesViewModelTest`).

---

## Task 8 — Coverage floor in CI (`koverVerify`)

**Do not start this before tasks 3–7 have landed** — the floor should be set from the improved
numbers, otherwise it locks in the old ones.

**Why:** coverage is measured and uploaded to Codecov but **nothing enforces it**. A PR can drop
branch coverage freely and CI stays green. A floor makes the number directional instead of
decorative.

**Scope:** root `build.gradle.kts` `kover { }` block, and a `koverVerify` step in
`.github/workflows/code_analysis.yml`.

**How to pick the numbers:** run `./gradlew koverXmlReport` on a clean tree *after* tasks 3–7, read
the actual figures, and set the bound a couple of points **below** them. A floor that fails on the
day it lands teaches people to bypass it.

Baseline measured at `21bcb6ad` on 2026-08-02, before any of tasks 3–7: **line 67.4 %, branch
47.9 %, instruction 63.7 %, method 50.6 %, class 61.3 %.** The report XML has no summary line, so
read the totals with:

```bash
./gradlew koverXmlReport
python3 -c "
import xml.etree.ElementTree as ET
for c in ET.parse('build/reports/kover/report.xml').getroot().findall('counter'):
    cov, mis = int(c.get('covered')), int(c.get('missed'))
    print(f\"{c.get('type'):12} {cov:6} / {cov+mis:6}  {100*cov/(cov+mis):.1f}%\")"
```

Only root-level `<counter>` elements are totals; the file also contains per-package and per-class
counters, so do not grep it blindly.

**Set a branch bound, not only a line bound.** The whole point is the 20-point line-vs-branch gap;
a line-only floor would not notice it.

**Done when:** `./gradlew koverVerify` passes on `dev`, fails when the bound is temporarily raised
above actual, and the workflow runs it.

**Finalize focus:** record the chosen numbers and the ratcheting intent in `CLAUDE.md` — otherwise
the next person to hit the gate will just lower it.

**Result (2026-08-03):** done. Two named bounds — `Line coverage` ≥ 58 and `Branch coverage` ≥ 38 —
in `total { verify { } }` in the root `build.gradle.kts`, plus a `./gradlew :koverVerify` step in
`code_analysis.yml` placed **after** the Codecov upload so a breached floor still publishes the
report that explains it. `:koverVerify` is qualified because the bare task name also runs the
rule-less `koverVerify` of all 77 modules. Verified both ways: green on a clean tree, and red with
both rules named when the bounds were temporarily set to 63/43. `detekt`, `ktlintCheck` and the full
`jvmTest koverXmlReport :koverVerify` sequence are green.

**The task's premise about the numbers was wrong, in two separate ways.**

*First*, coverage appeared to have **dropped** since the recorded `21bcb6ad` baseline — line
67.4 % → 65.3 %, branch 47.9 % → 45.9 % — despite tasks 3–7 adding ~100 tests. It had not. Covered
counts rose across every counter; the *denominator* grew by 1147 lines while production code changed
by +22 (the `WikiApi` interface split). Subtracting the four packages listed below reproduces the
baseline denominators to within 78 lines on LINE and 2 on BRANCH, and shows the real improvement:
**line 67.4 % → 71.9 %, branch 47.9 % → 49.7 %.** Do not read a Kover delta without checking whether
the denominator moved.

*Second*, `koverXmlReport` and `koverVerify` **do not agree**. In one invocation, over identical
artifacts: XML 65.30 % / 45.88 %, verify 60.47 % / 40.29 %. With all filters removed they agree to
four decimal places, so the divergence is entirely in how each applies the `excludes` block — and
neither applies it in full (a faithful application gives 71.97 % / 49.73 %). Several exclusion
entries are silent no-ops, all of them in `:core:storage`; full evidence in
[revisit #8](../revisit.md#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify).

The bounds are therefore set from **`:koverVerify`'s own numbers, not the XML's** — the gate has to
be tuned to the task that enforces it. This is safe: fixing the excludes can only raise coverage.
**Anyone tuning these bounds must read them off `./gradlew :koverVerify`**; the Codecov figure is a
different number and will mislead by ~5 points.

The measurement snippet in this section still works, but it reports the *XML* figures — treat it as
the Codecov number, not the gate number.

---

## Task 9 — Error-path convention + first sweep

**Why:** the single biggest quality signal in the survey. Line coverage is 67 % but branch coverage
is 47 % — the suite exercises happy paths through functions without covering their conditionals.
That 20-point gap **is** the error-handling gap.

**Scope — do the convention first, then one module, then stop:**

1. Write the convention down: every repository-impl and ViewModel test file gets at least one test
   where the collaborating fake throws `testException`. Add it to `.claude/agents/testing.md` and
   `CLAUDE.md`'s Testing section.
2. Apply it to **one** module as a proof — `feature/workitem/data` (73 tests, 3 files) is the
   highest-leverage target.
3. Measure the branch-coverage delta for that module and write it into this section.

**Explicitly out of scope:** sweeping all 44 modules. If the proof works, add follow-up tasks 9a,
9b, … to this file rather than doing them here. This task is the pattern, not the sweep.

**Done when:** the convention is documented, `feature/workitem/data` has failure-path coverage for
every public method, and the measured before/after branch numbers are recorded above.

**Finalize focus:** high — this task's output is mostly a convention, and conventions only survive
if they land in `CLAUDE.md` and the `testing` agent.

**Result (2026-08-03):** done. The convention is in `CLAUDE.md` (Testing) and in
`.claude/agents/testing.md` as a required *Failure-path convention* section. `feature/workitem/data`
gained 22 tests (`WorkItemRepositoryImplTest` 25 → 47) — one failure test per public method of
`WorkItemRepositoryImpl` plus seven covering `getWorkItems`' offline / cache-fallback branch. The
full `jvmTest`, `detekt`, `ktlintCheck` and `:koverVerify` are all green.

**Measured branch delta**, `koverXmlReport`, identical denominators before and after (so this is a
real move, not the denominator trap from task 8):

| Scope | Before | After |
|---|---|---|
| `WorkItemRepositoryImpl` BRANCH | 11/24 — 45.8 % | **24/24 — 100 %** |
| `WorkItemRepositoryImpl` LINE | 101/113 — 89.4 % | 112/113 — 99.1 % |
| package `feature/workitem/data` BRANCH | 34/63 — 54.0 % | **47/63 — 74.6 %** |
| package `feature/workitem/data` LINE | 220/267 — 82.4 % | 231/267 — 86.5 % |

The residual 16 package branches are `WorkItemRemoteMediator` (0/11, no test at all) and
`WorkItemEntityMapper` (19/24) — neither is a failure-path gap; the mediator becomes task 9b.

Three things worth carrying forward:

- **A bare `assertFailsWith<IllegalStateException>` is not a valid failure-path assertion here.**
  `testException` is an `IllegalStateException`, and so is every fake's own `error("… not set")`
  guard — so the test passes when the fake bailed out before the SUT reached the code under test.
  Added `assertFailsWithTestException` to `:testing` `TestUtils.kt`, which matches type **and
  message**; the message match also handles the async-copy problem task 4 found. The existing
  `WikiRepositoryImplTest` failure tests use the bare form and are exposed to this — not changed
  here (surgical-changes rule), but worth fixing when that file is next touched.
- **`kotlin("test")` is now `api(...)` in `:testing`'s `commonMain`**, which is what let the helper
  live in `:testing` at all. `:testing` is referenced only from `commonTest` (by the convention
  plugin — no build file names it), so this cannot reach a production classpath.
- **The 20-point line-vs-branch gap is not all missing tests.** `feature/filters/domain/model` shows
  144 branches for nine files of `@Serializable data class` — `FiltersData` alone is 110 branches in
  93 lines with no hand-written conditional in it. Compiler-generated `equals`/`hashCode`/
  `copy$default`/serializer branches are a large share of the denominator, and no test will ever
  take them. Rank sweep targets by *missed branches in hand-written code*, not by package percentage.

---

## Task 9a — Missed-branch sweep, one module per session

**Depends on:** task 9's convention. Apply it where it fits, do not re-derive it.

**This task was originally called "error-path sweep: the rest of the repository/use-case layer", and
all four modules done so far have falsified that name.** `core/api` was Ktor plugins, `feature/kanban/ui`
was a ViewModel whose failure paths were *already* tested, `feature/projects/data` had a repository
with no happy-path test at all, and `utils/ui` has no collaborator that can throw. The ranking below
is by missed branches, which finds whatever is untested — not specifically error paths and not
specifically repositories. **Read the module and decide what it actually needs before scoping the
session**; "add a `testException` test per public method" is one possible answer, not the brief.

**Verify the row before you take it — including the `⬅ next` marker — and be willing to close it
instead of working it.** The row's number is a package total; it says nothing about whether those
branches are *reachable* from a JVM test. Get the per-class breakdown out of the report first (the
snippet is in the `…delegates/customfields` section) and check for `@Composable`. `main` was marked
NEXT on a 4/35 and turned out to be 31 branches of composition-blocked code over an already-fully-
covered ViewModel; it was closed with the evidence rather than worked. Two rows' Note columns were
also simply wrong when re-derived. A row that survives verification is the session; a row that does
not gets a ⛔ entry recording *why*, which is worth as much as tests.

**Verify in both directions — a row can be *under*stated too.** `core/storage` was written down as
0/18 across three named classes; the same package tree also held `AuthStorageImpl$isLoggedIn$1` (0/6)
and `DataStoreServerStorage` (2/8), both hand-written and equally reachable, making the real figure
2/32. Re-derive the per-class breakdown for the whole package prefix rather than reading the row's
class list as complete.

**Why:** task 9 proved the pattern on one module. These are the modules where a session buys the
most, ranked by missed branches in **hand-written** code (measured 2026-08-03, `koverXmlReport`):

**Re-derived 2026-08-03** after the `feature/kanban/ui` module, with the root `excludes` block
applied by [kover-rank.py](kover-rank.py) rather than trusting the report's own filtering:

| Module | BRANCH | Note |
|---|---|---|
| ~~`core/api` + `core/api/errors`~~ | 73/126 | ✅ done 2026-08-03 |
| ~~`feature/projects/data` + `feature/projects/mapper`~~ | 8/66 | ✅ done 2026-08-03 — now 66/66 |
| ~~`feature/kanban/ui`~~ | 65/166 | ✅ done 2026-08-03 — now 159/166 |
| ~~`utils/ui`~~ | 8/107 | ✅ done 2026-08-03 — now 61/107; the residual 46 all need a composition |
| ~~`main`~~ | 4/35 | ⛔ closed-as-blocked 2026-08-03 — 31 of the 35 need a composition, `MainViewModel` is already 4/4 + 44/44 |
| ~~`feature/workitem/ui/delegates/customfields`~~ | 0/30 | ✅ done 2026-08-03 — now 28/30, LINE 86/86; the residual 2 are unreachable |
| ~~`feature/workitem/ui/delegates/badge`~~ | 0/22 | ✅ done 2026-08-04 — now **22/22, LINE 66/66** |
| ~~`feature/settings/ui/attributes/projectvalues`~~ | 0/22 | ✅ done 2026-08-04 — now **22/22**, LINE 117/125 |
| ~~`feature/workitem/ui/screens/edittags`~~ | 0/20 | ✅ done 2026-08-04 — now **20/20**, LINE 124/127 |
| ~~`feature/workitem/ui/screens/sprint`~~ | 0/18 | ✅ done 2026-08-04 — now **18/18**, LINE 74/75 |
| ~~`feature/settings/ui/modules`~~ | 0/16 | ✅ done 2026-08-04 — now **14/16**, LINE 88/88; the residual 2 are unreachable |
| ~~`createtask`~~ | 0/13 | ✅ done 2026-08-04 — now **13/13**, LINE 96/97 |
| ~~`feature/settings/ui/user`~~ | 0/12 | ✅ done 2026-08-04 — the 4 reachable branches are now **4/4**, LINE 16/17; the other 8 are `@Composable` and stay 0 |
| ~~`feature/settings/ui`~~ | 0/8 | ⛔ closed-as-blocked 2026-08-04 — all 8 are `ThemeSelectorKt`, a `@Composable`. See the section below |
| ~~`core/storage` + `/auth` + `/server`~~ | 2/32 | ✅ done 2026-08-04 — now **45/46**; the row said 0/18 and was an undercount, see the section below |
| **`feature/userstories/ui`** | 13/40 | ⬅ next. `UserStoryDetailsViewModel`, LINE 263/528 with a large block of wholly-untested lambdas — likely needs splitting |
| ~~`core/api/errors`~~ | 47/76 | ⛔ do not take. 25 of the 29 missed are `TaigaErrorResponse`, a `@Serializable data class` at LINE 5/5; the two real classes are 10/12 and 28/30 |

**Re-derived 2026-08-04** after the `core/storage` module, with [kover-rank.py](kover-rank.py) over a
clean 742-class report. The candidates below `feature/userstories/ui`, for whoever takes the session
after it — none verified yet, so check each for `@Composable` and generated code before scoping:

| Module | BRANCH | Note |
|---|---|---|
| `feature/epics/ui/details` | 12/30 | LINE 245/468 — a details ViewModel, same shape as `feature/userstories/ui` |
| `feature/issues/ui/details` | 17/34 | LINE 285/512 — likewise |
| `core/domain` | 20/36 | LINE 43/53, small and hand-written |
| `feature/workitem/ui/mappers` | 41/56 | **LINE 105/105 already** — pure mappers whose conditionals are untested, so this is branch-only work and cheap |
| `feature/sprint/data` | 16/29 | LINE 147/205 |
| `feature/tasks/ui` | 17/30 | LINE 274/479 |

Skip `feature/filters/domain/model` (142 missed) and `feature/userstories/dto` (38) — generated
`data class` / `@Serializable` branches, unreachable from a test. `feature/login/ui` has dropped off
the list: its old 27/126 row was `LoginScreen`, and the ViewModel alone is 27/44.

**Use [kover-rank.py](kover-rank.py) to re-derive this table; do not read it off the raw report.**
`koverXmlReport` flips unpredictably between applying the `excludes` block and ignoring it (class
counts 742 / 821 / 854 observed), and a report from the wrong side counts thousands of branches no
test can move. The script re-applies the exclusion rules itself, so it does not matter which side of
the flip you got — on 2026-08-03 it turned an 854-class report into 742 classes and reproduced a
genuine 742-class run's totals to the digit. **This supersedes the earlier advice to keep re-running
until a 742 appears**; there is still no known way to force one, and now there is no need to.

**Do one module per session**, recording the same before/after table task 9 recorded. Do **not**
take the packages that top the raw missed-branch list — `core/storage/db/dao` (0/180) and
`feature/filters/domain/model` (2/144) are Room-generated and data-class-generated branches
respectively, and are unreachable from a test.

### `core/api` — ✅ done 2026-08-03

55 tests across 8 new files, plus `CoreApiFakes.kt`. `:core:api:jvmTest` goes 20 → 75 tests; the full
`jvmTest`, `detekt`, `ktlintCheck` and `:koverVerify` are all green.

| File | Tests | Covers |
|---|---|---|
| `TokenRefreshPluginTest` | 7 | every branch of the 401/refresh/retry path, incl. both double-check paths and the refresh-throws fallback |
| `ErrorMappingPluginTest` | 8 | pass-through, status→code mapping, project-limit headers (both / one / unparseable), rethrow vs. map |
| `TokenRefresherImplTest` | 3 | request shape + response mapping, failure propagation, `logout()` through a real `AuthStateManager` |
| `TryCatchExtensionsTest` | 4 | success, catch, and both rethrow clauses |
| `DebugLocalhostPluginTest` | 3 | all three host-rewrite branches |
| `AuthHeaderPluginTest` | 3 | user-agent, token present / absent |
| `HostSelectionPluginTest` | 2 | rewrite and no-op |
| `ResponseExtensionsTest` | 2 | `hasNextPage` both ways |

**The measured coverage delta is almost nil, and that is the headline finding.** Taken between two
runs with identical denominators (see the comparability trap below):

| Scope | Before | After |
|---|---|---|
| package `core/api` LINE | 55/74 — 74.3 % | 63/74 — 85.1 % |
| package `core/api` BRANCH | 26/50 — 52.0 % | 28/50 — 56.0 % |
| package `core/api/errors` LINE | 52/54 | **52/54 — unchanged** |
| package `core/api/errors` BRANCH | 47/76 | **47/76 — unchanged** |

**Why:** the root `kover` `excludes` block drops `**.*Plugin` and `**.*Module` as "architecture
boilerplate", which in this module means all five Ktor plugins — ~98 lines and 38 branches of real
auth / error-mapping / host-rewriting logic, previously at 0 % and now fully covered, none of it in
the report. `core/api/errors` does not move at all because `ErrorMappingPlugin` is entirely excluded.
Filed as [revisit #10](../revisit.md#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi)
with the per-class figures; narrowing the patterns has to happen together with revisit #8, so it was
not done here.

**Three things to know before taking the next module:**

- **`ktor-client-mock` is now in the catalog** (`ktor-client-mock`), wired into `core/api`'s
  `commonTest`. Any Ktor plugin, or anything needing a real `HttpResponse`, is testable through
  `MockEngine`: install the plugin on a `HttpClient(MockEngine { … })` and read the *final*
  `HttpRequestData` from the engine lambda to see what the plugin rewrote. `HttpSend.intercept` is
  not reachable any other way.
- **`koverXmlReport` flips between two class universes and the trigger is unknown** — 821 classes /
  62.00 % here, 742 / 71.96 % after a build-script edit, because the `excludes` are only applied in
  full in the second. A baseline and an after-run on opposite sides are *not* comparable; this cost
  most of a session. Full reproduction in
  [revisit #8](../revisit.md#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify).
  ⚠️ The rule this note originally gave — "take both measurements with a build-file change present" —
  **was disproved** by the `feature/projects/data` session below, which saw a third value (854) with
  no build file touched. Print the class count and compare package denominators instead.
- **The fakes are local to the module, not in `:testing`.** `CoreApiFakes.kt` holds
  `FakeAppInfoProvider`, `FakeBaseUrlProvider` and `FakeTokenRefresher`; hosting them in `:testing`
  would make it depend on `:core:api`, which everything else already depends on. `.claude/agents/testing.md`
  was therefore not touched. Name the backing property `xToReturn`, not `x` — a `var versionName`
  behind `getVersionName()` is a JVM signature clash.

Two behaviour findings came out of it, both deferred:
[revisit #11](../revisit.md#11-tokenrefreshplugins-max_retries-guard-is-unreachable) (the plugin's
retry cap can never fire, because `execute()` does not re-enter its own interceptor) and
[revisit #12](../revisit.md#12-two-small-dead-spots-in-coreapi).

### `feature/projects/data` + `feature/projects/mapper` — ✅ done 2026-08-03

39 new tests: `ProjectsRepositoryImplTest` 12 → 36, a new `ProjectValuesRepositoryImplTest` (14), and
one test in `ProjectMapperTest`. `:testing` gained `FakeProjectValuesApi` and `getProjectValueItemDTO()`;
`FakeProjectsApi`, `FakeProjectDao` gained `errorToThrow` and call recorders
(`.claude/agents/testing.md` updated). The full `jvmTest`, `detekt`, `ktlintCheck` and `:koverVerify`
are all green.

| Scope | Before | After |
|---|---|---|
| package `feature/projects/data` BRANCH | 3/26 — 11.5 % | **26/26 — 100 %** |
| package `feature/projects/data` LINE | 50/124 — 40.3 % | 120/125 — 96.0 % |
| `ProjectValuesRepositoryImpl` BRANCH | 0/20 | **20/20** |
| `ProjectValuesRepositoryImpl` LINE | 3/41 | **41/41** |
| `ProjectsRepositoryImpl` BRANCH | 3/6 | **6/6** |
| `ProjectsRepositoryImpl` LINE | 47/83 | 79/84 |
| package `feature/projects/mapper` BRANCH | 5/40 | **40/40** |
| package `feature/projects/mapper` LINE | 77/146 | **146/146** |

Unlike `core/api`, none of this module is hidden by the `excludes` block — `**.*Repository` does not
match `…RepositoryImpl` — so the delta is real and visible in the report.

**This was not purely an error-path sweep.** `ProjectValuesRepositoryImpl` had **no test at all**
(0/20 branches), and four `ProjectsRepositoryImpl` methods (`getProjectDetails`, `getProjectModules`,
`updateModules`, `updateProject`) and `getCurrentProjectFlow` were untested. Check whether the module
actually has happy-path coverage before scoping the next one as "just add the failure paths".

Four things worth carrying forward:

- **The Kover class-count flip is not triggered only by build-file changes.** CLAUDE.md records two
  stable outputs (821 / 742) with a build-script edit as the trigger. This session produced a **third,
  854**, with *no* build file touched — adding test sources was enough. Comparing the package tables
  of the two reports shows exactly what the 854 run leaks in: `core/storage/db/dao` (+1182 lines),
  `core/storage/db`, `core/storage/di`, `*Widget`/`*Screen` classes in `feature/*/ui`, the Ktor
  plugins in `core/api` — i.e. **854 = excludes not applied, 742 = excludes applied in full**. The
  direction is the opposite of what CLAUDE.md's "clean tree → 821" note implies, so treat the trigger
  as unknown and the *class count* as the only reliable signal, exactly as that note's last bullet says.
- **The escape hatch when the counts disagree: diff the per-package denominators.** A three-line
  script over the two XMLs showed every package the two runs disagree about, and confirmed
  `feature/projects/data` (BRANCH 26) and `feature/projects/mapper` (BRANCH 40) have *identical*
  denominators in both. That makes the table above valid despite 742 vs 854. Do this instead of
  discarding the measurement.
- **`git stash -u` + re-run reproduced the baseline exactly** (742, same numbers to the digit), which
  is what proved the 854 was caused by the change rather than by a stale build.
- **`fetchProjects` is tested without collecting the flow, deliberately.** Collecting it runs
  `ProjectsPagingSource` against `FakeProjectsApi.getProjectsPaging`, which cannot return an
  `HttpResponse`; the escaping exception would be attributed to an unrelated test. The reason is in
  a KDoc on the test. `ProjectsPagingSource` is Kover-excluded (`**.*PagingSource`) *and* blocked on
  the same `HttpResponse` problem as task 9b — solve it once, there.

### `feature/kanban/ui` — ✅ done 2026-08-03

25 new tests in `KanbanViewModelTest` (11 → 36). `:feature:kanban:ui:jvmTest`, the full `jvmTest`,
`detekt`, `ktlintCheck` and `:koverVerify` are all green.

| Scope | Before | After |
|---|---|---|
| `KanbanViewModel` BRANCH | 44/142 — 31.0 % | **135/142 — 95.1 %** |
| `KanbanViewModel` LINE | 109/169 — 64.5 % | **169/169 — 100 %** |
| package `feature/kanban/ui` BRANCH | 65/166 — 39.2 % | **159/166 — 95.8 %** |
| package `feature/kanban/ui` LINE | 231/295 — 78.3 % | 291/295 — 98.6 % |

The before/after reports had different class counts (854 vs 744), but every class in this package has
an *identical* denominator in both, so the table is valid — the escape hatch the
`feature/projects/data` session documented, used a second time.

**Like the previous module, this was not an error-path sweep.** Both failure paths already had tests.
What was missing was the ViewModel's two largest private functions, neither of which any test reached
past its first line:

- **`computeSwimlaneFilters`** (~55 branches) — scopes the project-wide filter list down to the
  stories actually on the board, counting assignees / creators / statuses / tags / epics / roles and
  dropping filters that match nothing. Every existing test set `filtersDataResult = FiltersData()`,
  whose `filtersNumber` is 0, so all eleven of them took the `if (allFilters.filtersNumber == 0)`
  early return. **A fixture that looks like a neutral default can be an early-return switch**;
  check what the SUT does with `FiltersData()` before reusing it everywhere.
- **`filterStories`** (~30 branches) — six `matches*` predicates. One of the six was covered.

Three things worth carrying forward:

- **The Kover class-count flip no longer needs to be fought.** Added
  [kover-rank.py](kover-rank.py), which re-applies the root `excludes` block to whatever report you
  have and ranks packages by missed branches. It reduced this session's 854-class report to 742
  classes and reproduced the genuine 742-class run's totals exactly. Two sessions have now been
  spent on this flip; the script ends that.
- **The 9a ranking table was inflated for a second reason beyond the flip.** `feature/login/ui`'s
  126 branches were almost entirely `LoginScreen`; the ViewModel is 44. Rank on the filtered numbers
  or you will pick a module whose work is already excluded from the report.
- **`FakeFiltersRepository.getFiltersData` had no `…Throws` hook**, so the existing failure test
  forced an error by leaving `filtersDataResult = null` and tripping the fake's own
  `error("filtersDataResult not set")` guard — exactly the failure-for-the-wrong-reason trap task 9
  documented. Added `filtersDataThrows` and switched that test to it. `FakeUserStoriesRepository`
  gained a recorder per `bulkUpdateKanbanOrder` argument, which is what let the neighbour-validation
  branches in `moveStory` be asserted at all. `.claude/agents/testing.md` updated for both.

One dead branch found, not filed: `computeOptimisticUpdate`'s `.takeIf { it >= 0 } ?: size` fallback
cannot fire, because `moveStory` has already validated `beforeStoryId` against the target column. It
is two tokens of defensive code, not worth a revisit entry. Passing an unknown `newStatusId` also
drops the story off the board entirely — unreachable from the UI, where the id always comes from a
rendered column; noted in a comment on the test that covers it.

### `utils/ui` — ✅ done 2026-08-03

68 new tests across 6 new files. `:utils:ui:jvmTest` goes 7 → 75 tests; the full `jvmTest`, `detekt`,
`ktlintCheck` and `:koverVerify` are all green. `:testing` was **not** touched — this module needs no
fakes, so `.claude/agents/testing.md` is unchanged.

| File | Tests | Covers |
|---|---|---|
| `GetErrorMessageTest` (commonTest) | 24 | all 19 `NetworkException` error codes, the `taigaError.message` short-circuit, the `else`, `UntrustedCertificateNetworkException`, and the generic-throwable fallback |
| `JsonSerializableNavTypeTest` (commonTest) | 11 | `typeMapOf` both branches; `put`/`get`/`serializeAsValue`/`parseValue` on both nav types, incl. the whole nullable one |
| `ColorMapperTest` (commonTest) | 9 | `fromColorToString`, `fromStringToColor` (6/8/invalid), `fromStringToInt` all three branches |
| `ComposableUtilsTest` (commonTest) | 7 | `fixNullColor`, `textColor` both sides, the deprecated `toHex`/`toColor` |
| `NativeTextTest` (commonTest) | 6 | `isEmpty`/`isNotEmpty`, `asStringBlocking` for `Simple`/`Empty`/`Multi` incl. nesting |
| `StringUtilsJvmTest` (**jvmTest**) | 5 | the JVM actuals of `formatColor` and `formatStringKmp` |
| `PagingUtilsTest` (commonTest) | 5 | `CombinedLoadStates.getErrorMessage` — error, non-error, append/prepend ignored, custom fallback |

| Scope | Before | After |
|---|---|---|
| package `utils/ui` BRANCH | 8/107 — 7.5 % | **61/107 — 57.0 %** |
| package `utils/ui` LINE | 46/185 — 24.9 % | **126/185 — 68.1 %** |
| `NativeTextKt` BRANCH | 2/52 | **35/52** |
| `JsonSerializableNullableNavType` BRANCH | 0/8 | **8/8** (LINE 0/17 → 17/17) |
| `PagingUtilsKt` BRANCH | 0/12 | 7/12 |
| `ColorMapperKt` / `ComposableUtilsKt` / `JsonSerializableNavTypeKt` BRANCH | 1/3, 4/6, 1/2 | **3/3, 6/6, 2/2** |

Both reports were 742-class runs with identical totals (2049 BRANCH / 9709 LINE), so no comparability
dance was needed this time.

**This was not an error-path sweep at all, and the next-module ranking should stop assuming one.**
`utils/ui` has no repository, no ViewModel and no collaborator that can throw — it is pure functions
plus Composables. The 99 missed branches were simply *never-tested code*, and the one place a
`catch` exists (`fromStringToColor`) needed a malformed string, not a fake. The 9a table ranks by
missed branches, which finds modules like this one just as readily as repository-shaped ones; read
the module before scoping the session.

**The residual 46 missed branches are all blocked on the same thing: a composition.** Nothing left in
this module is reachable from a plain JVM test —

- `ObserveAsEventsKt` 0/16 and `ColorSourceKt.asColor` 0/4 are `@Composable`;
- `NativeTextKt`'s remaining 17 are the `@Composable` `asString` plus the `Resource`/`Plural`/
  `Arguments` arms of `asStringBlocking`;
- `PagingUtilsKt`'s remaining 5 are the `LazyPagingItems` extensions, and `LazyPagingItems` has no
  constructor outside a composition;
- `ContextExtensionsKt` 0/4 is `androidMain`, which no JVM test can reach at all.

So this module is **done** as far as non-instrumented testing goes, and the leftover is a concrete
argument for task 10 rather than an omission here.

Three things worth carrying forward:

- **Compose resources cannot be resolved from a plain KMP module's `jvmTest`.** A probe calling
  `NativeText.Resource(RString.error_not_found).asStringBlocking()` dies with
  `ExceptionInInitializerError` ← `org.jetbrains.skiko.LibraryLoadException: Cannot find
  libskiko-linux-x64.so.sha256`: the resource loader's initialisation pulls in Skiko, whose native
  binary only arrives with `compose.desktop.currentOs`. That was **not** added — it is a native
  dependency on a test classpath to buy three `when` arms, and it overlaps task 10's wiring. Anyone
  who wants `getString`/`getPluralString` under test should do it there, once, deliberately. Asserting
  on `NativeText` structurally (`assertEquals(NativeText.Resource(RString.x), …)`) needs none of it,
  which is why `GetErrorMessageTest`'s 24 tests cost nothing.
- **`:koverVerify` and a 742-class `koverXmlReport` agree exactly.** `:koverVerify` reported
  75.4249 % / 60.5173 %; `kover-rank.py` over the same run's XML reported 75.42 % / 60.52 %. The
  "~5 points apart" warning in CLAUDE.md and [revisit #8](../revisit.md#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify)
  therefore describes `koverVerify` versus an **821/854-class** XML, not an intrinsic difference —
  **`kover-rank.py`'s totals are the gate number**, not an approximation of it. Read `:koverVerify`'s
  own figures by temporarily setting both `minValue`s to 99; it names both rules and prints the actual
  percentages.
- **The floor is now ~17/22 points below actual** (58/38 versus 75.42/60.52) and was deliberately
  *not* raised — the gap is far larger than the tests added since task 8 can explain, which suggests
  `:koverVerify` may flip between excludes modes the same way `koverXmlReport` does. Filed as
  [revisit #14](../revisit.md#14-the-kover-coverage-floor-is-now-1722-points-below-actual) with the
  arithmetic and the check to run first. Do not raise it from a single reading.

Also found and filed, not fixed: `urlDecode` is an `internal expect` with three actuals and **zero
call sites** ([revisit #13](../revisit.md#13-urldecode-in-utilsui-is-dead-code-with-three-actuals)).
`JsonSerializableNavTypeTest` uses it to reverse `serializeAsValue`, so deleting it means rewriting
two assertions.

### `main` — ⛔ closed-as-blocked 2026-08-03

**No tests written. This module cannot be moved without a composition, and was closed rather than
worked.** It was marked NEXT on the strength of its 4/35 branch figure alone; reading it first — which
the preamble above tells you to do — shows the figure is unreachable. Per-class, from a clean
742-class report:

| Class | BRANCH | LINE | Reachable? |
|---|---|---|---|
| `MainViewModel` (+ its two lambdas) | **4/4** | **44/44 + 13/13** | already fully covered |
| `MainAppState` | 0/22 | 0/29 | 20 of 22 are `@Composable` getters |
| `TaigaAppContentKt` | 0/9 | 0/14 | `@Composable` |
| `MainScreenState` / `InitialNavState` | 0/0 | 3/3, 1/1 | — |

`MainAppState`'s 22 decompose exactly: `currentDestination` ~4, `currentTopLevelDestination` ~6,
`areDrawerGesturesEnabled` 2, `isTopBarVisible` ~8 — all `@Composable get()` — plus
`navigateToTopLevelDestination` 2. That last one is the *only* thing in the package a JVM test could
reach, and it needs a `NavHostController` with a real graph installed. A whole session for at most 2
branches.

`MainScreen.kt` and `MainNavHost.kt` do not appear at all: `**.*Screen` and `**.*NavHost` are in the
root `excludes`.

**The lesson for whoever picks the next module: a package's missed-branch count does not tell you
whether the branches are reachable.** This is the second time — after `utils/ui`'s residual 46 — that
the ranking has pointed at composition-blocked code. Before taking a module, get the *per-class*
breakdown out of the report (the snippet in the `customfields` section below does it) and check
whether the classes carrying the branches are `@Composable`. `main` joins `utils/ui` as concrete
evidence for task 10.

### `feature/workitem/ui/delegates/customfields` — ✅ done 2026-08-03

23 tests in `WorkItemCustomFieldsDelegateImplTest` (1 → 23). `:feature:workitem:ui:jvmTest`, the full
`jvmTest`, `detekt`, `ktlintCheck` and `:koverVerify` are all green.

| Scope | Before | After |
|---|---|---|
| `WorkItemCustomFieldsDelegateImpl` BRANCH | 0/30 — 0 % | **28/30 — 93.3 %** |
| `WorkItemCustomFieldsDelegateImpl` LINE | 19/86 — 22.1 % | **86/86 — 100 %** |
| package `…delegates.customfields` BRANCH | 0/30 | **28/30** |
| package `…delegates.customfields` LINE | 27/94 | **94/94** |

The two reports had different class counts (742 vs 746, raw 742 vs 797), but this package's
denominators are identical in both — the escape hatch, used a fourth time.

**The residual 2 branches are unreachable**, and the XML says which: a `<method>`-level read shows
they are both in `getCustomFieldValue`, i.e. the two `?.` null-checks in
`valueToUse?.toString()?.toLongOrNull()`. `NumberItemState.originalValue` and `.currentValue` are
non-null `String`s, so `valueToUse` is never null on that arm. Useful technique in general:

```bash
python3 -c "
import xml.etree.ElementTree as ET
r=ET.parse('build/reports/kover/report.xml').getroot()
for p in r.findall('package'):
    if 'YOUR.PACKAGE' not in p.get('name').replace('/','.'): continue
    for c in p.findall('class'):
        for m in c.findall('method'):
            cs={x.get('type'):(int(x.get('covered')),int(x.get('missed'))) for x in m.findall('counter')}
            b=cs.get('BRANCH')
            if b and b[1]: print(c.get('name').split('/')[-1], m.get('name'), f'B {b[0]}/{b[0]+b[1]}')"
```

Kover's XML carries per-**method** counters, not just per-class. Nothing in this plan had used them
before, and they answer "is the leftover real?" in one command instead of by reading the source.

**The 9a table's note on this module — "no test at all" — was wrong**; there was one test, covering
`setIsCustomFieldsWidgetExpanded`. It contributed 19 lines and 0 branches, which is why the package
looked untested. Likewise the table's claim that `core/api/errors`' 29 missed branches are "inside
the Kover-excluded `ErrorMappingPlugin`" was wrong — `kover-rank.py` has already filtered that plugin
out; the 29 are 25 generated `TaigaErrorResponse` branches plus 4 real ones. Both rows are corrected
above. **Re-derive a row before trusting its Note column**, not just its number.

Three things worth carrying forward:

- **A private pure function can be driven entirely through the payload it produces.**
  `getCustomFieldValue` is private and has 12 of the class's 30 branches. Rather than making it
  internal for testability, the tests read
  `patchCustomAttributesCalls.last().payload["attributes_values"]` — the map the delegate hands
  `PatchDataGenerator` — and assert the per-field value. That reached every item type
  (`Date` non-null / null, `Number` parseable / not, `Checkbox`, `Dropdown`, the `else`) with no
  production change. The unwrapping is a two-line helper with a KDoc saying why.
- **`PatchDataGeneratorImpl` is used real, not faked.** It is a pure map-builder in
  `feature/workitem/data`, already on this module's classpath, and faking it would have hidden the
  `attributes_values` nesting the assertions depend on.
- **`FakeWorkItemRepository.patchCustomAttributes` was `error("not used in this test")`** and gained
  `patchCustomAttributesResult/Throws/Calls` plus a `PatchCustomAttributesCall` record
  (`.claude/agents/testing.md` updated). That fake now has hooks on every method a test has needed;
  the remaining `error("not used in this test")` stubs are `createWorkItem` and nothing else.

One behaviour finding, filed not fixed:
[revisit #15](../revisit.md#15-saving-a-non-editable-custom-field-leaks-its-id-into-editingitemids) —
`handleCustomFieldSave`'s success path calls the *toggle* `onCustomFieldEditToggle(item)` to close
edit mode, but the save button is rendered for every item type while edit mode is only ever entered
for `EditableItem`s. So saving a Text/Number/Date/Dropdown/Checkbox field adds its id to
`editingItemIds` permanently. Invisible today only because the single reader of that set ANDs it with
`isEditableItem`. The test that documents it is named `- adds a non-editing item to editingItemIds`
and carries a KDoc saying it will need inverting when the bug is fixed.

### `feature/workitem/ui/delegates/badge` — ✅ done 2026-08-04

17 tests added to `WorkItemBadgeDelegateImplTest` (2 → 19). `:feature:workitem:ui:jvmTest`, the full
`jvmTest`, `koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing` was
**not** touched — `FakeWorkItemRepository` already carried `patchDataResult/Throws/Calls` and
`getStatusUI()` already existed, so `.claude/agents/testing.md` is unchanged.

| Scope | Before | After |
|---|---|---|
| `WorkItemBadgeDelegateImpl` BRANCH | 0/22 — 0 % | **22/22 — 100 %** |
| `WorkItemBadgeDelegateImpl` LINE | 15/66 — 22.7 % | **66/66 — 100 %** |
| package `…delegates.badge` BRANCH | 0/22 | **22/22** |
| package `…delegates.badge` LINE | 20/71 | **71/71** |

Both reports were 742-class runs with identical totals (2049 BRANCH / 9709 LINE), so no comparability
dance was needed. Repo-wide: BRANCH 1271 → 1293, LINE 7400 → 7451.

**All 22 branches were in one method.** A per-method read of the baseline XML (the snippet in the
`customfields` section) put every one of them in `handleBadgeSave` — the class's other four members
were already covered by the two pre-existing tests. That made the session's shape obvious before any
code was read: 4 payload arms × the same 4 `badge.copy(currentValue = item)` arms, the `badge == type`
else arm, the `?.invoke` null-checks on `doOnPreExecute`/`doOnSuccess`, and `resultOf`'s catches.

Three things worth carrying forward:

- **The 9a row's note was right this time** — "same shape as `customfields`" held, and the module took
  well under a session because the fake needed no new hooks. Two `…delegates/*` modules in a row have
  been the cheapest rows on the table; the remaining delegates are a reasonable place to keep going
  when a short session is what's available.
- **`resultOf`'s cancellation rethrow is testable and worth one test.** `resultOf` is `inline`, so its
  `catch (e: CancellationException) { throw e }` counts against the *caller's* branch total. Setting
  `patchDataThrows = CancellationException("cancelled")` and wrapping the call in
  `assertFailsWith<CancellationException>` inside `runTest` works — the exception is thrown by a plain
  suspend call, not by a cancelled job, so `runTest` does not treat it as a test cancellation. The
  `catch (e: TimeoutCancellationException)` clause below it is dead code (it is a `CancellationException`
  subclass, so the first clause always wins) and needs no test.
- **`assertEquals` against an `ImmutableMap<String, Any?>` needs the expected side typed.**
  `assertEquals(mapOf("status" to id), call.payload)` fails to compile with "the value of the type
  parameter 'T' must be mentioned in input types"; `mapOf<String, Any?>(…)` fixes it. Extracting the
  payload read into a one-line helper also keeps those assertions inside ktlint's 120-column limit.

The one behaviour observation, not filed as a revisit because it is unreachable from the UI: saving a
badge that is **not** in `workItemBadges` patches the work item and changes nothing in state — the
badge handed to `handleBadgeSave` always comes from a rendered `workItemBadges` entry. It is the
`else` arm of `badge == type` and is covered by a test whose KDoc says so.

### `feature/settings/ui/attributes/projectvalues` — ✅ done 2026-08-04

20 tests in a new `ProjectValuesViewModelTest`. `:feature:settings:ui:jvmTest`, the full `jvmTest`,
`koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing` gained
`FakeProjectValuesRepository` and a settable `presetColorsResult` on `FakeTaigaSessionStorage`
(`.claude/agents/testing.md` updated for both).

| Scope | Before | After |
|---|---|---|
| `ProjectValuesViewModel` (+ its 4 lambdas) BRANCH | 0/22 — 0 % | **22/22 — 100 %** |
| `ProjectValuesViewModel` (+ its 4 lambdas) LINE | 5/99 — 5.1 % | **99/99 — 100 %** |
| package `…attributes.projectvalues` BRANCH | 0/22 | **22/22** |
| package `…attributes.projectvalues` LINE | 5/125 | **117/125** |

The baseline and the final after-run were both 742-class runs with identical totals (2049 BRANCH /
9709 LINE), so no comparability dance was needed. Repo-wide: BRANCH 1293 → 1314, LINE 7451 → 7563.
The *intermediate* after-run came back at **787** classes and is what turned up a third Kover
class-count mode — `excludes` applied in full, surplus made up of Android-variant / Room classes.
That is written up in `CLAUDE.md` (Testing) and [revisit #8](../revisit.md#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify);
the package's denominators were identical in all three runs, so it did not affect this table.

**The residual 8 lines are unreachable and carry no branches.** `EditFormState` (0/7) is a
`private data class` declared *inside* `ProjectValuesScreen.kt` and constructed only by
`initialEditState`, which is called from a `@Composable`; the file's `*Screen` function is
Kover-excluded but the nested private class is not, so it shows up as a 7-line hole that no JVM test
can fill. The other is `ProjectValuesState.kt:30` — the *default* value of the `onSaveItem` parameter,
a six-underscore no-op lambda the ViewModel always overrides.

Three things worth carrying forward:

- **`resultOf`'s cancellation rethrow is testable inside a `viewModelScope.launch`, not just from a
  plain suspend call.** The badge session established the pattern with `assertFailsWith`; here the
  call sites are fire-and-forget launches, so setting `…Throws = CancellationException("cancelled")`
  and asserting the *loading flag is still set* is what proves the rethrow happened — neither
  `onSuccess` nor `onFailure` ran, so nothing cleared it. `viewModelScope` is a `SupervisorJob`, so the
  cancelled child neither fails the test nor disturbs its siblings. One such test per `resultOf` call
  site (three here) is what took the package from 19/22 to 22/22.
- **A repeated inline expression is repeated branches.** `color.ifBlank { null }` appears once in the
  create arm and once in the update arm of `onSaveItem`; covering blank-vs-blank on the create path
  left the update path's copy missed, and the after-run came back 21/22 for that one reason. The fix
  was one argument in an existing test, but only a per-method re-read of the XML found it — budget a
  second measurement pass for any method with duplicated argument-building code.
- **The `…Calls` recorder as a single `data class` per method pays for itself.**
  `FakeProjectValuesRepository.SaveCall` holds all eight create/update arguments (`id = null` marks a
  create), which lets the create test assert the entire argument set in one `assertEquals` instead of
  eight. That is what made the `ifBlank` / `toDoubleOrNull` / `toIntOrNull` argument mapping cheap to
  pin down.

No behaviour bug found; nothing filed to [revisit.md](../revisit.md) this session.

### `feature/workitem/ui/screens/edittags` — ✅ done 2026-08-04

20 tests in a new `WorkItemEditTagsViewModelTest`. `:feature:workitem:ui:jvmTest`, the full
`jvmTest`, `koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. **`:testing`
was not touched** — `FakeProjectsRepository` already had `getTagsColorsResult/Throws`,
`createTagCalled/Throws` and the `editTag…` recorders, and `FakeTaigaSessionStorage` already had
`tagPresetColorsResult`, so `.claude/agents/testing.md` is unchanged.

| Scope | Before | After |
|---|---|---|
| `WorkItemEditTagsViewModel` BRANCH | 0/6 | **6/6** |
| `…$fetchTags$1` BRANCH | 0/10 | **10/10** |
| `…$notifyChange$1` BRANCH | 0/4 | **4/4** |
| package `…screens.edittags` BRANCH | 0/20 — 0 % | **20/20 — 100 %** |
| package `…screens.edittags` LINE | 13/127 — 10.2 % | **124/127 — 97.6 %** |

Both reports were 742-class runs with identical totals (2049 BRANCH / 9709 LINE), so no
comparability dance was needed. Repo-wide: BRANCH 1314 → 1335, LINE 7563 → 7676.

**`EditEpicViewModelTest` is a drop-in template for this whole family of screens.** Same
`SavedStateHandle(mapOf("workItemId" to …, "taskIdentifier" to Json.encodeToString(taskIdentifier)))`
wiring, same real (not faked) `WorkItemEditStateRepository`, same `launch { …getXFlow(…).take(1)
.collect { } }` + `sut.onBackAction.test { }` pairing for the `onGoingBack`/`notifyChange` paths —
those channels are rendezvous, so nothing is sent unless a collector is already waiting. Copying its
skeleton is most of why this row cost well under a session. `screens/sprint` is the same shape again.

Three things worth carrying forward:

- **The reload arm of a fetch function may only be reachable through a *different* public entry
  point.** `fetchTags`' four `isInitialLoad = false` branches cannot be reached from `init` — the
  only second call is the one `onSaveTag`'s `doOnSuccess` makes after a tag is created. So the test
  that covers them is the `onSaveClick` test, not a "refresh" test; there is no refresh callback on
  this screen. Before concluding a branch is unreachable, look for an indirect caller.
- **`resultOf`'s cancellation rethrow can be covered without any observable difference in state.**
  Unlike the badge (`assertFailsWith`) and projectvalues (a still-set loading flag) sessions, this
  ViewModel has no loading flag and swallows failures into a log line, so the cancellation test and
  the failure test assert *the same thing* — an empty `tags`. Only the branch counter tells them
  apart. The test carries a KDoc saying so; that is more honest than inventing an assertion.
- **Every `logcat { }` message lambda is an uncovered line no JVM test can reach**, because the JVM
  backend is the no-op `NoLog` and it never invokes the lambda. Two of this package's three residual
  lines are exactly that (`onTagClick`'s tag-not-found warning and `fetchTags`' failure log); the
  third is `EditTagsState`'s six-underscore default `onSaveClick`, the same unreachable
  default-parameter lambda the projectvalues session found. Repo-wide there are 96 such call sites.
  Written up as [revisit #16](../revisit.md#16-every-logcat-message-lambda-is-a-permanently-uncovered-line),
  including the `:testing`-installs-a-logger fix and why it is a build change rather than a test one.
  **Recognise the signature — a 1-line hole in an otherwise 100 % method — and stop.**

No behaviour bug found in the ViewModel itself.

### `feature/workitem/ui/screens/sprint` — ✅ done 2026-08-04

20 tests in a new `EditSprintViewModelTest`. `:feature:workitem:ui:jvmTest`, the full `jvmTest`,
`koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing` gained
`getSprintsResult` / `getSprintsThrows` / `getSprintsIsClosed` on `FakeSprintsRepository`
(`getSprints` was `error("not used in this test")`); `.claude/agents/testing.md`'s
`FakeSprintsRepository` entry said "check file for fields" and now lists them.

| Scope | Before | After |
|---|---|---|
| `EditSprintViewModel` BRANCH | 0/6 | **6/6** |
| `…$getPermissions$1` BRANCH | 0/4 | **4/4** |
| `…$getSprints$1` BRANCH | 0/4 | **4/4** |
| `…$notifyChange$1` BRANCH | 0/4 | **4/4** |
| package `…screens.sprint` BRANCH | 0/18 — 0 % | **18/18 — 100 %** |
| package `…screens.sprint` LINE | 8/75 — 10.7 % | **74/75 — 98.7 %** |

The baseline was a 742-class run and the after-run a **781**-class one (745 after `kover-rank.py`
filtering) — the Android-variant/Room mode, not the excludes-skipped mode (zero excluded-suffix
leaks in both). The BRANCH denominator is 2049 in both, i.e. identical to the gate; LINE is 9762 vs
9709, the ~53-line inflation that mode is already documented to cause. This package's own
denominators (18 BRANCH / 75 LINE) are identical in both reports, so the table is valid — the escape
hatch used again. Repo-wide BRANCH 1335 → 1353.

**The `edittags` row's claim that `screens/sprint` is "the same shape again" was correct**, and this
was the cheapest row yet: `EditEpicViewModelTest`'s skeleton transferred almost verbatim (same
`SavedStateHandle(mapOf("workItemId" to …, "taskIdentifier" to Json.encodeToString(taskIdentifier)))`
wiring, same real `WorkItemEditStateRepository`, same `launch { …getSprintFlow(…).take(1)
.collect { } }` + `sut.onBackAction.test { }` pairing for the rendezvous channel). No behaviour bug
found.

Two things worth carrying forward:

- **A `when` over `TaskIdentifier` means the test's `SavedStateHandle` has to be per-test, not a
  field.** `getPermissions`' four branches are `is TaskIdentifier.WorkItem` × `CommonTaskType.Issue`
  vs. `else`, so covering them needs three different routes — `WorkItem(Issue)` with and without
  `MODIFY_ISSUE`, `WorkItem(UserStory)`, and `TaskIdentifier.Wiki`. Making `createViewModel()` take a
  `taskIdentifier` parameter (defaulting to `WorkItem(UserStory)`) and building the handle inside it
  is what made that cheap; the sibling tests all hold the handle as a field and could not have done
  this. `Json.encodeToString(TaskIdentifier.Wiki)` round-trips through `typeMapOf` fine.
- **The single residual line is a `logcat { }` lambda**, exactly the signature CLAUDE.md says to stop
  at — `getSprints`' `"Error while getting sprints"` failure log, reported as
  `EditSprintViewModel$getSprints$1.invokeSuspend$lambda$2$0` at 0/1. Recognised and left; see
  [revisit #16](../revisit.md#16-every-logcat-message-lambda-is-a-permanently-uncovered-line).

---

### `feature/settings/ui/modules` — ✅ done 2026-08-04

16 tests in a new `ModulesViewModelTest`. `:feature:settings:ui:jvmTest`, the full `jvmTest`,
`koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing` gained
`FakeProjectsRepository.UpdateModulesCall` + `updateModulesCalls` (the fake only had a
`updateModulesCalled` boolean, which cannot see the `toIntOrNull` / `toDoubleOrNull` conversions);
`.claude/agents/testing.md`'s `FakeProjectsRepository` entry now lists it.

| Scope | Before | After |
|---|---|---|
| `ModulesViewModel` LINE | 20/28 | **28/28** |
| `…$loadModules$1` BRANCH | 0/12 | **10/12** |
| `…$save$1` BRANCH | 0/4 | **4/4** |
| package `…settings.ui.modules` BRANCH | 0/16 — 0 % | **14/16 — 87.5 %** |
| package `…settings.ui.modules` LINE | 38/88 — 43.2 % | **88/88 — 100 %** |

The baseline was a **781**-class run (Android-variant mode, zero excluded-suffix leaks) and the
after-run an **822**-class one *with* 20 leaks — i.e. the excludes-skipped mode, so the repo-wide
totals in the two are not comparable. This package's own denominators (16 BRANCH / 88 LINE) are
identical in both, so the table above is valid: the package-scope escape hatch, used again.
`kover-rank.py` filtered the after-run to 746 classes / BRANCH 1368-2053 / LINE 7842-9771.

**LINE reached 100 %, which no module in this sweep had managed before** — this ViewModel has no
`logcat { }` message lambda that a test cannot enter, because both of its two `logcat` calls sit on
paths the failure tests take *and* Kover attributes them to the already-covered `invokeSuspend`
rather than to a separate synthetic method. Do not generalise the 96-`logcat` rule into "LINE can
never be 100 %".

**The residual 2 branches are the documented unreachable shape, not a gap.** Lines 59–60 are
`modules.totalMilestones?.toString() ?: ""` (and the same for `totalStoryPoints`), each reported
`mb=1 cb=3`: the safe call contributes 2 branches and the elvis 2, but `toString()` on a non-null
receiver never returns null, so the elvis's null arm is unreachable from the non-null path. Same
shape as `WorkItemCustomFieldsDelegateImpl`'s residual 2/30. **`<line mb= cb=>` attributes in the
`<sourcefile>` element are how to pin this down** — the per-*method* breakdown says only
"`invokeSuspend` 10/12", which is not enough to tell an unreachable elvis from a missing test:

```bash
python3 -c "
import xml.etree.ElementTree as ET
r=ET.parse('build/reports/kover/report.xml').getroot()
for p in r.findall('package'):
  if p.get('name','').endswith('settings/ui/modules'):
    for sf in p.findall('sourcefile'):
      for l in sf.findall('line'):
        if l.get('mb')!='0': print(sf.get('name'), 'line', l.get('nr'), 'mb', l.get('mb'), 'cb', l.get('cb'))"
```

One thing worth carrying forward: **this ViewModel suspends on *two* rendezvous channels, and each
needs the collector started first.** `save`'s success arm ends in `_navigateBack.send(Unit)` and its
failure arm calls `showSnackbarSuspend` — both `Channel()` with no buffer. So `onSaveClick()` has to
be triggered *inside* the turbine block (`sut.navigateBack.test { sut.state.value.onSaveClick();
awaitItem() }`), and any assertion about state the coroutine sets *after* the send — `isSaving =
false` on the failure path — has to come after the block, not inside it. Getting that order wrong
hangs the failure test rather than failing it. No behaviour bug found.

### `createtask` — ✅ done 2026-08-04

23 tests in two new files under `composeApp/src/commonTest/…/createtask/`:
`CreateWorkItemUseCaseTest` (9) and `CreateTaskViewModelTest` (14). The full `jvmTest`,
`koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing` gained
`create*Result/Throws/Calls` on all four repository fakes the use case fans out to —
`FakeTasksRepository.CreateTaskCall`, `FakeIssuesRepository.CreateIssueCall`,
`FakeUserStoriesRepository.CreateUserStoryCall` and the top-level `CreateWorkItemCall` for
`FakeWorkItemRepository`; all four `create*` methods were `error("not used in this test")` before.
`.claude/agents/testing.md`'s four entries were updated.

| Scope | Before | After |
|---|---|---|
| `CreateWorkItemUseCase` BRANCH | 0/3 | **3/3** |
| `CreateWorkItemUseCase` LINE | 5/31 | **31/31** |
| `CreateTaskViewModel` BRANCH | 0/6 | **6/6** |
| `CreateTaskViewModel` LINE | 4/34 | **34/34** |
| `…$onCreateTask$3` BRANCH | 0/4 | **4/4** |
| `…$onCreateTask$3` LINE | 0/23 | 22/23 |
| package `createtask` BRANCH | 0/13 — 0 % | **13/13 — 100 %** |
| package `createtask` LINE | 9/97 — 9.3 % | **96/97 — 99.0 %** |

The baseline was a clean **742**-class run — the CI-equivalent mode — and the after-run an
**822**-class one with 20 leaks, i.e. the test-sources-only flip *into* the excludes-skipped mode
documented in CLAUDE.md, for the third session running. This package's denominators (13 BRANCH /
97 LINE) are identical in both, so the table is valid. `kover-rank.py` filtered the after-run to 746
classes / BRANCH 1381-2053 67.27 % / LINE 7929-9771 81.15 %.

**The residual 1 line is the documented `logcat` hole** — `CreateTaskViewModel.kt:94`, the
`"Error creating task"` message lambda, `mi=1 ci=0 mb=0 cb=0`. The failure test does take that path;
the JVM `NoLog` backend just never invokes the lambda.

Three things worth carrying forward:

- **The `else ->` arm of a `when` over a 4-value enum is reachable only through the one value the
  `when` doesn't name.** `CreateWorkItemUseCase` handles `Task`/`Issue`/`UserStory` explicitly and
  routes everything else to `WorkItemRepository.createWorkItem`, so `CommonTaskType.Epic` *is* the
  `else` test. Kover counts that `when` as 3 branches, not 4.
- **A route argument that goes through `typeMapOf` must be put into the `SavedStateHandle` as its
  JSON encoding, not its name.** `"type" to Json.encodeToString(CommonTaskType.Task)` — i.e. the
  string `"Task"` *including the quotes*, since `JsonSerializableNavType.get` calls
  `Json.decodeFromString`. The four `Long?` arguments alongside it use navigation's own nullable
  types and go in as raw `Long`/`null` values, and `toRoute` parses both in the same call.
- **`CreateWorkItemUseCase` is a concrete class in `composeApp`, so it cannot be faked and `:testing`
  cannot host a fake for it** (that would be a dependency cycle). The ViewModel test therefore builds
  the **real** use case over the four repository fakes — same shape as `MainViewModelTest` building a
  real `AuthStateManager`. This is why 23 tests cover both classes to 100 % of branches.

Also: `assertFailsWithTestException` does not fit a function returning `Result` — it takes a
throwing block, and `resultOf` returns the throwable instead of rethrowing. `CreateWorkItemUseCaseTest`
has a local `assertFailure(result)` doing the same type+message check on `result.exceptionOrNull()`.
Two tests now want this; if a third appears, move it to `:testing`. No behaviour bug found.

### `feature/settings/ui/user` — ✅ done 2026-08-04

4 tests in a new `SettingsUserScreenViewModelTest`. `:feature:settings:ui:jvmTest`, the full
`jvmTest`, `koverXmlReport`, `:koverVerify`, `detekt` and `ktlintCheck` are all green. `:testing`
gained `FakeUsersRepository.getMeResult/Throws/CallCount` — `getMe()` was
`error("not used in this test")`; `.claude/agents/testing.md`'s entry was updated.

| Scope | Before | After |
|---|---|---|
| `SettingsUserScreenViewModel$loadData$1` BRANCH | 0/4 | **4/4** |
| `SettingsUserScreenViewModel$loadData$1` LINE | 0/17 | **16/17** |
| package `…settings.ui.user` BRANCH | 0/12 — 0 % | **4/12 — 33.3 %** (the reachable 4 are 4/4) |
| package `…settings.ui.user` LINE | 14/46 — 30.4 % | **30/46 — 65.2 %** |

The residual 1 line is the documented `logcat` hole — `SettingsUserScreenViewModel.kt:46`, the
`"Error loading user"` message lambda, `mi=1 mb=0 cb=0`. The failure test does take that path.
The residual 8 branches are `OpenByDefaultSettingsButton`'s Android actual (0/6) and JVM actual
(0/2), both `@Composable`; the JVM one is a deliberately empty function that Kover still credits
with 2 branches. Neither is reachable from a JVM unit test, exactly as the row predicted.

**The class-count flip went the other way this session**, for the first time: the baseline was an
**822**-class run with 20 excluded-suffix leaks (excludes-skipped mode) on a *clean tree*, and the
after-run — with two test-source files added — was a clean **742** with zero leaks, i.e. the
CI-equivalent mode. That is the exact opposite of the "adding test sources flips a clean run *into*
the leaky mode" transition recorded for the three preceding sessions. Treat the direction as
genuinely unpredictable and rely on the package-scope escape hatch, which held: this package's
denominators (12 BRANCH / 46 LINE) are identical in both reports, so the table is valid.
`kover-rank.py` on the after-run: 742 classes, BRANCH 1385/2049 67.59 %, LINE 7895/9709 81.32 %.

No behaviour bug found. Nothing about this ViewModel needed a turbine dance — it has no
`SnackbarDelegate` and no channel; the failure path only writes `error` into state, same shape as
`TeamViewModel`.

### `feature/settings/ui` — ⛔ closed-as-blocked 2026-08-04

Verified from the same 742-class report as the row above, without spending a session on it. All 8 of
the package's missed branches are in `ThemeSelectorKt` (0/8 BRANCH, 0/25 LINE), a `@Composable` — the
same composition-blocked shape that closed `main`. The rest of the package is already covered:
`SettingsState` LINE 1/1, `SettingsViewModel` LINE 7/7.

The one genuine leftover is **`SettingsViewModel$1` at LINE 0/5 and 0 branches** — the `init`
coroutine that reads `getCurrentProjectSimple().isAdmin` into `canSeeAttributes`. A two-test file
would close it, but it buys **zero branches**, so it does not belong in a sweep ranked by missed
branches. Worth doing only if someone is in that file anyway.

### `core/storage` — ✅ done 2026-08-04

56 new tests across 5 files; the module goes 12 → 68 tests. `:core:storage:jvmTest`, the full
`jvmTest`, `detekt`, `ktlintCheck` and `:koverVerify` are all green. Nothing in `:testing` was
touched, so `.claude/agents/testing.md` needed no update.

| File | Source set | Tests | Covers |
|---|---|---|---|
| `ThemeSettingsTest` | `commonTest` | 8 | `fromValue` (each entry, null, unknown, case), `default()`, the three `is*` extensions |
| `AuthStorageImplTest` | `jvmTest` | 10 | empty defaults, store/overwrite, all four `isLoggedIn` combinations, `clear` |
| `TaigaSessionStorageImplTest` | `jvmTest` | 23 | every property and method, incl. all six defaults and the preset-colour add/remove branches |
| `FiltersStorageImplTest` | `jvmTest` | 9 | all four sections, the null/blank/populated decode paths, key independence, `resetFilters` |
| `DataStoreServerStorageTest` | `jvmTest` | 6 | all three host-fallback branches, `defineServer`, the file-name constant |

**The decision the row asked for: real `PreferenceDataStoreFactory` over a temp file in `jvmTest`,
not a hand-written in-memory `DataStore` in `commonTest`.** `TrustedCertStorageImplTest` already set
that precedent in this module. The rationale is written into
`jvmTest/…/core/storage/TestDataStore.kt`, which holds the shared `createTestDataStore(name)` helper:
a fake `DataStore` would assert the fake's behaviour rather than the real read-modify-write and
serialization these classes depend on. Nothing platform-specific is under test — the storage classes
are all `commonMain`; `jvmTest` is purely where a filesystem path is available.
`TrustedCertStorageImplTest` was left with its own inline copy (surgical-changes rule).

| Scope | Before | After |
|---|---|---|
| package `core/storage` BRANCH | 0/18 | **27/28** |
| package `core/storage` LINE | 54/100 | **106/106** |
| package `core/storage/auth` BRANCH | 0/6 | **10/10** |
| package `core/storage/auth` LINE | 23/31 | **33/33** |
| package `core/storage/server` BRANCH | 2/8 | **8/8** |
| package `core/storage/server` LINE | 15/19 | **19/19** |
| `TaigaSessionStorageImpl` BRANCH | 0/4 | **14/14** |
| `TaigaSessionStorageImpl` LINE | 29/49 | **55/55** |
| whole-report BRANCH | 1385/2049 — 67.59 % | **1465/2063 — 71.01 %** |

Both runs were the clean 742-class / zero-leak mode, i.e. directly comparable and what CI sees.

Five things worth carrying forward:

- **The row's 18 branches were an undercount.** It named `FiltersStorageImpl` 0/8,
  `TaigaSessionStorageImpl` 0/4 and `ThemeSettingsKt` 0/6, but `AuthStorageImpl$isLoggedIn$1` (0/6)
  and `DataStoreServerStorage` (1/6 + 1/2) are in the same package tree, hand-written and equally
  reachable. Re-derive a row's per-class breakdown before scoping, in both directions — the standing
  advice is to check whether a row is *over*stated, and this one was understated by more than half.
- **`:testing`'s `getFiltersData()` returns a value equal to `FiltersData()`** — every one of its
  nine lists is `persistentListOf()`. A `StateFlow` conflates an update equal to the value it already
  holds, so a `changeScrumFilters(getFiltersData())` test times out in turbine with "No value
  produced in 3s" while the write has in fact succeeded. Cost half an hour; the DataStore probe
  showed `filters_scrum={}` on disk with the `StateFlow` unmoved. The test file now builds its own
  non-default `FiltersData` and says why. **Before using a model factory as a "changed" value, check
  that it differs from the type's default.**
- **A Kover denominator can grow simply because the code started executing.**
  `TaigaSessionStorageImpl` went BRANCH 0/4 → 14/14 and LINE 29/49 → 55/55 in two same-mode,
  same-class-count runs. CLAUDE.md treats a moved denominator as a signal that two runs are not
  comparable; that rule is about *report-level* totals and does not hold at class level, where an
  unexecuted class can be reported with fewer branches than it has. Compare `covered` against the
  *after* denominator, and do not read a growing denominator here as a bad measurement.
- **`feature/filters/domain/model` moved from 2/144 to 39/144 as a side effect**, without a single
  test being written against it. Round-tripping `FiltersData` through `Json` in
  `FiltersStorageImplTest` exercised the generated serializer branches. Both this plan and CLAUDE.md
  write that package off as unreachable generated code — that is **too strong**: the `equals`/
  `hashCode`/`copy$default` share is genuinely unreachable, but the `@Serializable` share is reached
  by any test that serializes the type. Do not add it to a sweep on the strength of this, but do not
  quote "142 unreachable" either.
- **Residuals, all verified unreachable or out of scope.** `FiltersStorageImpl` sits at 7/8: the
  per-line view pins it to line 33, `value?.takeIf { … }?.let { … } ?: FiltersData()`, whose elvis
  null arm is dead once `let` has run — the same always-one-short `?.`-plus-elvis shape CLAUDE.md
  records for `x?.toString() ?: ""`. `core/storage/utils/StringPreference` (0/2 BRANCH, 0/10 LINE) is
  `androidMain`-only and invisible to a JVM test. `TrustedCertStorageImpl` stays at 7/10 — that is
  its pre-existing test file, untouched here.

---

## Task 9b — `WorkItemRemoteMediator`

**Why:** the one class in `feature/workitem/data` with **zero** coverage (0/11 branches), left
untouched by task 9 because it is a new-coverage task, not an error-path sweep.

**What the SUT does:** a Paging 3 `RemoteMediator` whose `load()` branches on `LoadType`
(REFRESH → page 1, PREPEND → early `Success(endOfPaginationReached = true)`, APPEND → page computed
from `state.pages`), on `taskType == UserStory` for the `sprint` parameter, and on
`response.hasNextPage()`. `defaultTryCatch` turns any throw into `MediatorResult.Error`.

**The blocker to solve first:** `load()` needs a real Ktor `HttpResponse` — `FakeWorkItemApi`'s
`getWorkItemsPagination` is `error("not used in this test")` and cannot simply return a stub, since
the SUT calls `.body()` and `hasNextPage()` (a header read) on it. Decide between a Ktor
`MockEngine`-backed response and narrowing `WorkItemApi`'s return type; **write the decision into the
test file.** This is why it is its own task.

---

## Task 10 — Compose UI test spike (one uikit widget)

**Why:** nothing in the repo tests a single Composable. Screens, uikit widgets and navigation wiring
have no automated verification at all. This task is a **spike**: prove the wiring works on one
widget, then decide whether to expand.

**Scope — deliberately tiny:**

- Add `runComposeUiTest` support to `:uikit` (`compose.uiTest` dependency in `commonTest`; on JVM it
  needs the desktop test artifact).
- Write **one** test for **one** simple, stateful uikit widget.
- Write down what the wiring took, in `docs/testing/`.

**Watch for:** `:uikit` is currently excluded from Kover aggregation, and per Task 1, non-aggregated
modules do not run in CI via the Kover step. Confirm the new test actually runs in CI — if Task 1's
`jvmTest` step is in place it will, but verify rather than assume. Re-including `:uikit` in Kover
aggregation is a reasonable follow-up but is **not** part of this task.

**Done when:** one Compose test passes via `./gradlew :uikit:jvmTest`, runs in CI, and the setup is
documented well enough that the next widget test needs no research.

**Finalize focus:** high. The output of a spike is knowledge, not the test. If the wiring turns out
to be painful or flaky, **say so** and recommend against expanding — a negative result is a
successful spike.

---

## Considered and deferred

Recorded so they are not silently forgotten, and not re-proposed without new information.

| Idea | Why not now |
|---|---|
| Run `commonTest` on a native target (`iosSimulatorArm64Test`) | Real gap — no `expect/actual` divergence is caught by tests today. But CI is `ubuntu-latest`; this needs a macOS runner at roughly 10× the minutes. Worst value-per-cost on the list. Revisit if an iOS-only regression actually ships. |
| Screenshot tests (Roborazzi / Paparazzi) | High maintenance for a solo-maintained app. Revisit only if visual regressions become a recurring, concrete problem — not preemptively. |
| Integration tests against a live Taiga instance | `tools/seed` and the local instance in `docs/local-info.md` make it feasible, but it would be a manually-triggered job, not part of PR CI. No pull for it yet. |
| Adding a mocking framework | The hand-written-fake convention is working and is genuinely consistent. Do not introduce MockK to `commonTest`. |
| Testing `dto` and pure-`domain` modules | Most of the 36 untested modules are serializable data holders or interfaces plus models. Correctly untested; leave them. |
