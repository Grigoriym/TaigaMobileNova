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
   what actually happened — especially anything that differed from the description.
5. Run the **`finalize` skill**. Each task lists a *Finalize focus* — the thing most likely worth
   capturing — but that is a hint, not a substitute for the skill's own harvest step.

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
| 4 | `GetProfileDataUseCase` | XS | ⬅ **NEXT** |
| 5 | `GetKanbanDataUseCase` + `FakeSwimlanesRepository` | M | todo |
| 6 | `DateTimeUtilsImpl` + `KotlinxDateTimeFormatter` | M | todo |
| 7 | `TeamViewModel` | S | todo |
| 8 | Coverage floor in CI (`koverVerify`) | S | todo — blocked on 3–7 |
| 9 | Error-path convention + first sweep | M | todo |
| 10 | Compose UI test spike (one uikit widget) | M | ⛔ deferred — do not start |

**Scope decision (2026-08-02):** tasks 0–9 — the unit / non-instrumented work — are in scope and
should be worked straight through. **Task 10 is deferred pending a decision**, along with
everything in [Considered and deferred](#considered-and-deferred). Do not start it without asking;
the other test *types* get decided once the unit-test work has landed.

Sizes: XS = minutes, S = under an hour, M = a focused session.

Tasks 0–2 are ordered deliberately: 0 makes the reference doc trustworthy, 1 guarantees that
anything later tasks write actually runs in CI, 2 is the highest-value single test in the plan.
Tasks 3–7 are independent of each other and can be reordered freely. Task 8 depends on 3–7 having
landed (the floor should be set from the improved numbers). Tasks 9 and 10 are the open-ended ones.

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
