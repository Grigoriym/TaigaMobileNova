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
| Koin DI graph verification | no | `koin-test` is declared but unreachable — see below |

That last row is worth knowing: `koin-test` and `koin-test-junit4` are declared in
`testing/build.gradle.kts:9-14` — but in the **`androidMain`** source set, so nothing in
`commonTest` or `jvmTest` can reach them. Nothing in the repo calls `checkModules()` or `verify()`.
The DI graph is validated only by the app actually starting. Closing this is
[Task 2](improvement-plan.md#task-2--koin-di-graph-test).

## Where tests live

| Source set | Modules | Contents |
|---|---|---|
| `src/commonTest` | 43 modules | 956 `@Test` in 97 files — effectively the whole suite |
| `src/jvmTest` | `core/api`, `core/storage` | 27 `@Test` in 3 files — JVM-only crypto (`CompositeTrustManager`, `TrustedCertStorage`, JVM error mapping) |
| `src/test` (Android) | `androidApp` | 1 `@Test` — the generated `ExampleUnitTest` (`assertEquals(4, 2 + 2)`) |

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

Measured via `./gradlew koverXmlReport` on a clean `dev` at `21bcb6ad`, re-run 2026-08-02:

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
3. `./gradlew koverXmlReport` — this is what actually runs the tests
4. Upload `build/reports/kover/report.xml` to Codecov, flag `unittests`

There is **no separate test step**: `koverXmlReport` is the only thing that executes tests. That has
a consequence worth stating plainly — Kover only runs `jvmTest` for the modules aggregated into the
report, and `:testing`, `:uikit`, `:tools:seed`, `:tools:utils` and `:androidApp` are excluded from
aggregation in the root `build.gradle.kts`. **A test added to any of those modules would never run
in CI, silently.** No tests are currently being lost this way, but nothing prevents it.
[Task 1](improvement-plan.md#task-1--close-the-tests-that-never-run-in-ci-trap) closes it.

## Gaps

36 of 80 modules have no tests. Most are unremarkable — `dto` modules are serializable data
holders and `domain` modules are often just interfaces plus models. The ones with actual untested
logic:

| Module | What's untested |
|---|---|
| `feature/wiki/data` | `WikiRepositoryImpl` — the only repository impl in the project without a test |
| `feature/kanban/domain` | `GetKanbanDataUseCase` |
| `feature/profile/domain` | `GetProfileDataUseCase` |
| `utils/formatter/datetime` | `DateTimeUtilsImpl`, `KotlinxDateTimeFormatter` (+ 3 platform actuals) |
| `utils/formatter/decimal` | platform actuals |
| `feature/teams/ui` | `TeamViewModel` — the last untested ViewModel; every sibling `ui` module has tests |
| `core/navigation` | — |
| `uikit` | excluded from Kover aggregation entirely |

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
- **`androidApp` has only the generated `ExampleUnitTest`** (`2 + 2 == 4`), the sole inhabitant of
  the repo's only Android unit-test source set. It tests nothing.

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
