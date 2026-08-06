# CLAUDE.md

TaigaMobileNova is an unofficial Kotlin Multiplatform client for Taiga.io targeting Android, iOS, and Desktop. Built with Kotlin, Compose Multiplatform, and follows a modular MVVM + Clean Architecture.

## Build Commands

```bash
# Android - build debug APK
./gradlew :androidApp:assembleGplayDebug
./gradlew :androidApp:assembleFdroidDebug

# Desktop - run or package
./gradlew :composeApp:run
./gradlew :composeApp:packageDistributionForCurrentOS   # Deb / Dmg / Msi

# iOS - link the framework (Xcode calls embedAndSignAppleFrameworkForXcode automatically)
./gradlew :composeApp:linkReleaseFrameworkIosArm64          # device
./gradlew :composeApp:linkReleaseFrameworkIosSimulatorArm64 # simulator

# Run tests — KMP modules use jvmTest; androidApp has Android-specific variants
./gradlew :module:path:jvmTest --tests "com.package.TestClass"
./gradlew :androidApp:testFdroidDebugUnitTest --tests "com.package.TestClass"

# Run all JVM tests across all modules (skips Android unit tests which require mocking)
./gradlew jvmTest

# Run JVM tests for a single KMP module
./gradlew :module:path:jvmTest

# Generate coverage report (Kover — runs jvmTest on all aggregated modules)
./gradlew koverXmlReport    # XML → build/reports/kover/report.xml (uploaded to Codecov)
./gradlew koverHtmlReport   # HTML → build/reports/kover/html/index.html
./gradlew :koverVerify      # coverage floor (line ≥ 58 %, branch ≥ 38 %) — must be qualified

# Force Koin compiler to re-run (skipped on UP-TO-DATE, which causes "no definition found" crashes)
# Run this before launching from Xcode whenever DI definitions may have changed
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks   # iOS simulator
./gradlew :composeApp:compileKotlinIosArm64 --rerun-tasks            # iOS device
# Android equivalent (also forces Koin compiler logs):
./gradlew :androidApp:compileFdroidDebugKotlin --rerun-tasks
```

## Architecture

**Module structure:** `androidApp/` (Android entry point) + `composeApp/` (KMP library) → `feature/` → `core/` → `utils/`
- Features have data/domain/ui layers (sometimes dto/mapper)
- Use `NativeText` (in `utils:ui`) for localized strings in ViewModels
- Dependencies via Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Build plugins in `build-logic/`

**Platforms:**

- **Android** — `androidTarget()`, Min SDK 24, Target SDK 37, flavors: Gplay / Fdroid
- **iOS** — `iosArm64()` + `iosSimulatorArm64()`, static framework `TaigaMobileNovaIos`; entry point in `main.ios.kt`
- **Desktop/JVM** — `jvm()`, entry point `TaigaMobileDesktop.kt`; packages Deb/Dmg/Msi

**Tech Stack:**

- Kotlin 2.3.x, JDK 21
- Compose Multiplatform with Material Design 3
- Koin (with `io.insert-koin.compiler.plugin` IR/FIR plugin) for DI
- Ktor for networking (OkHttp on Android/JVM, Darwin on iOS)
- Navigation Compose (KMP) with type-safe routes
- Kotlin Serialization for JSON
- Coroutines, Coil 3.x for images (KMP-ready)
- Room 2.8.4 + BundledSQLiteDriver (KMP-ready)
- `core/logger` — KMP logging facade (see Logging below); Timber backs it on Android only

**Convention Plugins** (in `build-logic/`):

- `taigamobile.android.application` - Android application module (`androidApp`) — applies AGP, Compose, Koin compiler plugin
- `taigamobile.kmp.library` - KMP base (Android + iOS + JVM targets, coroutines, collections)
- `taigamobile.kmp.library.compose` - Adds Compose Multiplatform across all targets; enables `androidResources` for CMP asset pipeline
- `taigamobile.kmp.di` - Applies `io.insert-koin.compiler.plugin` + Koin dependencies
- `taigamobile.kmp.serialization` - Kotlin Serialization setup
- `taigamobile.kmp.network` - Ktor with platform-specific engines (OkHttp / Darwin)
- `taigamobile.kotlin.library` - Pure Kotlin library (no Android/KMP)

## Navigation Pattern

Destinations use `@Serializable` data classes/objects:
```kotlin
@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long)

fun NavController.navigateToTask(taskId: Long, ref: Long) {
    navigate(route = TaskDetailsNavDestination(taskId, ref))
}
```

ViewModels extract arguments via `SavedStateHandle.toRoute<T>()`:
```kotlin
private val route = savedStateHandle.toRoute<TaskDetailsNavDestination>()
private val taskId = route.taskId
```

## ViewModel + State Pattern

State class contains data AND callback functions:
```kotlin
data class FeatureState(
    val data: String = "",
    val onDataChange: (String) -> Unit,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
```

ViewModel exposes `StateFlow`, updates via `.update {}`:
```kotlin
private val _state = MutableStateFlow(FeatureState(onDataChange = ::setData, ...))
val state = _state.asStateFlow()

private fun setData(value: String) {
    _state.update { it.copy(data = value) }
}
```

Use `NativeText` for strings from ViewModel → resolve in UI with `text.asString(context)`.

## One-off Events Pattern

Use `Channel` + `receiveAsFlow()` for navigation, snackbars, and other one-off events. Never put these in UI state.

```kotlin
// ViewModel - use SnackbarDelegate or create Channel directly
private val _navigateBack = Channel<Unit>()
val navigateBack = _navigateBack.receiveAsFlow()

// Screen - observe with ObserveAsEvents from utils.ui
ObserveAsEvents(viewModel.navigateBack) { onNavigateBack() }
```

For snackbars, use `SnackbarDelegate` from `utils.ui`:
```kotlin
class MyViewModel @Inject constructor() : ViewModel(), SnackbarDelegate by SnackbarDelegateImpl() {
    // Use showSnackbarSuspend(message) to show snackbars
}
```

## Use Cases

Use cases only when multiple repository calls are needed. For single repo calls, call repository directly from ViewModel.

## Feature Module Structure

```
feature/{name}/
├── data/     → API, DTOs, RepositoryImpl, Koin module
├── domain/   → Models, Repository interface
└── ui/       → NavDestination, Screen, State, ViewModel
```

## Koin DI

For DI patterns, the `expect/actual @Configuration` rule, module registry, qualifier map, and troubleshooting, see the **koin-expert** subagent. It is not in this repo — it lives in `agentic-grappim` and is symlinked into `~/.claude/agents/` (see Skills below). Never use KSP — this project uses `io.insert-koin.compiler.plugin` exclusively.

## KMP String Resources

Every `RString.x` reference needs its **own** import (`import com.grappim.taigamobile.strings.generated.resources.create_task`)
— the generated strings are extension properties, so importing `RString` alone gives
`Unresolved reference 'create_task'`. This bites in test files as often as in Composables: a test
asserting `NativeText.Resource(RString.title_is_empty)` needs the same import line the ViewModel has.

`strings.xml` (`strings/src/commonMain/composeResources/values/`) does not need Android-style
apostrophe/quote escaping (`\'`) — Compose Multiplatform's resource loader doesn't apply AAPT's
escaping rules, so plain `'` works correctly. Don't escape apostrophes in new strings.

## uikit Components

For available Composable components, theme tokens, TopBarController usage, drag-and-drop, and offline/permission UI patterns, see the **uikit-guide** subagent (`.claude/agents/uikit-guide.md`). Consult it before creating any new widget.

## Permissions Pattern

`TaigaPermission` enum defines all Taiga project permissions (VIEW_*, ADD_*, MODIFY_*, COMMENT_*, DELETE_* for each entity type).

**Extension functions** in `ProjectPermissions.kt` on `ImmutableList<TaigaPermission>`:
```kotlin
permissions.canAddEpic()      // checks ADD_EPIC
permissions.canModifyTask()   // checks MODIFY_TASK
permissions.hasPermission(TaigaPermission.COMMENT_US)
```

**UI behavior:** When permission is false, **hide** the action (don't show disabled buttons). Map permission checks to boolean fields in state, set from the permissions list in ViewModel init.

## Offline State Pattern

Use `LocalOfflineState` (from `uikit`) to disable write actions when offline.

**Key difference from permissions:**

- No permission → **hide** action (user can never do this)
- Offline → **disable** action (user can do this, just not right now)

Read offline state with `val isOffline = LocalOfflineState.current`, then pass it to uikit widgets (`AddButtonWidget`, `CreateCommentBar`, `DropdownSelector`, `TopBarActionIconButton`, etc.) which disable themselves when `isOffline = true`.

## Testing

For writing new KMP tests, creating fakes, or understanding test patterns, use the **testing** subagent (`.claude/agents/testing.md`). It knows the full fake inventory, model factories, test utilities, and patterns.

- `:testing` module has utilities: `getRandomString()`, `MainDispatcherRule`, fake generators
- `kotlin.test` assertions + hand-written fakes — no MockK in `commonTest`
- Test dependencies added automatically via convention plugins
- **The apparent `:testing` ↔ module dependency cycle is not a problem.** `:testing` `api`-depends on
  ~40 modules (`core:domain`, `core:storage`, every `feature/*`), and the convention plugin puts
  `:testing` on *every* module's `commonTest`. So `:core:domain:commonTest` → `:testing` →
  `:core:domain:commonMain` looks circular and Gradle resolves it fine — the test and main source
  sets are separate compilations. Verified for `core/domain` (2026-08-05); no build-file change was
  needed to add its first test.
- `docs/testing/` — [survey.md](docs/testing/survey.md) (what exists) and
  [improvement-plan.md](docs/testing/improvement-plan.md) (sequenced tasks, one per session)

**CI runs `./gradlew jvmTest`, then `koverXmlReport`, then `:koverVerify`.** The `jvmTest` step exists because
`koverXmlReport` only runs tests for modules aggregated in the root `build.gradle.kts` `kover {}`
block — `:testing`, `:uikit` and `:tools:*` are excluded, so before that step a test written there
would have been silently skipped forever. Two consequences:

- A new test only runs in CI if its module has a **`jvmTest`** task. `tools/seed` and `tools/utils`
  are `kotlin("jvm")` modules whose task is `test`, not `jvmTest` — a test added there needs its own
  workflow step.
- **Do not add tests under `src/test/`.** KMP tests go in `commonTest` (or `jvmTest` for
  JVM-specific behaviour). The repo has no Android unit-test source set at all, by design; nothing
  in CI would execute one.

**The coverage floor is a ratchet: raise it, never lower it.** `:koverVerify` enforces line ≥ 58 %
and branch ≥ 38 % (root `build.gradle.kts`, `total { verify { } }`). A PR that breaches it needs
tests, not a smaller bound. The traps when touching those numbers:

- **`koverVerify` and `koverXmlReport` can report different coverage** — up to ~5 points apart on the
  same artifacts ([revisit #8](docs/revisit.md)). The bounds are tuned to **`./gradlew :koverVerify`**;
  never set one from the Codecov dashboard. But the divergence is **not** intrinsic to the two tasks:
  when the XML lands on the 742-class side, `:koverVerify` and `kover-rank.py`'s filtered totals agree
  to four decimal places (75.4249 / 60.5173 vs. 75.42 / 60.52, measured 2026-08-03). The gap is the
  class-count flip below, not a second mechanism — so **`kover-rank.py`'s output *is* the gate number**,
  whichever side of the flip your report landed on.
- **To read `:koverVerify`'s own percentages, temporarily set both `minValue`s to 99** in the root
  `build.gradle.kts` and run it: it names each violated rule and prints the actual figure. There is no
  other way to get the number the gate is actually comparing against. `git checkout build.gradle.kts`
  afterwards.
- **A moved percentage is not a moved numerator.** Kover's totals here shift when the denominator
  changes, so compare `covered`/`total` counts between reports before concluding coverage regressed.
  Reading percentages alone once made ~100 new tests look like a 2-point *drop*.
- **A before/after comparison is only valid between equally fresh runs.** `koverXmlReport` reports
  on whichever test tasks actually executed, so a baseline taken from a mostly-`UP-TO-DATE` build
  instruments a different class universe than the after-run. One such pair differed by 855 lines and
  152 branches in the *totals*, in packages the change never touched. Compare at **package scope**,
  where you can see the denominator is identical, and treat a moved total denominator as a signal
  the two runs aren't comparable rather than as a result.
  **That rule is about report-level totals and does not extend to a single class or package.** An
  unexecuted class is reported with *fewer* branches than it has, so covering it grows its own
  denominator: `TaigaSessionStorageImpl` went BRANCH 0/4 → **14/14** and LINE 29/49 → **55/55**
  between two same-mode, same-742-class runs. Compare `covered` against the **after** denominator at
  class scope, and don't read the growth as a bad measurement.
- **Concretely: `koverXmlReport` flips between a "high class count" and a "low class count" mode, and
  which one you get is not predictable.** Observed values so far: **821** (62.00 % line / 43.49 %
  branch), **854** (62.70 % / 43.24 %) and **742** (71.96–72.07 % / 50.37–50.51 %). The high counts
  are the runs where the `excludes` block is **not** applied — they leak in `core/storage/db/dao`
  (+1182 lines alone), `core/storage/db|di|cache|network`, the Ktor plugins in `core/api` and every
  `*Widget`/`*Screen` in `feature/*/ui`. 742 is the run where `excludes` is applied in full, and is
  what CI sees. A build-script edit was once found by bisection to flip it, but that is **not** the
  whole trigger: on 2026-08-03 a clean tree gave 742 and adding only *test sources* gave 854, i.e.
  the opposite direction with no build file touched. Treat the mechanism as unknown. That direction
  reproduced on 2026-08-04 with different numbers — baseline **781** (zero leaks) → after adding only
  a test file and one `:testing` field, **822** with 20 leaks — so a test-sources-only edit flipping a
  clean run *into* the excludes-skipped mode is the most reliably observed transition. Expect the
  before/after pair to straddle the flip and plan on the package-scope escape hatch below.
  Confirmed a third time on 2026-08-04, from the cleanest possible starting point: a **742** baseline
  (zero leaks, i.e. exactly what CI sees) → **822** with 20 leaks after adding two test files and four
  `:testing` fields. **But the direction is not a rule — it reversed on the very next session** (also
  2026-08-04): a *clean tree* gave an **822** baseline with 20 leaks, and after adding one test file
  and three `:testing` fields the after-run was a clean **742** with zero leaks. So neither the
  starting mode nor the effect of adding test sources is predictable; only the *straddle* is the
  expected case. Plan on the package-scope escape hatch below and do not assume which side either run
  will land on.
  **Candidate discriminator (hypothesis, 9 supporting sessions, 0 counter-examples since it was
  noticed): crossing *into* the leaky excludes-skipped mode has only ever been seen alongside a change
  to `:testing`'s own sources.** Sessions that added *only* test files under a feature/core module
  stayed zero-leak — `feature/epics/ui/details`, `feature/issues/ui/details`, `core/domain` and
  `feature/userstories/mapper` each
  ran 742 → 742, and `feature/workitem/ui/mappers` went 780 → 742, i.e. it moved between the two
  *zero-leak* counts without ever reaching 822/854. Every recorded 822/854 flip above involved adding
  `:testing` fields — including `feature/sprint/data` on 2026-08-05, which added a `:testing` source
  file plus fake fields and went 742/0 leaks → **823/20 leaks**, and
  `feature/settings/ui/projectdetails` the same day, which added one recorder to `FakeProjectsRepository`
  and went 742/0 → **849/20**. This is **not** established — the 2026-08-03 note says "adding only test sources
  gave 854", and it is not known whether that session also touched `:testing` — so keep taking the
  before/after diff. **The hypothesis constrains one direction only: it does *not* predict a
  comparable pair when you leave `:testing` alone.** `feature/tasks/ui` (2026-08-05) touched exactly
  one test file, and its pair still straddled — an 823/20-leak baseline (inherited on-disk from the
  previous session) against a clean 742/0 after-run, with 364 classes present in only one report.
  `feature/filters/domain` (2026-08-05) reproduced that exactly: one test file, no `:testing` edit,
  an 827/20-leak inherited baseline → clean 742/0, 385 keys in one report only. So a straddle with
  `:testing` untouched is now the *expected* case, seen twice, and is not evidence against the
  hypothesis — which only ever claimed the leaky direction.
  Plan on the straddle and the package-scope escape hatch regardless of what you touched; the
  all-counter diff below is what makes such a pair usable. It is only *unpredictable*, though, not
  hopeless — `feature/filters/mapper` (2026-08-05) also touched exactly one test file and got a
  perfectly comparable 742/0 → 742/0 pair, with a zero-length key-set difference and zero
  denominator changes anywhere in the report. `feature/userstories/mapper` (2026-08-05) repeated that
  exactly — three consecutive sessions have now had a *free* baseline (the on-disk `report.xml` from
  the previous session, clean tree, same commit) and a provably comparable pair.
  `feature/workitem/ui/screens/editdescription` (2026-08-05) made it four: same free 742/0 baseline,
  742/0 after-run, zero-length key-set difference, zero denominator changes, and movement confined to
  the target package. `feature/userstories/ui` (2026-08-05, task 9c) made it five, and additionally
  stayed on the 742/0 side across **three** `koverXmlReport` runs in one session as test sources were
  added between them. So the free baseline is the *normal* case on a clean tree, and a straddle is
  what needs explaining — not the other way round.
- **A high class count does not by itself mean the `excludes` were skipped — there are at least two
  high modes.** On 2026-08-04 a run gave **787** classes with the `excludes` applied *in full* (zero
  `*Screen` / `*Widget` / `*Plugin` classes in the report); it exceeded a 742 run by 45
  **Android-variant / Room-generated** classes — `*_Impl`, `TaigaDB_Impl`,
  `core/storage/db/entities/*`, `StorageModule_androidKt`, `NetworkMonitorImpl`. Tell the modes apart
  in one command before reaching for any of the advice above:

  ```bash
  python3 -c "
  import xml.etree.ElementTree as ET
  n=[c.get('name') for p in ET.parse('build/reports/kover/report.xml').getroot().findall('package') for c in p.findall('class')]
  print(len(n), 'excluded-suffix leaks:', len([x for x in n if x.split('/')[-1].split('\$')[0].endswith(('Screen','Widget','Plugin'))]))"
  ```

  Zero leaks means the `excludes` ran and the surplus is variant artifacts; a non-zero count is the
  821/854 mode. Two 742-side runs also differ by ±2 classes (Koin-generated `LoginDataModule`, both
  lines covered, BRANCH identical) — that much wobble is noise, not a mode.
  **The raw count in this mode is not a fixed number** — a second run on 2026-08-04 gave **781**, and
  it is the same mode: zero leaks, `kover-rank.py` filters it to the same 745 classes, BRANCH 2049 and
  LINE 9762 identical to the 787 run's. Recognise the mode by *zero leaks plus a 745 filtered count*,
  not by matching 787. A third value, **798**, was seen on 2026-08-05.
  **The mode is not sticky within a session, so re-running is worth one attempt before you reach for
  the escape hatches.** That 798 run and a clean **742** were consecutive `koverXmlReport`
  invocations minutes apart, with only two more test methods added in between — the second one
  matched the 742-class baseline exactly, giving identical key sets and zero denominator changes in
  the all-counter diff. Cheaper than reasoning about a straddle, and it costs one command.
- **In the 787/781 mode, `kover-rank.py` normalises BRANCH exactly but leaves LINE ~53 lines high.** It
  filtered that report to 745 classes, not 742: the three `core.storage.db.entities` classes survive
  because the root `excludes` `packages(...)` list does not name `…db.entities` either, so the script
  is faithfully in sync and the 742 run simply never contained them. The BRANCH denominator was 2049
  in both, i.e. identical to the gate; only LINE was inflated. So the "`kover-rank.py`'s output *is*
  the gate number" claim above holds **for branch coverage**; check the LINE denominator against a
  known 742 run before quoting a line figure. Package-scope tables are unaffected either way.
- **One thing that is *not* the flip trigger:** re-running `koverXmlReport` after touching only
  `:testing`'s `commonMain` (which does recompile its `androidMain`) gave **744**, not 787 — so the
  Android-artifact regeneration that coincided with the 787 run does not reproduce it on its own.
  Recorded so the experiment is not repeated.
- **Do not fight the flip — re-apply the `excludes` yourself with
  [docs/testing/kover-rank.py](docs/testing/kover-rank.py).** It filters whatever report you have by
  the same suffix/package rules as the root `build.gradle.kts`, prints the kept class count and the
  filtered totals, and ranks packages by missed branches. On 2026-08-03 it reduced an 854-class
  report to 742 classes and reproduced a genuine 742-class run's totals to the digit, so which side
  of the flip you landed on stops mattering. **Use it for every coverage figure you record and for
  every ranking of what to test next**; the raw report is only trustworthy when it happens to say
  742, and you cannot make it say that on purpose. Keep the script's lists in sync when the
  `excludes` block changes.
- **When two reports still disagree, diff their per-package denominators rather than discarding the
  measurement.** The package you care about usually has an *identical* denominator in both, which
  makes a before/after table valid anyway — this is how the `feature/projects/data` and
  `feature/kanban/ui` tables in [improvement-plan.md](docs/testing/improvement-plan.md) survived
  742-vs-854 and 744-vs-854 pairs. To confirm a delta is caused by the change rather than by build
  staleness, `git stash -u` and re-run: a clean-tree re-run that reproduces the baseline to the digit
  settles it.
  **Better than checking the one package you care about: diff *every* counter in both reports at once
  and assert nothing else moved.** Load `{(name, type): (covered, total)}` for every `<package>` *and*
  `<class>` from each report, then print the entries whose values differ, the entries whose
  *denominator* differs, and the size of the symmetric difference of the key sets. On 2026-08-05 that
  came back as "only `feature/issues/ui/details` moved, zero denominator changes, zero classes in one
  report but not the other" — which turns a before/after table from *probably* comparable into
  provably so, and simultaneously catches side effects in packages you would never have thought to
  look at (the `feature/userstories/ui` session moved two `feature/workitem/ui` rows). It costs one
  command and subsumes the per-package check above. **Use
  [docs/testing/kover-diff.py](docs/testing/kover-diff.py)** (`python3 docs/testing/kover-diff.py
  before.xml after.xml`) rather than retyping this snippet — added 2026-08-06 after writing it out
  ad hoc often enough that it was worth saving next to `kover-rank.py`.
  **It is also what rescues a *straddled* pair, so run it before discarding one.** In
  `feature/tasks/ui` the two reports differed by 364 classes, and the same diff showed the target
  package's own denominators (BRANCH 30, LINE 479, CLASS 31) identical in both with no class of that
  package missing from either — which makes the row provably valid even though the totals are not
  comparable at all. Read the key-set difference and the per-target denominators as two separate
  answers; only the second one gates your table.
  **Split the "moved" list by element type — mode-flip noise is `<package>`-level *only*.** When the
  flip drops leaked classes, they leave the key set entirely rather than moving, so every counter it
  disturbs is a package total; a `<class>` row that moves is real. `feature/filters/domain`
  (2026-08-05) straddled 827/20-leak → 742/0 and the diff showed 28 changed package denominators
  across `core/api`, `core/domain`, `feature/login/ui`, `feature/wiki/ui/*` — and **zero class-level
  movement outside the target package**. That last count is the isolation proof: it says the change
  touched nothing else *anywhere*, which checking your own package's denominators cannot. Print the
  two levels separately and read a non-empty class-level list as the only thing worth explaining.
- **`koverXmlReport` always writes `build/reports/kover/report.xml`.** Copy it to a distinct path
  immediately after each run. Forgetting once makes the "before" and "after" the same file, and the
  diff comes back showing nothing changed anywhere — which reads like a plausible result, not like a
  mistake. Equally, **do not write test sources while a baseline run is in flight** — the run compiles
  test sources partway through, so a file added at the wrong moment silently lands in the "before".
  Cheap check either way: confirm the baseline reports the *pre-change* figure for the class you are
  about to test before trusting it.
  **On a clean tree at the same commit as the last session's final run, you do not need a baseline
  run at all** — `koverXmlReport` comes back `UP-TO-DATE` and the `report.xml` already on disk *is*
  the baseline. Copy it aside and run the class-count/leak check on it as usual; that plus the
  pre-change-figure check is the same evidence a fresh run would give, for free. Do this before
  writing any test, since the first test source you add is what makes the task out-of-date.
- **A class excluded by name shows no movement however well you test it.** The `excludes` block
  filters by suffix — `**.*Plugin`, `**.*Module`, `**.*Repository`, `**.*Api`, `**.*Screen` … — which
  in `core/api` drops all five Ktor plugins, i.e. ~98 lines and 38 branches of real auth and
  error-mapping logic. Check the exclusion list before ranking a package by its missed branches, and
  before reading a flat delta as "the tests did nothing" ([revisit #10](docs/revisit.md)).
  The suffix match is exact, so the reverse also holds: `**.*Repository` does **not** match
  `…RepositoryImpl`, and every repository impl in the project is measured normally.
  `**.*Exception` is on that list too, which hides real logic in `core/domain`
  (`NetworkException.message`, and every custom exception in the module), and
  `**.*ResultExtensionKt` is named explicitly.
  **`**.*DTO` is on it as well, so a `…/dto` package's report rows are its *non*-`DTO`-suffixed
  classes only** — `feature/userstories/dto` shows `BulkUpdateKanbanOrderRequest` and
  `CreateUserStoryRequest` at BRANCH 0/38 while `UserStoryShortInfoDTO`, which
  `UserStoryShortInfoMapperTest` covers thoroughly, has no row at all. Don't read a `dto` package's
  ranking as a statement about its DTOs.
  **An excluded class is absent from the report entirely — not listed at 0 % — so a class that is
  both excluded *and* dead is invisible to every coverage-driven ranking.** `SprintPagingSource`
  (`**.*PagingSource`) has zero references repo-wide and no report row of any kind; nothing in a
  missed-branch sweep could ever have surfaced it ([revisit #21](docs/revisit.md)). When a sweep
  closes a package, `ls` its source directory against the class names in the report before calling
  the package done — the difference is the excluded set, and it is worth a look.
- **A `*_androidKt` / `*_iosKt` class in the report is dead weight, and it can dominate a sweep row.**
  Android- and iOS-variant classes get compiled into the report, but CI runs `jvmTest` only and the
  repo has no Android unit-test source set by design — so they sit at 0 % forever. In `core/domain`
  that is **14 of the package's 16 missed branches**, all in `PlatformNetworkErrorMapper_androidKt`,
  whose JVM twin `PlatformNetworkErrorMapper_jvmKt` is **byte-for-byte identical** and already 14/14
  BRANCH / 10/10 LINE (covered incidentally by `core/api`'s `NetworkErrorMapper` tests). The logic is
  not untested; it is counted twice and only one copy is executable. **Diff the actuals before
  scoping any `expect`/`actual` package** — a `*_androidKt` row is a reason to close the row, not to
  write tests.
- **Much of the branch denominator is unreachable**, in two distinct ways, and a package's
  missed-branch count distinguishes neither. *Generated:* `equals`/`hashCode`/`copy$default` on data
  classes and Room DAO impls — `feature/filters/domain/model` is 2/144 across nine files with no
  hand-written conditional in them. **`@Serializable` serializers are *not* in this category**,
  though: they are reached by any test that serializes the type, wherever it lives. Round-tripping
  `FiltersData` through `Json` inside `FiltersStorageImplTest` (a different module) took that same
  package from 2/144 to **39/144** with no test written against it. So don't quote a
  `@Serializable`-heavy package's missed branches as unreachable — but don't take it as a sweep
  target either, since the reachable share moves as a side effect of testing its callers.
  *Composition-blocked:* hand-written
  branches inside `@Composable` functions and `@Composable get()` properties, which no plain JVM test
  can enter — `utils/ui` left 46 such branches and the whole `main` package is 31 of 35 (`MainAppState`
  is `@Composable` getters; `MainViewModel` is already 4/4). Rank work by missed branches in
  hand-written, *non-composable* code. A third, much smaller kind is **`x?.toString() ?: ""`**, which
  is always 3/4: the safe call contributes two branches and the elvis two, but `toString()` on a
  non-null receiver never returns null, so the elvis's null arm is dead on that path. Seen twice —
  `WorkItemCustomFieldsDelegateImpl` and `ModulesViewModel` lines 59–60 — so recognise it rather than
  hunting for the test. Same family, same one-short result: a `?.`-chain whose last link cannot
  return null feeding an elvis, e.g. `FiltersStorageImpl:33`
  `value?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: FiltersData()` at 7/8.
  **The elvis is not required** — any `?.`-chain with a link that cannot return null is one short,
  e.g. `UserStoryDetailsViewModel:823-824`
  `currentUserStory?.userStoryEpics?.map { it.id }?.toImmutableList()` at 3/4, because
  `userStoryEpics` is a non-null `ImmutableList` and `map` never returns null. Recognise the shape,
  not the elvis. **Its report signature is `mb=1 cb=3` on a `?.`-chain line**, and a getter declared
  `get() = x as? T ?: error(...)` is a reliable producer of it — `error()` returns `Nothing`, so the
  property is typed non-null and every `field.value?.stringValue ?: ""` reading it is 3/4 forever.
  That one getter, `CustomFieldValue.stringValue`, accounts for all 8 residual branches in
  `feature/workitem/ui/mappers`. When the same line shape is one short in several places at once,
  look for a shared non-null-typed callee rather than testing each site.
- **The same is true of LINE for every `logcat { }` call site** — 96 of them. The JVM backend is the
  no-op `NoLog` (see Logging), which never invokes the `message: () -> String` lambda, so each one is
  a synthetic method Kover reports as one missed line and zero branches. **Signature to recognise: a
  1-line hole in an otherwise 100 % method.** Stop there rather than hunting for the test that would
  reach it. Also unreachable in the same way: the default value of a state class's callback parameter
  (`onSaveClick: (String, Color) -> Unit = { _, _ -> }`), which the ViewModel always overrides.
  [revisit #16](docs/revisit.md) has the fix if it is ever judged worth the ~96 lines.
  **This is not a ceiling on LINE, though** — whether the lambda becomes its own synthetic method
  varies. `EditSprintViewModel`'s `logcat` inside a `viewModelScope.launch` was split out at 0/1, and
  `ModulesViewModel`'s two were folded into the covered `invokeSuspend`, taking that package to LINE
  88/88. So "1-line hole → stop" is the right rule, but "100 % LINE is impossible here" is not.
- **`onCleared()` is unreachable from a unit test, and every details ViewModel has one.**
  `ViewModel.onCleared` is `protected` and `ViewModel.clear()` is internal to `lifecycle-viewmodel`,
  so nothing in `commonTest` can trigger it. In `UserStoryDetailsViewModel` its body is 6 of the 10
  residual lines after the package was otherwise closed. Recognise the override and skip it — same
  family as the `logcat` holes, just bigger.
- **A low missed-branch row can still be the best session available — look for the "sleeper"
  signature: a `$1`/`$2` coroutine-body class at BRANCH 0/n *and* LINE 0/m.** That pairing means a
  whole `viewModelScope.launch` body has never executed, so the branch number only prices the
  `resultOf` `onSuccess`/`onFailure` arms while the real prize is the untested body around them.
  `feature/settings/ui/projectdetails` ranked 0/8 — below a dozen bigger-looking rows — and closing it
  took the package to **100 % on every counter**, buying 47 lines, 9 methods and 547 instructions.
  **Rank such a row by its LINE gap, not by `missedB`**; `kover-rank.py` prints both columns for
  exactly this reason. The inverse of the mapper heuristic below, and it points at the same thing: a
  row whose LINE is also short is the cheap, high-yield kind.
  It repeated on `feature/workitem/ui/screens/editdescription` (2026-08-05, 0/4 BRANCH but LINE
  6/36 → **100 % on every counter**), and that session added a cheap way to *find* the sleeper's
  test: **when a sweep row is a `screens/<x>` sibling of an already-tested `screens/<y>`, read the
  sibling's test before writing anything.** `EditDescriptionViewModel` is a strictly smaller
  `EditSprintViewModel` — same `SavedStateHandle` + `WorkItemEditStateRepository` constructor, same
  `onGoingBack` / `setIsDialogVisible` / rendezvous-channel shape — so `EditSprintViewModelTest`
  transferred almost verbatim, including the `launch { … take(1) }` collector that a rendezvous
  channel requires. `WorkItemEditStateRepository` needs no fake: it is a plain in-memory class, and
  these tests construct the real one.
- **As of 2026-08-05 the *branch* sweep (task 9a) is out of worthwhile rows** — every remaining high
  row is generated `equals`/`hashCode`, a `@Serializable` serializer, `@Composable`-blocked, or dead
  `*_androidKt` weight. Rank what is left by **missed lines on never-executed classes** instead; the
  query and the resulting backlog are in
  [improvement-plan.md](docs/testing/improvement-plan.md#where-9a-stands-2026-08-05). Don't re-derive
  the exhausted ranking — that table names what each misleading row actually is.
- **Get the per-class breakdown before scoping a session around a package**, and the per-**method**
  one before concluding a leftover is real — Kover's XML carries `<counter>` elements on
  `<package>`, `<class>` *and* `<method>`, so `for c in p.findall('class'): for m in c.findall('method')`
  answers "which function still has missed branches" in one command instead of by reading the source.
  That is how `WorkItemCustomFieldsDelegateImpl`'s residual 2/30 was pinned to the `?.` null-checks in
  `valueToUse?.toString()?.toLongOrNull()`, unreachable because `NumberItemState`'s values are
  non-null. The worked snippet is in
  [improvement-plan.md](docs/testing/improvement-plan.md) under `…delegates/customfields`.
- **When the per-method breakdown is still too coarse, go to the `<sourcefile>` element** — its
  `<line>` children carry `nr`, `mb` (missed branches) and `cb` (covered), which names the *source
  line*. A whole coroutine body is one `invokeSuspend` method, so "`invokeSuspend` 10/12" is as much
  as the per-method view can say; the per-line view says "line 59 `mb=1 cb=3`" and the question is
  answered without reading Kotlin. The snippet is in
  [improvement-plan.md](docs/testing/improvement-plan.md) under `…settings/ui/modules`.
  **Run it at scoping time, not only to explain a leftover.** Dumping every `mb>0` line for the
  target class *before writing any test* turns a large ViewModel from an exploration into a
  checklist — `feature/userstories/ui`'s 27 missed branches resolved to 15 named source lines in one
  command, and each test was then written against a known target. It also prices the session
  honestly: that dump is what showed the line half was branch-free and had to be split off.
  **Read the `mb`/`cb` split, not just `mb>0`** — it says which lines are worth a test before you
  write one. On a `?.`-chain, `mb=2 cb=2` is a missing test (one input never tried, +1 branch each);
  `mb=1 cb=3` is the dead arm above and buys nothing. In `feature/workitem/ui/mappers` that split
  predicted the exact final figure — 41/56 → **48/56**, four tests — before any test was written.
  `mb=2 cb=0` means the line was **never executed at all**, so it needs *two* tests, not one — an
  `x?.invoke()` callback site wants both the present and the null case. Sizing
  `WorkItemSprintDelegateImpl` that way (two lines at `mb=2 cb=0`, eight at `mb=1 cb=1`) predicted
  ten tests and 40/40, and both were exact.
  **A high `mb` on one line does not price as many tests** — `mb=4 cb=2` on a
  `find { … } ?: return null` line means the `find` *lambda* was executed (by some other method's
  test walking the same collection) while this method's own elvis never was, so one happy-path test
  closes all four. In `StatusesMapper`, `getSeverity`/`getPriority` each showed `mb=1 cb=1` on their
  first guard, `mb=4 cb=2` on the `find` line, and `mb=0 mi>0` on the whole constructor body below
  it — the signature of *a method never called at all*, which prices as three tests (happy path,
  null id, no match), not as six. Read the block of lines together, not line by line.
  **The general rule behind both: `mb` counts branches, and one expression's branches are usually
  driven by a single input — so price a line by how many distinct *values* it can take, not by its
  `mb`.** `UserStoryMapper:64` `resp.fromTaskRef?.isNotEmpty() == true` is `mb=5 cb=1`: six branches
  (the safe call, the `isNotEmpty` test, the boxed `== true` comparison) fed by one three-valued
  `String?`, so three tests — null / empty / non-empty — close all five. Any
  `x?.someBooleanCall() == true` has this shape.
  **Then re-run the dump for missed *lines* (`mi>0`) once the branch tests are green** — a
  missed-branch ranking is structurally blind to branch-free code, so one-line functions are
  invisible to it however untested they are. `WorkItemSprintDelegateImpl` sat at LINE 138/144 after
  reaching 40/40 BRANCH, and four of the six leftovers were one-line private callbacks
  (`onStartDateDismissRequest` and friends) that two more tests closed. Expect this on any delegate
  whose public surface is a state object full of callbacks; what is left after that is the `logcat`
  1-line holes, which are the signal to stop.
  **Filter that dump on `ci=0`, not on `mi>0`** — a `<line>` carries both, and Kover counts the line
  *covered* if `ci>0`, so an `mi>0 ci>0` line is a partially-covered expression that no test can
  move off the list. `UserStoryDetailsViewModel:199`
  `get() = requireNotNull(_state.value.currentUserStory)` reports `mi=9 ci=7 mb=1 cb=1` and is
  executed by nearly every test in the file; filtering on `mi>0` puts it top of a list of "untested"
  lines. Only `ci=0` lines are the never-executed ones the LINE counter is actually missing.
- **On a `*Mapper`, a block of `mb=0 mi>0` lines is usually one collection lambda starved by a
  hard-coded `null` in a `:testing` factory** — not a missing test *shape*, just a missing input.
  `getWorkItemResponseDTO()` sets `epics = null`, which left all six lines of
  `UserStoryMapper.epicsToDomain`'s body unexecuted; one `.copy(epics = listOf(…))` test closed the
  whole LINE gap. So **a `*Mapper` row whose LINE is *also* short is the cheap, high-yield kind** —
  check the factory's defaults before scoping it. The inverse holds too: a `*Mapper` at full LINE
  with residual branches is the `mb=1 cb=3` unreachable kind (`feature/issues/mapper`,
  `feature/workitem/domain/customfield`). Two consecutive pure-mapper rows — `feature/filters/mapper`
  and `feature/userstories/mapper` — went to 100 % on every counter in well under an hour each,
  because a pure mapper has no `logcat`, no coroutine and no collaborator that can throw.
  **A file of pure top-level extension functions is the same row type and is even cheaper** —
  `feature/filters/domain` is one `Utils.kt` of three `List<T>` extensions, and 7 tests took the
  package 0/10 → **10/10 BRANCH, 6/6 LINE, 3/3 METHOD, 83/83 INSTRUCTION** in minutes. Recognise it
  by a whole package at LINE 0/n with a `…Kt` class name: no class to construct, no fake to wire, so
  the only work is enumerating each expression's input values.

Qualify the task as **`:koverVerify`** — the bare name also runs the rule-less `koverVerify` in all
77 modules.

**Verify with the full `./gradlew jvmTest`, not just the module's own task.** All modules' JVM tests
share a process, and `kotlinx-coroutines-test` registers a `ServiceLoader`-global
`CoroutineExceptionHandler` — so an exception escaping a coroutine in *any* test is reported against
whichever `runTest` happens to be live, in a different module. `:feature:x:jvmTest` passing proves
your test works; only the full run proves it did not break someone else's. When a failure appears
alongside your change, A/B it against a clean tree (`git stash -u`) before assuming you caused it.

**Run `ktlintCheck` too — a green `jvmTest` says nothing about it.** The rule that catches new test
code is `standard:function-signature`: a signature written across multiple lines fails if it *would
fit* on one within the 120-char limit. It bit two consecutive sessions on the same construct — a
`setupSuccessfulLoad(data: XDetailsData = getXDetailsData(...))` default-argument helper at ~106
characters, which reads as over-long and is nonetheless required to be one line. Dropping argument
names in the nested factory calls is what keeps it under the limit.

**Every `XApi` is an `interface XApi` + `@Single(binds = [XApi::class]) class XApiImpl`** — no
exceptions, so any API can be faked in `:testing`. `WikiApi` was the last concrete one and was split
in the course of testing it.

**Failure-path convention: every public method of a repository impl, use case or ViewModel gets a
test where a collaborator throws `testException`.** This is what closes the ~20-point line-vs-branch
coverage gap — happy-path-only tests walk through a function without ever taking its `catch`, its
`?:` or its `if`. Assert with `assertFailsWithTestException { }` (`:testing`, `TestUtils.kt`), not a
bare `assertFailsWith<IllegalStateException>`: `testException` *is* an `IllegalStateException`, and
so is a fake's own `error("… not set")` guard, so a bare type check passes when the test never
reached the code it claims to cover. It also matches by message rather than identity, which a throw
from inside an `async` child requires. Any fake the test touches needs a `…Throws` hook; add one
while you are in the file even if this test doesn't use it.

A method that *swallows* failures is the same convention pointed the other way: assert the fallback.
`WorkItemRepositoryImpl.getWorkItems` catches API errors and reads the Room cache, so its
failure-path test asserts the cache was read, not that anything was thrown.

**Testing `expect`/`actual` code: prefer the platform whose actual is real over stubbing one out.**
JVM is a fully supported target here — desktop runs the app for real — so `jvmTest` can exercise
platform-backed code (Room, DataStore, Ktor/OkHttp) with nothing faked, which `commonTest` cannot.
Put such a test in `jvmTest` and **state in the test file which platform's behaviour is being
asserted**, because the Android and iOS actuals stay uncovered. `KoinGraphTest`
(`composeApp/src/jvmTest/`) is the worked example — see [docs/koin/koin-graph-test.md](docs/koin/koin-graph-test.md),
including the wiring it deliberately cannot check. `KotlinxDateTimeFormatterJvmTest` is the smaller
one: `commonTest` proves the delegation happens, `jvmTest` proves what the JVM actual produces.

Where behaviour depends on the environment (time zone, locale), **assert a fixed expectation rather
than mutating the global default** — and if you claim a test passes under a changed environment,
prove the change reached the forked test JVM. `TZ` does; `LANG`, `LC_ALL` and `JAVA_TOOL_OPTIONS`
do not. The `testing` agent's gotcha 11 has the details.

## Skills & Agents

`.claude/agents/` in this repo holds only the project-specific agents: **testing** and
**uikit-guide**. Everything else is wired up per-machine, not per-clone.

**Shared skills and agents** live in the `agentic-grappim` repo (`~/proj/grappim/agentic-grappim/`),
symlinked into `~/.claude/skills/` and `~/.claude/agents/`. The **koin-expert** agent comes from
there — it used to be duplicated in this repo, and the in-repo copy went stale while shadowing the
real one, so it was removed. Edits to any of these land in `agentic-grappim` and must be committed
there; changing them affects every project on this machine.

| Skill | Description |
|-------|-------------|
| `finalize` | Capture what a session learned into CLAUDE.md, memory, and shared-skill proposals |
| `investigate-issue` | Root-cause a reported bug and write it up under `docs/issues/` before fixing |
| `update-gradle-wrapper` | Bump the Gradle wrapper with a real checksum from Gradle's server |

**Android skills** come from the `android-skills` plugin, invoked as `android-skills:<name>`.
Relevant ones here: `navigation-3`, `edge-to-edge`, `adaptive`, `agp-9-upgrade` (note: that one
does not cover KMP — see `docs/build/agp9-kmp.md`), `r8-analyzer`, `perfetto-trace-analysis`.

## Coding Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.
- Don't add UI elements or navigation that weren't asked for — if asked to create a settings screen, don't add a settings button to other screens unless explicitly requested.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

**When you notice a real problem outside the current task: write it into
[docs/revisit.md](docs/revisit.md) and keep going.** Not fixed inline (it makes the diff
unreviewable), not dropped, not just mentioned in chat — chat is not persistence. Give the entry
enough evidence (`file:line`, or a link to an issue doc) that a cold session can act on it without
re-deriving anything.

The test: Every changed line should trace directly to the user's request.

## Logging

`core/logger` is a KMP logging facade — it is added to every KMP module's `commonMain` automatically
by the convention plugin, so `logcat` is always available without a dependency change.

```kotlin
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.logger.LogPriority   // separate import, only if you set a priority

logcat { "plain debug message" }                               // as an Any extension: tag = this::class.simpleName
logcat(tag = "Ktor") { "explicit tag" }                        // top-level overload: tag is null unless given
logcat(LogPriority.ERROR, throwable = e) { "failed to load" }
```

Priorities: `VERBOSE`, `DEBUG` (default), `INFO`, `WARN`, `ERROR`, `ASSERT`.

The message is a lambda, so it isn't built unless a logger is installed. Never call `Timber`
directly outside `core/logger`.

**Backends** — `TaigaLogger.install(...)` is called once per platform entry point:

| Platform | Impl | Installed in |
|----------|------|--------------|
| Android | `TimberLogger` → Timber (`DebugTree` on debug, `CrashlyticsTree` on gplay) | `androidApp/TaigaApp.kt` |
| iOS | `NSLogLogger` → `NSLog`, chunked at 3000 chars to survive its ~4096-byte truncation | `main.ios.kt` |
| Desktop/JVM | **none** — falls back to the `NoLog` no-op, so `logcat` output is silently dropped | — |

## Error Handling

- Never swallow exceptions silently. Every `catch` block must at least log the exception:
  `logcat(LogPriority.ERROR, throwable = e) { "what failed" }`.

## Compose / Platform Rules

- Do not use early returns in Composable functions — use conditional wrapping
- Lambda parameters: present tense (`onClick` not `onClicked`)
- Prefer `kotlinx-collections-immutable` (`ImmutableList`, `persistentListOf()`) over `List`/`MutableList` in state classes and Composable parameters for stable recomposition
- For Composable Previews, use `@PreviewTaigaDarkLight` annotation and wrap content with `TaigaMobilePreviewTheme` (both from `uikit`):

```kotlin
@PreviewTaigaDarkLight
@Composable
private fun MyWidgetPreview() {
    TaigaMobilePreviewTheme {
        MyWidget(...)
    }
}
```

- Settings screens with fixed items use `Column` instead of `LazyColumn` — lazy loading unnecessary when item count is known and small
