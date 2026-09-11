# CLAUDE.md

TaigaMobileNova is an unofficial Kotlin Multiplatform client for Taiga.io targeting Android, iOS, and Desktop. Built with Kotlin, Compose Multiplatform, and follows a modular MVVM + Clean Architecture.

## Keeping this file lean

**A section that grows a worked-example catalogue instead of staying a stable convention has
outgrown this file — split it into its own doc under `docs/` and leave a one-line pointer.** The
Testing section's Kover coverage-sweep heuristics did exactly this over many sessions: the file
reached 717 lines before the catalogue (missed-branch/line ranking heuristics, `mb`/`cb` report
signatures, `kover-rank.py`/`kover-diff.py` usage) moved to
[docs/testing/kover-coverage-heuristics.md](docs/testing/kover-coverage-heuristics.md) (2026-08-09,
[docs/archive/revisit-resolved.md](docs/archive/revisit-resolved.md#28-claudemd-has-grown-too-big-split-the-kover-ranking-heuristics-out-into-their-own-doc)
#28). Watch for the same shape starting again elsewhere: a bullet
accumulating dated, confirmed cases ("Confirmed for X (date)... And for Y (date)...") is reference
material earned by a specific investigation, not a day-to-day rule every session needs to read.

A rule that fits in a sentence or two, with at most one example, stays here. A rule that needs a
worked example, a script snippet, or a running list of confirmed cases belongs in `docs/` — link it
and stop.

## Template Adoption

This file does not use `agentic-grappim/templates/CLAUDE.md.template`'s section names or structure
verbatim — headings like "Coding Guidelines" (template: "Working agreements"), "Settled Decisions"
(template: "Settled decisions"), and no standalone "Reference projects" section. **Decision
(2026-09-05): intentional, not drift.** The template's substance — working agreements, settled
decisions, friction log, close-out ritual, `/finalize` — is already present here section-by-section,
grown organically before the shared template existed; renaming or reorganizing to match it verbatim
would be pure churn with no functional benefit. Treat a future divergence in *content*, not heading
names, as the thing worth reconciling.

## Build Commands

```bash
# Android - build debug APK
# -PgplayBuild is required for Gplay builds: it's what gates the google-services
# and firebase-crashlytics plugins on (androidApp/build.gradle.kts). Omit it and the
# flavor still compiles, but Firebase never initializes — CrashReporterImpl then
# throws "Default FirebaseApp is not initialized" at app startup, release or debug.
./gradlew :androidApp:assembleGplayDebug -PgplayBuild
./gradlew :androidApp:assembleGplayRelease -PgplayBuild
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
./gradlew :koverVerify      # coverage floor (line ≥ 92 %, branch ≥ 78 %) — must be qualified

# Force Koin compiler to re-run (skipped on UP-TO-DATE, which causes "no definition found" crashes)
# Run this before launching from Xcode whenever DI definitions may have changed
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks   # iOS simulator
./gradlew :composeApp:compileKotlinIosArm64 --rerun-tasks            # iOS device
# Android equivalent (also forces Koin compiler logs):
./gradlew :androidApp:compileFdroidDebugKotlin --rerun-tasks

# Render the full module dependency graph (requires graphviz: apt/brew install graphviz)
./gradlew generateProjectDependencyGraph   # → build/reports/dependency-graph/project.png
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

- Kotlin 2.4.x, JDK 21
- Compose Multiplatform with Material Design 3
- Koin (with `io.insert-koin.compiler.plugin` IR/FIR plugin) for DI
- Ktor for networking (OkHttp on Android/JVM, Darwin on iOS)
- Navigation Compose (KMP) with type-safe routes
- Kotlin Serialization for JSON
- Coroutines, Coil 3.x for images (KMP-ready)
- Room 2.8.4 + BundledSQLiteDriver (KMP-ready) — **`RoomDatabase.clearAllTables()` is Android-only**;
  the JVM/native actual doesn't declare it at all (confirmed via `javap` on the `room-runtime`
  artifacts). To clear all tables on JVM/iOS, add a no-arg `deleteAll()` `@Query` to each DAO instead.
- `grappim-kit-logger` — KMP logging facade (see Logging below); Timber backs it on Android only
- The JetBrains AndroidX forks (`org.jetbrains.androidx.lifecycle`, `org.jetbrains.androidx.navigation3`,
  `org.jetbrains.androidx.savedstate`) publish their real per-platform code under the **plain
  upstream group id** (`androidx.lifecycle`, `androidx.navigation3`) with platform classifiers
  (`-desktop`, `-iosarm64`, …) — the `org.jetbrains.androidx.*` coordinate is a thin Gradle Module
  Metadata redirect with no real classes or sources of its own. Downloading a `org.jetbrains.androidx.*`
  sources jar to read the implementation returns an empty `redirectCommonMain/EmptyRedirectRoot.kt`
  stub; look under `androidx.*` instead. Confirmed 2026-08-29 chasing `lifecycle-viewmodel-savedstate`
  and `lifecycle-viewmodel-navigation3` sources.

**Convention Plugins** (in `build-logic/`):

- `taigamobile.android.application` - Android application module (`androidApp`) — applies AGP, Compose, Koin compiler plugin
- `taigamobile.kmp.library` - KMP base (Android + iOS + JVM targets, coroutines, collections)
- `taigamobile.kmp.library.compose` - Adds Compose Multiplatform across all targets; enables `androidResources` for CMP asset pipeline
- `taigamobile.kmp.di` - Applies `io.insert-koin.compiler.plugin` + Koin dependencies
- `taigamobile.kmp.serialization` - Kotlin Serialization setup
- `taigamobile.kmp.network` - Ktor with platform-specific engines (OkHttp / Darwin)
- `taigamobile.kotlin.library` - Pure Kotlin library (no Android/KMP)

## grappim-kit Modules

`core/*` modules are progressively being extracted into `grappim-kit` (sibling repo, shared
across this app, wallosmobile, wayprint, HateItOrRateIt) and swapped for the published Maven
artifact — `core/navigation` → `io.github.grigoriym:grappim-kit-navigation` was the first
(2026-09-08, PR #393). Before wiring a swap like this, diff the published module's actual source
against this repo's current HEAD rather than trusting an extraction's "canonical, mechanical
swap" verdict — an extraction commit can predate a fix that lands here afterward and still get
published stale (confirmed for `navigation` 0.1.0 vs. this repo's `e78fe61b`). See
`grappim-kit/CONSUMING.md`'s `navigation` section for the general gotcha. When the swap deletes
a `core/*` module outright, also drop its `kover(projects.X)` line
from the root `build.gradle.kts` aggregation and re-run `koverXmlReport`/`:koverVerify` — the
floor is enforced against whatever remains aggregated.

`uikit`'s theme/top-bar scaffolding and `NativeText` → `io.github.grigoriym:grappim-kit-uikit`
was the second swap (2026-09-09, PR #394) — unlike `navigation`, no local module was deleted
(`:uikit`/`:utils:ui` keep existing with reduced content), so add the new artifact as `api(...)`
on whichever local module's consumers reach the moved type through same-module resolution, not
`implementation(...)`. **A type moving out of a module breaks any file that referenced it via
same-package implicit resolution with no explicit import** — a repo-wide import-line rename
alone misses those; the build has to actually run to surface them (confirmed:
`utils/ui/PagingUtils.kt` and `SnackbarDelegate.kt` both referenced `NativeText` with no import
since it used to live in their own package). See `grappim-kit/CONSUMING.md`'s `uikit` section
for what diverged from the extraction's own writeup (an added `Surface` wrap in `KitTheme`, an
added top-bar slide animation, and the adapter pattern for a platform-varying `ColorScheme` like
Android's dynamic color, which `KitTheme` has no accommodation for on its own).

`core/logger`'s `Logcat`/`LogPriority`/`TaigaLogger`/`TimberLogger`/`NSLogLogger`/`FileLogger` →
`io.github.grigoriym:grappim-kit-logger` was the third swap (2026-09-09, PR #413) — the local
module was deleted, same as `navigation`. **Unlike `navigation`/`uikit`, check whether the
consuming app wires the old local module in centrally before assuming it's a per-module
`build.gradle.kts` line**: this repo's `build-logic/convention/.../KmpConfiguration.kt` hardcoded
`implementation(project(":core:logger"))` once inside `configureKmp()` (applied to every KMP
library module), not 40-odd separate `build.gradle.kts` additions — the swap there was a single
line to `libs.grappim.kit.logger`. `androidApp` (a plain Android application module that never
goes through `configureKmp()`) still needed its own explicit dependency line update, same as any
consumer.

## Navigation Pattern

Navigation 3 (`core/navigation`'s hand-rolled `Navigator`/`NavigationState`, ported from
wallosmobile — see `docs/architecture/tablet-form-factor-support/IMPLEMENTATION_PLAN.md`'s step 7
and step 10 notes for why this is hand-rolled rather than depending on the real Nav3 alpha APIs).
Destinations are `@Serializable` classes/objects implementing `NavKey`, each with a `Navigator`
extension function next to it:
```kotlin
@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long) : NavKey

fun Navigator.navigateToTask(taskId: Long, ref: Long) {
    navigate(TaskDetailsNavDestination(taskId, ref))
}
```

Each `composeApp/.../nav/*NavGraph.kt` file wires one feature's destinations as
`EntryProviderScope<NavKey>` extensions, called from `MainNavHost.kt`'s single `entryProvider { }`
block:
```kotlin
fun EntryProviderScope<NavKey>.taskNavGraph(navigator: Navigator) {
    entry<TaskDetailsNavDestination> { route ->
        TaskDetailsScreen(route = route, goBack = { navigator.goBack() }, ...)
    }
}
```

ViewModels receive the route via a Koin `@InjectedParam` constructor parameter — not
`SavedStateHandle.toRoute<T>()`, which was Nav2-only and has no Nav3 equivalent:
```kotlin
@KoinViewModel
class TaskDetailsViewModel(
    @InjectedParam private val route: TaskDetailsNavDestination,
    ...
) : ViewModel() {
    private val taskId: Long = route.taskId
```
passed at the call site as `koinViewModel { parametersOf(route) }` in the `Screen` composable.

**`Navigator.navigate(key)` is single-top per top-level section, not a stack push everywhere.** A
top-level key (one of the drawer sections, seeded in `MainAppState.kt`'s `TOP_LEVEL_KEYS`) gets its
own independent sub-stack; navigating to a non-top-level key pushes onto whichever section's
sub-stack is currently active. Two extra primitives beyond `navigate`/`goBack`: `resetTo(key)` wipes
every section and lands on `key` alone (login/logout); `replaceCurrent(key)` swaps the sub-stack's
top entry instead of pushing (a "this screen is done, hand off to the next one" transition, e.g. a
wiki create-page screen handing off to the page it just created).

**Switching top-level sections replaces the active slot in `topLevelStack`, it does not push.**
`goToTopLevel()` (the branch `navigate()` takes for any top-level key that isn't the current one)
writes the new key into `topLevelStack`'s existing slot rather than appending — so drawer-tap chains
(Epics → Issues → Kanban) never grow the stack past whatever depth it already had (size 1 outside
`resetTo()`), and `goBack()`/`canGoBack()` treat any section's root as unhandled — falling through to
the system/exit — instead of walking back through previously-visited sections. Fixed 2026-09-07
(was `removeAll` + `add`, which pushed and made back cascade through every section visited that
session); see `docs/archive/revisit-resolved.md`#51 for the full mechanism and fix.

**Any screen whose back handling assumes `goBack()` pops past its own top-level boundary broke
when the above landed.** `ProjectSelectorScreen`'s login-abandon path is the one confirmed case:
its `NavigationBackHandler`/back-arrow (`isBackEnabled = state.isFromLogin`) used to rely on
`goBack()` popping `topLevelStack` from `[Login, ProjectSelector]` back to `[Login]` — under the
replace-not-push fix that pop has nothing to do, so `goBack()` silently no-ops and the back
gesture does nothing. Fixed 2026-09-08 by having `MainNavHost.kt`'s `ProjectSelectorScreen` call
site wire `goBack` to `navigator.resetTo(LoginNavDestination)` instead — see
`docs/issues/2026-09-08-project-selector-back-after-login-does-nothing.md`. Any other screen that
is simultaneously a top-level key *and* expects back to escape past its own section (not just
pop its own sub-stack) needs the same treatment, not plain `navigator.goBack()`.

**`NavigationIconConfig.Back()` with no `onBackClick` does not call the screen's own `goBack`
param** — `TaigaTopAppBar.kt`'s `NavigationIcon` falls back to a `defaultGoBack` wired centrally in
`MainScreen.kt` instead. Any screen whose `goBack` lambda does something beyond `navigator.goBack()`
(most commonly: `resultBus.sendResult(UpdateDataOnBack)` before popping, so a list screen refreshes
on return — see `EpicNavGraph.kt`/`EpicDetailsScreen.kt` for the pattern) must pass
`NavigationIconConfig.Back(onBackClick = { goBack() })` explicitly, or the top bar's back arrow — the
primary way users leave the screen — silently bypasses it. Confirmed 2026-09-02: `WikiPageScreen`
used the bare form and its `UpdateDataOnBack` send never fired from the back arrow.

**`resetTo()` fully disposes every top-level screen's ViewModel, not just whichever section's
sub-stack actually lost entries.** Every `TOP_LEVEL_KEYS` entry (`MainAppState.kt`) except
`ProjectSelectorNavDestination` is a payload-less `data object` singleton, and writing that same
singleton back into a `NavBackStack` slot is invisible to Nav3's own `ViewModelStoreNavEntryDecorator`,
which only disposes a `ViewModelStore` when a key structurally *disappears* from the tracked
backstack — this made `DashboardViewModel` keep showing a previous account's data after
logout→login until a manual refresh
(`docs/issues/2026-09-09-account-switch-stale-data-flash-and-refresh-failure.md`). Fixed at the
library level in `grappim-kit-navigation` 0.1.3: `NavigationState` gained a `resetGeneration`
counter that `resetTo()` bumps, and `toEntries()` wraps its per-section decoration in
`key(resetGeneration)`, forcing Compose to discard and recreate every section's decorators (and
therefore their `ViewModelStore`s) on a reset regardless of whether any individual key's identity
changed. `navigate()`/`goToTopLevel()`/`goBack()` never touch it — ordinary tab-switch/back state
preservation is unaffected, and `goToTopLevel()` was never actually part of this bug (preserving a
section's state across a switch is intended behavior). This app's own interim workaround —
wrapping `MainScreen.kt`'s `MainScreenContent` body in an app-level `key(sessionGeneration)`,
landed 2026-09-09 — was removed the same day once 0.1.3 shipped; `MainScreen.kt`'s logout handler
now just calls `navigator.resetTo(LoginNavDestination)` directly, relying on the library fix.
**A GUI check that force-stops the app between logins cannot catch a regression here** — a fresh
process never had the stale instance to begin with; verification needs two logins inside one
continuous process.

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

**`WorkItemEditStateRepository` (`feature/workitem/ui/.../screens/WorkItemEditStateRepository.kt`)
sessions use unbuffered rendezvous `Channel`s, keyed by `(workItemId, TaskIdentifier)`.** If the
screen that writes an update (e.g. the edit-description screen) and the screen that's supposed to
receive it key their session with different ids, the write doesn't fail or get dropped — `send()`
just suspends forever inside whichever `viewModelScope.launch` called it, since nothing is
`receive()`-ing that session. Confirmed 2026-09-02: `WikiPageViewModel` keyed its listener off
`route.id` (the nav-route's id) while the writer keyed off the loaded page's real id: for any screen
reached with a different id than its own (e.g. `WikiPageNavDestination` opened via a bookmark, whose
`id` is the wiki-link's id, not the page's), the update silently hung rather than erroring. Key both
ends off the same, authoritative id — not whatever the caller happened to navigate in with.

## Use Cases

Use cases only when multiple repository calls are needed. For single repo calls, call repository directly from ViewModel.

## Feature Module Structure

```
feature/{name}/
├── data/     → API, DTOs, RepositoryImpl, Koin module
├── domain/   → Models, Repository interface
└── ui/       → NavDestination, Screen, State, ViewModel
```

**A DTO must never appear outside `data/`** — no `Repository`/`UseCase` interface, ViewModel, or
`ui/` type may take or return one. Map to a domain model before returning, even when the wire shape
is already a perfect fit; this keeps the domain/ui layers insulated from API schema changes and
keeps every repository swappable.

## Koin DI

For DI patterns, the `expect/actual @Configuration` rule, module registry, qualifier map, and troubleshooting, see the **koin-expert** subagent. It is not in this repo — it lives in `agentic-grappim` and is symlinked into `~/.claude/agents/` (see Skills below). Never use KSP — this project uses `io.insert-koin.compiler.plugin` exclusively.

## KMP String Resources

Every `RString.x` reference needs its **own** import (`import com.grappim.taigamobile.strings.generated.resources.create_task`)
— the generated strings are extension properties, so importing `RString` alone gives
`Unresolved reference 'create_task'`. This bites in test files as often as in Composables: a test
asserting `NativeText.Resource(RString.title_is_empty)` needs the same import line the ViewModel has.
Same rule for `RDrawable.x` (confirmed 2026-08-22 adding `RDrawable.ic_refresh` inside `uikit` itself
— the generic "unresolved reference" error gives no hint that a per-symbol import is missing) and any
other generated resource accessor (font, etc.) — it's a Compose Resources mechanism, not specific to
strings.

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
- `docs/testing/` — [survey.md](docs/testing/survey.md) (what exists),
  [improvement-plan.md](docs/testing/improvement-plan.md) (closed 2026-08-08; historical record of
  tasks 0–21) and [deferred.md](docs/testing/deferred.md) (ideas surveyed but not actioned — check
  here before proposing a new testing task)

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

**The coverage floor is a ratchet: raise it, never lower it.** `:koverVerify` enforces line ≥ 92 %
and branch ≥ 78 % (root `build.gradle.kts`, `total { verify { } }`). A PR that breaches it needs
tests, not a smaller bound. For the missed-branch/missed-line ranking heuristics, the
`koverXmlReport`-vs-`koverVerify` traps, and the `kover-rank.py`/`kover-diff.py` usage notes — read
when running a coverage sweep — see
[docs/testing/kover-coverage-heuristics.md](docs/testing/kover-coverage-heuristics.md).

**Verify with the full `./gradlew jvmTest`, not just the module's own task.** All modules' JVM tests
share one process, so a coroutine that escapes a test in one module can fail an unrelated test in
another (mechanism: **testing** subagent, gotcha 7). `:feature:x:jvmTest` passing proves your test
works; only the full run proves it did not break someone else's. When a failure appears alongside
your change, A/B it against a clean tree (`git stash -u`) before assuming you caused it.

**Run `ktlintCheck` too — a green `jvmTest` says nothing about it.** The rule that catches new test
code is `standard:function-signature`: a signature written across multiple lines fails if it *would
fit* on one within the 120-char limit. It bit two consecutive sessions on the same construct — a
`setupSuccessfulLoad(data: XDetailsData = getXDetailsData(...))` default-argument helper at ~106
characters, which reads as over-long and is nonetheless required to be one line. Dropping argument
names in the nested factory calls is what keeps it under the limit.

**`standard:class-signature` is the same trap for a class header, and it auto-fixes.** Adding a
second constructor param that pushes `class Foo(...) : SuperType {` past 120 chars — do not hand-wrap
it as one param per line with the closing paren and `: SuperType {` on their own lines; ktlint instead
wants every param kept on the constructor's own line and only the `: SuperType {` broken onto the
next one. Don't guess the format: run `./gradlew :module:path:ktlintCommonMainSourceSetFormat` (or the
matching task for the source set that failed) and let it rewrite the file.

**Every `XApi` is an `interface XApi` + `@Single(binds = [XApi::class]) class XApiImpl`** — no
exceptions, so any API can be faked in `:testing`. `WikiApi` was the last concrete one and was split
in the course of testing it.

**Failure-path convention (required): every public method of a repository impl, use case or
ViewModel gets a test where a collaborator throws `testException`, asserted with
`assertFailsWithTestException { }`** (`:testing`, `TestUtils.kt`) — not a bare
`assertFailsWith<IllegalStateException>`, which also matches a fake's own `error("… not set")` guard
and can pass without the test ever reaching the code it claims to cover. This is what closes the
line-vs-branch coverage gap. Full convention — swallowing methods, the `…Throws` fake hook,
`Result`-returning methods — in the **testing** subagent's "Failure-path convention" section.

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

**Integration tests against a live external server** are the same "real actual" preference taken one
step further: a plain `jvmTest` class, gated on `System.getenv("X") ?: return` (not a separate
Gradle source set), can exercise the real Ktor/OkHttp client against a real backend —
`LoginIntegrationTest` (`composeApp/src/jvmTest/.../di/`) was the first. **Always call the shared
`liveTaigaSessionOrSkip(): Koin?` helper (`LiveTaigaSession.kt`) rather than building a fresh
`koinApplication` per test** — the JVM `DataStore` backends tolerate only one open
`koinApplication` per process. Full pattern, the `DataStore`-collision mechanism, and the
write-round-trip convention are in the **testing** subagent's "Integration test against a live
server" section.

**Gradle does not track env vars — or `-P` project properties — as task inputs.** Re-running
`./gradlew jvmTest` after only changing an env var (not source) reports `UP-TO-DATE` and silently
skips re-execution — pass `--rerun` to force it, or what looks like a pass is a stale cached result
from a previous run under different env vars. Confirmed the same trap for `-P` flags too: toggling
`-PcomposeStabilityReport` on a module whose compile task was already `UP-TO-DATE` produced no
reports at all until `--rerun-tasks` forced re-execution (see
[docs/compose/stability-reports.md](docs/compose/stability-reports.md)).

## CI Guardrails

`.github/workflows/guardrails.yml` (+ `.github/scripts/check-guardrails.sh`) fails a commit that
touches `.github/`, `build-logic/`, `config/detekt/`, or `.editorconfig`; that touches
`gradle/libs.versions.toml`'s `detekt`, `ktlint`, `composeRules`, `agp` or `kover` version keys
specifically (a plain dependency bump — Renovate's usual PR — doesn't trip it, narrowed 2026-08-14
after Renovate's own commits, which can never carry a `Gate-change:` line, failed guardrails on
every PR); or that adds a new `@Ignore`/`@Suppress`, unless the commit message carries a line:

```
Gate-change: what was widened, and why
```

This doesn't prevent widening a gate — it makes doing so silently impossible. It runs with no
`paths-ignore`, unlike `build.yml`/`code_analysis.yml`, so a CLAUDE.md-only commit is still checked.
Run it locally before committing: `.github/scripts/check-guardrails.sh HEAD~1..HEAD`.

**Validate a `.github/workflows/*.yml` edit locally before pushing** — `docker run --rm -v
"$(pwd)":/repo -w /repo rhysd/actionlint:latest .github/workflows/<file>.yml` catches invalid
expressions, unknown action inputs, and embeds shellcheck on `run:` blocks, none of which a plain
YAML-syntax check (`python3 -c "import yaml; yaml.safe_load(...)"`) would catch. Confirmed
2026-08-30 adding `build.yml`'s `apk-size-check` job.

**A `pull_request`-triggered job's default shallow checkout doesn't have the base branch's commit
object locally** — to build against the PR's merge-base (e.g. for a size/perf diff), fetch it
explicitly: `git fetch --depth=1 origin ${{ github.event.pull_request.base.sha }}` before `git
checkout` that sha. Confirmed 2026-08-30 in `build.yml`'s `apk-size-check` job.

**`ubuntu-latest`'s preinstalled Google Chrome apt repo can intermittently fail `apt-get update`
with a Hash Sum mismatch** (a CDN metadata race on Google's end, unrelated to this repo) and abort
the whole `apt-get update`, breaking any later `apt-get install` in the same step — hit in
`build.yml`'s `desktop-package` job (`Install fakeroot and rpm`), which doesn't use Chrome at all.
Fix: `sudo rm -f /etc/apt/sources.list.d/google-chrome.sources` before `apt-get update`. Note the
filename — it's the newer deb822 format (`.sources`, `URIs:`/`Suites:` keys), not the legacy
`google-chrome.list` that `actions/runner-images`' own `install-google-chrome.sh` still references;
guessing the old name is a silent no-op (`rm -f` doesn't error on a missing path) rather than a
visible failure. Confirmed 2026-09-09 (PR #409) by listing `/etc/apt/sources.list.d/` in a debug
step rather than guessing twice.

## Multi-Session Work

For any initiative that spans multiple sessions — a feature investigation, a redesign, a
migration; not a single bug fix — split it into its own directory under `docs/` with two files:

- **`CHECKLIST.md`** — the executable plan: numbered, tickable steps, each sized to fit a single
  clean context. A `Progress` / `Current step` header at the top is the only record of how far
  the initiative has got — don't duplicate that anywhere else. Once a step is ticked, move its
  entry out into a sibling **`CHECKLIST-DONE.md`** so the live checklist stays short enough to
  read cold at the start of a session — `CHECKLIST-DONE.md` is precedent for a step cited by
  number, not a place to look for open work.
- **`IMPLEMENTATION_PLAN.md`** — the reference: architecture, rationale, options weighed and
  their tradeoffs. Updated with what each step actually taught, so it stays the canonical answer
  for the next session instead of the checklist's own step text going stale.

`docs/testing/{survey.md, improvement-plan.md, deferred.md}` already used this split informally,
before it had a name — read that as the worked example if a fresh one doesn't clarify something.

**"Do step N" means:** read `CHECKLIST.md`, do *exactly* that step, run its `Verify:` line, tick
it and move it to `CHECKLIST-DONE.md`, add a one-line `Note:` if anything deviated from the
description, and update the `Progress` header. Don't start a step whose dependencies aren't
ticked, and don't expand scope beyond it. **End the archived step's `Note:` by naming what comes
next** — the following step's number, or "queue is empty" if none is scoped — in prose, not just
via the `Progress` header: gregory reads the note to tell whether there's more to do without
re-reading the whole checklist, so the answer has to be stated, not implied.

**Answering a question a not-yet-started step will ask is not the same as asking for that step to
run.** If gregory states a preference or decision a later step needs, record it in
`IMPLEMENTATION_PLAN.md` for when that step starts — don't treat it as the "do step N" trigger and
launch the step immediately.

**Close a step out every time, without being asked:**

1. Run the `finalize` skill — a step almost always teaches something the plan didn't know; this
   is where it gets written down instead of dying with the context.
2. Fold anything structural out of the step's `Note:` into `IMPLEMENTATION_PLAN.md`.
3. Check the docs for claims the step just made false (grep for what changed — see the `finalize`
   entry under Skills & Agents below).
4. Commit, following Git Workflow below — branch + PR into `dev` unless told otherwise. One
   commit per step; the PR is a checkpoint before it lands, not a batching mechanism.

**Decomposing a new phase of an initiative into checklist steps is its own commit**, made the
moment it happens — not left for whoever picks up step 1 to discover via `git status`.

A step that's gated on a decision nobody has made yet (gregory needs to pick between options, a
prerequisite step isn't done) carries `⛔ **Gated — do not start without asking.**` as the first
line under its own heading, not only a note in the status table — two places to look beats one,
for when the table gets skipped anyway.

## Git Workflow

**Default to a feature branch + PR into `dev`.** Push straight to `dev` only when gregory says so
explicitly for that change — a general "commit and push" without naming `dev` means branch + PR,
not a direct push. `dev` has a GitHub branch-protection rule requiring PRs; a direct push still
goes through (bypassed via admin permissions), so nothing technically stops it — this is a process
discipline to follow regardless, not something the repo enforces on its own.

**Releases go through `release-prepare.yml`, not a hand-rolled branch/PR.** Trigger it
(`gh workflow run release-prepare.yml -f version=X.Y.Z -f version_code=N`, next code = current
`gradle/libs.versions.toml` `version-code` + 1) — it merges `dev` into a new `release/vX.Y.Z` branch,
bumps `gradle/libs.versions.toml`, stubs the F-Droid/Play Store changelogs, and opens a PR into
`master`. Edit the changelog stubs and commit any fixes found during release testing directly onto
that `release/vX.Y.Z` branch (small commits, same as any other work) — merging the PR auto-tags
`vX.Y.Z`, `release.yml` builds and publishes it, and `release-finalize.yml` back-merges `master` into
`dev`. **`guardrails.yml`'s range for a `release/* -> master` PR must diff against `dev`, not
`master`** (fixed 2026-09-02, first release PR since that workflow shipped) — `master` only advances
on releases, so diffing against it re-checks the entire dev-vs-master backlog instead of just the
release branch's own commits. See `docs/revisit.md` #47 for the equivalent, still-open gap on the
`push`-triggered run that fires on `master` right after the merge.

**Never write a bare `#N` to reference a `docs/revisit.md`/archive entry number in a PR/issue
description, comment, or commit message.** GitHub autolinks any `#<digits>` rendered through its
web UI (PR/issue bodies and comments, and file content rendered from the repo) to that repo's own
issue/PR `#N` — unrelated to what `N` actually means here — the moment an issue or PR with that
number exists. "tracked as revisit #51" in a PR description silently became a link to PR #51.
Write `revisit.md entry 51` / `revisit #51 (docs/revisit.md)`, or wrap it in backticks (`` `#51` ``,
which suppresses GitHub's autolink), instead of a bare `#51`.

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

**Run `finalize` automatically at the end of every task — don't offer it, don't wait to be asked.**
A task is only done once the work is verified *and* whatever it taught has been written down;
finalize is part of finishing, not a follow-up step. Applies to any unit of work that produced
non-obvious knowledge (a bug fix, a feature, a refactor, an investigation) — trivial one-line asks
don't need it. Also **check the docs for claims the work just made false** — grep for what changed
rather than trusting a read-through; a stale present-tense statement left behind is actively
misleading, and the next session will cite it as current.

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

**When a later session fixes a `docs/revisit.md` entry, close it out the same way the pre-#45
entries were: move the whole section to
[docs/archive/revisit-resolved.md](docs/archive/revisit-resolved.md) with a `**Fix (date):**`
paragraph appended describing what changed, and remove it from `revisit.md`.** Both files keep a
`| # | Item | ... |` index table above their sections — update both, not just the section body, or
the index goes stale while the section itself disappears. Confirmed 2026-09-11 closing entry 54.

The test: Every changed line should trace directly to the user's request.

### Comments

**A comment must describe only the current code, and stand alone.** Before writing one, ask: would
this make sense to someone reading cold, with no knowledge of the PR, the conversation, or what was
decided against? If not, it doesn't belong in the source. Do not reference:

- **History or migration:** "replaces X…", "used to live in…", "previously returned emptyFlow()".
- **Rejected alternatives:** "…not Y", "rather than Z" left unjustified on its own terms — if `X`
  is worth a comment, justify `X`; drop the `Y` half.
- **Conversation or process state:** "for now", "TBD", a task/checklist number ("task 19/20").

Migration history, rejected alternatives, and task tracking belong in the commit message or the
relevant `docs/` checklist — not in a comment that has to keep making sense years after the PR that
added it is forgotten.

### Don't Break Production in Favor of Tests

**Production code must not be shaped by testing needs.** If a code path is flaky or
can't be observed deterministically as written, fix or remove the *test* — don't add a
seam, injectable parameter, or abstraction to production code purely so a test can
control it. This holds even when the change is small, additive, and provably safe
(e.g. a defaulted constructor parameter verified not to affect DI resolution) — the
question is not "is this change safe," it's "does this belong in production code at
all." Prefer, in order: (1) find or write a lower-level test that already covers the
behavior deterministically without the racy synchronization; (2) simplify the test to
avoid it; (3) delete the test and say so plainly, rather than leaving a known flake
undocumented. Always ask before adding any production-code testability seam, even a
well-verified one.

### Determinism Over Process

If a task has one correct, computable answer, use a tool for it rather than following a fixed
procedure by hand — a script or a hook can't skip a step or get one wrong the way a prose checklist
can. `.github/scripts/check-guardrails.sh` is this project's own example: the gate rules are a
script, not a mental checklist to re-derive each session. Reserve judgment for what actually needs
it — ambiguous input, a plan, a choice between options.

**Never guess an external artifact's exact name from convention — list or query the real source
first.** A filename, URL, or API signature that "should" follow a pattern (a release-asset name
built from a version tag, an apt source's legacy filename, a sibling function's parameter list)
routinely doesn't, and a wrong guess that fails silently (e.g. `rm -f` on a missing path) can burn
a full round-trip before the mismatch is even visible. Confirmed three times: a GitHub release's
actual asset name vs. one built from the version tag (`docs/frictions.md` 2026-08-29), an
`androidx` API's real signature vs. a sibling overload's shape (2026-08-15), and a CI runner's
actual apt source filename vs. the legacy name referenced in its own build script (2026-09-09,
PR #409) — see the CI Guardrails entry above for that case. Check with `gh api`/`ls`/reading the
real source before writing the fix, not after it fails once.

### Goal-Driven Execution

Turn a task into a verifiable goal — "fix the bug" becomes "write a failing test, then make it
pass." For multi-step work, state the steps with a check each, then loop until they pass.

### Verification

**A UI-visible change isn't done until it's been driven on the emulator, not just proven by a
test.** A `jvmTest`/Compose-UI test proves the code path in isolation; only booting the emulator
(or a connected device) and exercising the actual screen proves it renders and behaves correctly
in the running app. Use the **emulator-testing** skill — `docs/EMULATOR_TESTING.md` holds this
project's device facts (AVD name, package ids, app-specific gotchas) — before calling a UI change
complete. Don't stop at a green test suite; a passing test and a rendered screen are different
claims.

**The same applies before starting a fix, not only after it.** A `docs/revisit.md`/checklist entry
describing an observed UI state can go stale between when it was queued and when a later session
picks it up. Confirmed 2026-08-22 (tablet checklist step 14): a queued "Issues list has no row
divider" entry turned out to already be false — a GUI check on the running desktop app caught it
before any code was written, instead of after a fix landed for a problem that no longer existed.

**A single successful manual check isn't enough for behavior that depends on prior interaction —
verify after normal usage, not just on first load.** Confirmed 2026-08-22 (tablet checklist step
15, desktop keyboard shortcut): a focus-based `onPreviewKeyEvent` implementation passed a GUI check
immediately after navigating to the screen, then silently stopped working after any other click
elsewhere in the app stole Compose focus from its hidden node — no error, no visible symptom. The
first check would have shipped a shortcut that's dead the moment a real user touches anything else.
Re-test after the kind of interaction a user would actually do in between, not just the one action
under test.

### Friction Goes in Writing Too

The rule above (in Surgical Changes) is for problems in the *code* — write those to
`docs/revisit.md`. This one is for friction in the *tooling*, the kind that silently never gets
reported: a guessed flag that failed, a command that needed different quoting, a check that
confidently returned the wrong answer. The reflex is to route around it and say nothing.

Add a one-line, past-tense entry to `docs/frictions.md` before moving on — create the file if it
isn't there. At the end of a session, read the file back and report what was added, with a count,
even when the count is zero. The same friction three times is a fix, not a fourth line — raise it
in `finalize`.

## Settled Decisions

Weighed and declined — don't re-propose these.

| Not used | Instead | Why |
|---|---|---|
| Mocking libraries (MockK, Mockito) | Hand-written fakes in `:testing` | `kotlin.test` + fakes keep `commonTest` portable across all KMP targets; a mocking library would tie tests to the JVM. See Testing above. |
| KSP for DI | `io.insert-koin.compiler.plugin` (IR/FIR) | This project uses the Koin compiler plugin exclusively — see Koin DI above. |

## Logging

`grappim-kit-logger` (published Maven artifact, `io.github.grigoriym:grappim-kit-logger` — swapped
in for the local `core/logger` module, see grappim-kit Modules above) is a KMP logging facade — it
is added to every KMP module's `commonMain` automatically by the convention plugin, so `logcat` is
always available without a dependency change.

```kotlin
import com.grappim.kit.logger.logcat
import com.grappim.kit.logger.LogPriority   // separate import, only if you set a priority

logcat { "plain debug message" }                               // as an Any extension: tag = this::class.simpleName
logcat(tag = "Ktor") { "explicit tag" }                        // top-level overload: tag is null unless given
logcat(LogPriority.ERROR, throwable = e) { "failed to load" }
```

Priorities: `VERBOSE`, `DEBUG` (default), `INFO`, `WARN`, `ERROR`, `ASSERT`.

The message is a lambda, so it isn't built unless a logger is installed. Never call `Timber`
directly outside `grappim-kit-logger`.

**Backends** — `KitLogger.install(...)` is called once per platform entry point:

| Platform | Impl | Installed in |
|----------|------|--------------|
| Android | `TimberLogger` → Timber (`DebugTree` on debug, `CrashlyticsTree` on gplay) | `androidApp/TaigaApp.kt` |
| iOS | `NSLogLogger` → `NSLog`, chunked at 3000 chars to survive its ~4096-byte truncation | `main.ios.kt` |
| Desktop/JVM | `FileLogger` → appends to `taigamobile.log` in the per-user app-data dir (`core/storage`'s `appDataDir()`), rotating to `<name>.old` past 5 MB | `TaigaMobileDesktop.kt` |

## Security

`docs/security/masvs.md` is the living MASVS v2 register — Accepted deviations (deliberate, bounded
tradeoffs like TOFU cert trust and cleartext-for-self-hosted-LAN), Open findings, and what still
needs a device/APK to verify. Check it before reporting a new security finding; a control already
reviewed there has a bound recorded, not a re-raise. Maintained by the **masvs-review** skill.
`docs/security/masvs-review-plan.md` is closed (all 8 categories done, 2026-08-10) and kept only as
historical record of how the register was built — `masvs.md` itself is the current source of truth.

## Error Handling

- Never swallow exceptions silently. Every `catch` block must at least log the exception:
  `logcat(LogPriority.ERROR, throwable = e) { "what failed" }`.
- `CrashlyticsTree` forwards every `logcat(priority = ERROR, throwable = ...)` call verbatim to
  Firebase, including the throwable's raw `.message`. Before logging a *raw* (unmapped) exception
  from a network/parsing/IO boundary at that priority, check whether its message can carry
  sensitive data (a hostname, a response body) — see `core/api/.../ExceptionSanitization.kt` and
  `docs/security/masvs.md`'s MASVS-PRIVACY-3 note for the pattern and the audit
  (`grep -rn "LogPriority.ERROR"` + `throwable =` is the full surface).

## Compose / Platform Rules

- Do not use early returns in Composable functions — use conditional wrapping
- Lambda parameters: present tense (`onClick` not `onClicked`)
- Prefer `kotlinx-collections-immutable` (`ImmutableList`, `persistentListOf()`) over `List`/`MutableList` in state classes and Composable parameters for stable recomposition — verify with the opt-in Compose Compiler stability reports, see [docs/compose/stability-reports.md](docs/compose/stability-reports.md)
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

## Performance

Android frame-timing technique (`dumpsys gfxinfo`, Perfetto) and the `:benchmark` module's
Baseline Profile generator live in [docs/perf/profiling.md](docs/perf/profiling.md) — read it before
profiling a jank report or touching `benchmark/src/main/kotlin/.../BaselineProfileGenerator.kt`.
`docs/perf/profiling-plan.md` is closed (all 3 tasks done) and kept only as historical record.
