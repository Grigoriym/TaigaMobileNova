# Testing suite: what exists today

**Surveyed:** 2026-08-01 · **Re-verified:** 2026-08-02 against `21bcb6ad`
**Scope:** survey of the test suite as it currently stands on `dev` — types of tests, layout,
tooling, distribution, coverage. Descriptive, not a proposal — the proposal is
[improvement-plan.md](improvement-plan.md).

> Every figure below was re-derived on 2026-08-02. The original survey was written against a tree
> that `af9731df` invalidated hours later; see [Survey drift](#survey-drift). Re-verify before
> citing.

## TL;DR

There is exactly **one kind of test in this project: host-side JVM unit tests.** 984 `@Test`
methods across 101 files in 44 modules, all running on the JVM via `kotlin.test`. No instrumented
tests, no Compose UI tests, no screenshot tests, no integration or end-to-end tests exist anywhere
in the repo.

The suite is fast (full run in seconds), green, and built entirely on hand-written fakes rather
than a mocking framework.

## Test types

Verified absent — grepped the whole repo for `runComposeUiTest`, `createComposeRule`, Roborazzi,
Paparazzi, Espresso, `src/androidTest`, and Koin's `checkModules()`/`verify()`. Zero hits.

| Type | Present | Notes |
|---|---|---|
| JVM unit tests | **yes** | the entire suite |
| Android instrumented (`androidTest`) | no | no such source set in any module |
| Compose UI / interaction tests | no | `uikit` and all `ui` modules test ViewModels only |
| Screenshot / snapshot tests | no | no Roborazzi or Paparazzi dependency |
| Integration / E2E | no | — |
| Koin DI graph verification | **yes**, since 2026-08-02 | `KoinGraphTest`, JVM only — see below |

That last row was "no" when this survey was written: `koin-test` and `koin-test-junit4` are declared
in `testing/build.gradle.kts:9-14` — but in the **`androidMain`** source set, so nothing in
`commonTest` or `jvmTest` could reach them, and the DI graph was validated only by the app actually
starting. [Task 2](improvement-plan.md#task-2--koin-di-graph-test) closed this with `KoinGraphTest`
in `composeApp/src/jvmTest/`, which resolves all 147 definitions of the real graph. The `androidMain`
declarations above are still unused. See [docs/koin/koin-graph-test.md](../koin/koin-graph-test.md)
for what it does and does not cover.

## Where tests live

| Source set | Modules | Contents |
|---|---|---|
| `src/commonTest` | 43 modules | 956 `@Test` in 97 files — effectively the whole suite |
| `src/jvmTest` | `core/api`, `core/storage` | 27 `@Test` in 3 files — JVM-only crypto (`CompositeTrustManager`, `TrustedCertStorage`, JVM error mapping) |
| ~~`src/test` (Android)~~ | ~~`androidApp`~~ | ~~1 `@Test` — the generated `ExampleUnitTest` (`assertEquals(4, 2 + 2)`)~~ — deleted 2026-08-02 by Task 1; the repo now has no Android unit-test source set |

Despite being `commonTest`, these only ever execute on the JVM. The convention plugin registers
`androidTarget`, `iosArm64`, `iosSimulatorArm64` and `jvm`, but CI and the documented workflow run
`jvmTest` exclusively — no `iosSimulatorArm64Test` invocation anywhere. Kover explicitly disables
the Android unit-test variants (`KmpConfiguration.kt:17-20`), so the common tests are, in
practice, JVM tests that merely *could* run on other targets.

## Wiring

Test dependencies are injected automatically for every KMP module by `configureKmp()` in
`build-logic/convention/src/main/kotlin/com/grappim/taigamobile.buildlogic/KmpConfiguration.kt:46`:

```kotlin
commonTest.dependencies {
    implementation(kotlin("test"))
    implementation(project(":testing"))
}
```

No module declares test dependencies by hand. Adding a test to any KMP module requires no build
file change at all.

## Tooling stack

| Tool | Role | Usage |
|---|---|---|
| `kotlin.test` | assertions, `@Test`/`@BeforeTest`/`@AfterTest` | all 101 files |
| `kotlinx-coroutines-test` | `runTest`, `UnconfinedTestDispatcher` | 68 files |
| Turbine (`app.cash.turbine`) | `StateFlow`/`Flow` emission assertions | 19 files |
| Hand-written fakes | test doubles | everywhere |
| Kover 0.9.7 | coverage | aggregated at root, uploaded to Codecov |

**No mocking framework in `commonTest`.** MockK appears nowhere in the shared suite. On the JVM
target, `kotlin.test` delegates to JUnit 4 (visible in failure stack traces as
`kotlin.test.junit.JUnitAsserter` → `org.junit.Assert`), but tests are written against the
`kotlin.test` API, not JUnit's.

## The `:testing` module

A first-class `commonMain` module (not a test source set) that every KMP module gets on its test
classpath. ~70 files:

- **12 fake APIs** — `FakeAuthApi`, `FakeEpicsApi`, `FakeWorkItemApi`, …
- **12 fake repositories** — `FakeWorkItemRepository`, `FakeProjectsRepository`, …
- **9 fake use cases** — `FakeGetKanbanDataUseCase`, `FakeTaskDetailsDataUseCase`, …
- **7 fake storages** — `FakeAuthStorage`, `FakeServerStorage`, `FakeTrustedCertStorage`, …
- **3 fake DAOs** — `FakeProjectDao`, `FakeSprintDao`, `FakeWorkItemDao`
- **16 model factories** — `WorkItemFakes.kt`, `UserFakes.kt`, `ProjectFakes.kt`, …
- **Utilities** — `MainDispatcherRule`, `TestUtils.kt`, and an `expect/actual`
  `PlatformTestUtils` with android/ios/jvm actuals

It `api`-exposes ~45 production modules so tests can reference domain types directly. That is why
`testing/build.gradle.kts` is a long list of `api(projects.…)` lines — it's a deliberate umbrella,
not accidental coupling.

### Fake data is randomized

`TestUtils.kt` provides `getRandomString()`, `getRandomLong()`, `getRandomInt()`,
`getRandomBoolean()`, `getRandomColor()`, `getRandomLocalDateTime()`, plus `nowLocalDate` /
`nowLocalDateTime` and a shared `testException`. Model factories build on these, so every fixture
is different per run.

This is a real strength — it prevents tests from accidentally depending on magic constants. It
also has a sharp edge: a test must capture the generated value and assert against *that*, never
re-invoke the generator expecting the same result. Two assertions in `TaskMapperTest` got this
wrong and went unnoticed for as long as the file was never executed — see
[Survey drift](#survey-drift).

## Conventions

The suite is unusually consistent. The dominant shape, e.g.
`feature/wiki/ui/.../WikiCreatePageViewModelTest.kt`:

```kotlin
internal class WikiCreatePageViewModelTest {

    private val wikiRepository = FakeWikiRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: WikiCreatePageViewModel

    @BeforeTest fun setup() { mainDispatcherRule.setup() }
    @AfterTest fun tearDown() { mainDispatcherRule.tearDown() }

    private fun createViewModel() { sut = WikiCreatePageViewModel(wikiRepository) }
    ...
}
```

Observed conventions:

- System under test is always named `sut`.
- Fakes are constructed as plain fields; the SUT is built by a private `createViewModel()` helper
  so individual tests can configure fakes before construction.
- `MainDispatcherRule` is a hand-rolled KMP replacement for the JUnit rule (it can't be a real
  `@Rule` in `commonTest`), driven manually from `@BeforeTest`/`@AfterTest`. Used in 25 files.
  Defaults to `UnconfinedTestDispatcher`.
- Backtick test names describing behaviour: ``fun `initial state has empty slug and content` ()``.
- Section comments (`// --- initial state ---`) group tests within a file.
- Test classes are `internal`.

## Distribution

By architectural layer:

| Layer | Tests | Files |
|---|---|---|
| `ui` (ViewModels, UI mappers, delegates) | 499 | 44 |
| `mapper` | 194 | 21 |
| `data` (repository impls) | 172 | 16 |
| `core` | 61 | 10 |
| `domain` (use cases) | 34 | 7 |
| app-level (`composeApp`, `androidApp`) | 24 | 3 |

Top modules by test count:

| Module | Tests | Files |
|---|---|---|
| `feature/workitem/ui` | 249 | 21 |
| `feature/workitem/mapper` | 88 | 10 |
| `feature/workitem/data` | 73 | 3 |
| `feature/wiki/ui` | 52 | 5 |
| `core/api` | 43 | 5 |
| `feature/filters/mapper` | 30 | 3 |
| `feature/sprint/data` | 26 | 2 |
| `feature/settings/ui` | 26 | 2 |

`feature/workitem` alone accounts for **410 of 984 tests (42%)** — consistent with it being the
hub feature that epics, issues, tasks and user stories all route through. Its 14 delegate
implementations each have their own test file.

## Coverage

Measured via `./gradlew koverXmlReport` on a clean `dev` at `21bcb6ad`, re-run 2026-08-02. **This is
the pre-improvement baseline, kept for comparison — it is not the current figure.** After
improvement-plan tasks 3–7, the same command at `af8a185a` reports line 65.3 % / branch 45.9 %; the
apparent drop is denominator growth, not lost coverage, and the like-for-like numbers are line
71.9 % / branch 49.7 %. Before quoting any Kover percentage, read
[task 8's result note](improvement-plan.md#task-8--coverage-floor-in-ci-koververify) — `koverVerify`
and `koverXmlReport` report different numbers.

| Counter | Covered | Total | % |
|---|---|---|---|
| Instruction | 49 374 | 77 542 | **63.7 %** |
| Line | 6 538 | 9 694 | **67.4 %** |
| Class | 455 | 742 | **61.3 %** |
| Method | 981 | 1 939 | **50.6 %** |
| Branch | 981 | 2 047 | **47.9 %** |

Note the shape: line coverage is decent but **branch coverage is ~20 points lower**. The suite
tends to exercise happy paths through a function without covering its conditionals. This is the
single clearest quality signal in the survey, and what
[Task 9](improvement-plan.md#task-9--error-path-convention--first-sweep) targets.

Restoring the 25 orphaned mapper tests (see [Survey drift](#survey-drift)) moved branch coverage by
0.6 points and line coverage by none — worth knowing, because it means those tests were not the
reason for the gap. The gap is systemic.

These figures are already flattered by aggressive exclusions in the root `build.gradle.kts`. A
`variants()` helper strips four JVM name variants each for `Api`, `DTO`, `Repository`, `Delegate`,
`Module`, `Screen`, `Widget`, `Dialog`, `Destination`, `NavHost`, and more, plus whole packages
(generated string resources, `core.storage.db`, `core.storage.cache`, DI glue). Most of these are
justifiable — generated or declarative code — but it means the percentages describe a
deliberately narrowed denominator, not the whole codebase. Note `*RepositoryImpl` classes are
*not* excluded (the pattern only matches names ending in `Repository`), so repository logic does
count.

## CI

Single job, `.github/workflows/code_analysis.yml`, on `ubuntu-latest`:

1. `./gradlew detekt`
2. `./gradlew ktlintCheck`
3. `./gradlew jvmTest` — this is what runs the tests
4. `./gradlew koverXmlReport` — reuses the results from step 3
5. Upload `build/reports/kover/report.xml` to Codecov, flag `unittests`

**Fixed 2026-08-02 by [Task 1](improvement-plan.md#task-1--close-the-tests-that-never-run-in-ci-trap).**
At the time of the survey there was **no separate test step**: `koverXmlReport` was the only thing
that executed tests, and Kover only runs `jvmTest` for the modules aggregated into the report —
`:testing`, `:uikit`, `:tools:seed`, `:tools:utils` and `:androidApp` are excluded from aggregation
in the root `build.gradle.kts`. A test added to any of those modules would never have run in CI,
silently. The `jvmTest` step now covers `:testing` and `:uikit`; the `tools/*` modules are
`kotlin("jvm")` and their task is `test`, so they remain uncovered (and remain testless).

## Gaps

36 of 80 modules have no tests. Most are unremarkable — `dto` modules are serializable data
holders and `domain` modules are often just interfaces plus models. The ones with actual untested
logic:

This table is a **baseline snapshot**, not a live status — the
[improvement plan](improvement-plan.md) is where "still open?" is answered. Closed rows are struck
through rather than deleted so the baseline stays readable.

| Module | What's untested |
|---|---|
| ~~`feature/wiki/data`~~ | ~~`WikiRepositoryImpl`~~ — closed by plan task 3 |
| ~~`feature/kanban/domain`~~ | ~~`GetKanbanDataUseCase`~~ — closed by plan task 5 |
| ~~`feature/profile/domain`~~ | ~~`GetProfileDataUseCase`~~ — closed by plan task 4 |
| ~~`utils/formatter/datetime`~~ | ~~`DateTimeUtilsImpl`, `KotlinxDateTimeFormatter`~~ — closed by plan task 6; the iOS actual stays uncovered |
| `utils/formatter/decimal` | platform actuals |
| ~~`feature/teams/ui`~~ | ~~`TeamViewModel`~~ — closed by plan task 7. **The "last untested ViewModel" claim was wrong** — see below |
| `core/navigation` | — |
| `uikit` | excluded from Kover aggregation entirely |

**Twelve ViewModels are still untested** (found 2026-08-03 while closing task 7, which the table
above wrongly called "the last untested ViewModel" — the original survey only walked the modules it
had flagged, not every `*ViewModel.kt`). Re-derive with:

```bash
for f in $(grep -rl "ViewModel(" --include="*ViewModel.kt" feature composeApp | grep -v "/build/"); do
  n=$(basename $f .kt)
  find . -name "${n}Test.kt" -not -path "*/build/*" | grep -q . || echo "$f"
done
```

| Module | ViewModel | Lines |
|---|---|---|
| `feature/settings/ui` | `SettingsViewModel` | 28 |
| `feature/settings/ui` | `SettingsAboutScreenViewModel` | 28 |
| `feature/settings/ui` | `SettingsUserScreenViewModel` | 57 |
| `feature/settings/ui` | `SettingsInterfaceViewModel` | 72 |
| `feature/settings/ui` | `ProjectDetailsViewModel` | 120 |
| `feature/settings/ui` | `ModulesViewModel` | 127 |
| `feature/settings/ui` | `ProjectValuesViewModel` | 177 |
| `feature/scrum/ui` | `ScrumClosedSprintsViewModel` | 14 |
| `feature/workitem/ui` | `EditDescriptionViewModel` | 72 |
| `feature/workitem/ui` | `EditSprintViewModel` | 143 |
| `feature/workitem/ui` | `WorkItemEditTagsViewModel` | 223 |
| `composeApp` | `CreateTaskViewModel` | 117 |

`feature/settings/ui` is the concentration — 7 of the 12. No plan task covers these; they are not
sequenced anywhere yet.

Structural gaps, independent of any module:

- **Nothing tests Compose output.** All `ui` module tests stop at the ViewModel boundary. Screen
  composables, uikit widgets, and navigation wiring have no automated verification. `feature/filters/ui`
  falls entirely in this gap — it contains no ViewModel at all, only `FilterModalBottomSheetWidget`
  and `TaskFiltersWidget`, so there is nothing there a unit test could reach.
- **Common tests never run on iOS.** They are `commonTest` by location but JVM-only in practice,
  so no `expect/actual` divergence is caught by tests.
- **No DI graph test**, despite the tooling being available.
- **Branch coverage 20 points below line coverage** — error paths and conditionals are
  systematically under-tested relative to happy paths.
- ~~**`androidApp` has only the generated `ExampleUnitTest`** (`2 + 2 == 4`), the sole inhabitant of
  the repo's only Android unit-test source set. It tests nothing.~~ Removed 2026-08-02 by Task 1;
  `androidApp/src/test` no longer exists.

## Survey drift

This document was first written on 2026-08-01 and was **partly obsolete within hours**. Commit
`af9731df` — which landed the same evening — fixed three of the things the survey reported as open:

- It moved `ProjectMapperTest` and `TaskMapperTest` out of `src/test/java` (a directory belonging to
  no KMP source set, silently skipped as `NO-SOURCE`) into `src/commonTest/kotlin`. Two assertions
  in `TaskMapperTest` had drifted from the `:testing` factories and failed the moment they actually
  executed; both were fixed in that commit. Those 25 tests now run.
- It re-enabled the Detekt step in `code_analysis.yml`, which the survey reported as commented out.
- It fixed Detekt's KMP source-set wiring — 75 of 77 module tasks had been reporting `NO-SOURCE`.

Two lessons, both of which apply to this file going forward:

1. **The `NO-SOURCE` failure mode is the one to watch.** A test that does not run looks identical to
   a test that passes. It appeared here as `src/test/java` in a KMP module; it appears again as the
   Kover-aggregation gap described under [CI](#ci). Both are silent.
2. **Re-derive counts before citing them.** The figures in this document are re-verified as of
   2026-08-02, but the commands that produce them are cheap — run them rather than trusting the
   table.

A third lesson, added 2026-08-03 while closing plan task 7:

3. **A gap table is only as complete as the sweep that produced it.** This file called
   `TeamViewModel` "the last untested ViewModel"; enumerating every `*ViewModel.kt` and looking for a
   matching `*Test.kt` found twelve more (see [Gaps](#gaps)). The original pass inspected the modules
   it had already flagged instead of the whole set, so the *absence* of a row meant nothing. Any
   "this is the last X" claim in this document should be re-derived by a mechanical sweep before it
   is relied on.
