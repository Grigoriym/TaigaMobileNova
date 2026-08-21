# Tablet and Other Form Factor Support — Done Steps

Archive of ticked steps from [CHECKLIST.md](CHECKLIST.md), kept for precedent when a later step
cites one by number. Not a place to look for open work.

## Step 1: Add a reusable content-width-cap primitive to uikit — ✅ done 2026-08-15

Added `TaigaAdaptiveContent` in
`uikit/src/commonMain/kotlin/com/grappim/taigamobile/uikit/widgets/layout/TaigaAdaptiveContent.kt`
— a `Box` that centers a `widthIn(max = 840.dp)` inner `Box` inside a `fillMaxWidth()` outer one,
a no-op below 840dp. Preview added with `@PreviewTaigaDarkLight` + `TaigaMobilePreviewTheme`, per
`uikit` convention. Not wired into any screen — that's gated, see CHECKLIST.md.

**Verify:** `./gradlew :uikit:compileKotlinJvm :uikit:ktlintCheck` — both green, no `Note:`,
nothing deviated from the step's description.

**Next:** step 2 (unblocked — no scope decision needed to add a dependency).

## Step 2: Add the `material3-adaptive-navigation-suite` dependency — ✅ done 2026-08-15

Added `jetbrains-compose-material3-adaptive-navigation-suite` (module
`org.jetbrains.compose.material3:material3-adaptive-navigation-suite`, pinned to
`jetbrainsComposeMaterial3` = `1.10.0-alpha05`) to `gradle/libs.versions.toml`, and
`implementation(libs.jetbrains.compose.material3.adaptive.navigation.suite)` to
`composeApp/build.gradle.kts`'s `commonMain` dependencies, next to the existing
`jetbrains.compose.icons.extended` line. No UI code changes, as scoped.

**Verify:** `./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`,
`:composeApp:compileKotlinIosArm64 --rerun-tasks`, `:androidApp:compileFdroidDebugKotlin
--rerun-tasks`, `:composeApp:compileKotlinJvm` — all four green, no `Note:`, nothing deviated
from the step's description.

**Next:** step 3 is gated on gregory choosing the `DrawerItem` → `NavigationSuiteScope` mapping
(see CHECKLIST.md and IMPLEMENTATION_PLAN.md).

## Step 3: Design the `DrawerItem` → `NavigationSuiteScope` item mapping — ✅ done 2026-08-15

Decision: dual-path, width-gated. Compact width keeps `TaigaDrawerWidget`'s `ModalNavigationDrawer`
unchanged (app-name header, group labels, divider, full grouped experience). Medium/expanded width
uses `NavigationSuiteScaffold` with a flattened item list — `DrawerItem.Group` unwrapped to its
`Destination`s with no group-label text, `DrawerItem.Divider` dropped, no app-name header — since
the `navigationSuiteItems` API has no slot for any of the three. gregory chose this over flattening
uniformly at all widths or investigating a multi-section API; full option list and rationale in
IMPLEMENTATION_PLAN.md's "Step 3 decision" section. No code changes — design-only step, as scoped.

**Verify:** IMPLEMENTATION_PLAN.md records the chosen mapping and why — done. No build/test
verification applies to a design-only step.

**Next:** step 4 — wire `NavigationSuiteScaffold` into `MainScreen.kt` / `TaigaDrawerWidget.kt`
using this mapping. It's a substantial code + emulator-verification task on its own; confirm with
gregory before starting it rather than continuing straight into it.

## Step 4: Wire `NavigationSuiteScaffold` into `MainScreen.kt` / `TaigaDrawerWidget.kt` — ✅ done 2026-08-15

Implemented the step 3 mapping. `flattenForNavigationSuite()` (new, `DrawerItem.kt`) unwraps
`DrawerItem.Group` to its inner `Destination`s and drops `DrawerItem.Divider`. New
`TaigaNavigationSuiteWidget` composable (`TaigaDrawerWidget.kt`) renders `NavigationSuiteScaffold`
over the flattened list. `MainScreen.kt`'s `MainScreenContent` now branches on
`currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)`:
compact renders `TaigaDrawerWidget` (unchanged, `NavigationBackHandler` included — modal-only,
since a rail/permanent drawer has no open/close animation state to intercept); medium/expanded
renders `TaigaNavigationSuiteWidget` with `layoutType =
NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)` (no back-handler).
Both share one `mainContent` composable lambda (the `Scaffold` + top bar + `MainNavHost`) so the
duplication is only the outer chrome, not the screen content.

**Deviation from the step's description — new dependency, not anticipated in CHECKLIST.md/
IMPLEMENTATION_PLAN.md:** `currentWindowAdaptiveInfo()`/`WindowAdaptiveInfo`/
`NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo` are **not** exported by the
`material3-adaptive-navigation-suite` artifact step 2 added — they live in a separate
`org.jetbrains.compose.material3.adaptive:adaptive` artifact (own release train, unrelated to
`jetbrainsComposeMaterial3`'s version). Confirmed via `.module`/jar inspection, not docs (this
library's docs don't spell out the artifact split). Added `jetbrainsComposeMaterial3Adaptive =
"1.3.0-beta02"` (verified latest via `adaptive-desktop`'s `maven-metadata.xml`, per
[[verify-dependency-versions-via-maven-metadata]]) and
`jetbrains-compose-material3-adaptive` to `gradle/libs.versions.toml`, and
`implementation(libs.jetbrains.compose.material3.adaptive)` next to the navigation-suite line in
`composeApp/build.gradle.kts`. Full detail in IMPLEMENTATION_PLAN.md's "Step 4 notes" section.

Also used the non-deprecated width check: `currentWindowAdaptiveInfoV2()` +
`WindowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)` (from the transitive
`androidx.window:window-core`), not `currentWindowAdaptiveInfo()` +
`.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT` as the step's own text
suggested — the latter compiles but the compiler flags all three symbols deprecated.

Added `composeApp/src/commonTest/kotlin/com/grappim/taigamobile/FlattenForNavigationSuiteTest.kt`
(3 cases: destinations pass through, group unwraps, divider drops) per the Failure-path/test
convention for new public functions — pure-function coverage, no Compose needed.

**Verify:** `./gradlew jvmTest` (full repo run, green) and `ktlintCheck` (green, one
`standard:function-signature` violation auto-fixed via `ktlintCommonMainSourceSetFormat`) both
passed. Compiled all three targets (`:composeApp:compileKotlinJvm`,
`:composeApp:compileAndroidMain`, `:composeApp:compileKotlinIosSimulatorArm64`) — all green.
Emulator-verified on both `Medium_Phone_API_36.1` (compact — modal drawer unchanged: header, group
labels, divider all present, hamburger opens/closes it) and `Medium_Tablet` (medium/expanded —
`NavigationSuiteScaffold` rendered a `NavigationRail`, not a permanent drawer, at ~1280dp width;
flattened list confirmed no header/group-labels/divider; tapping "Epics" navigated correctly and
updated selection highlighting). See `docs/EMULATOR_TESTING.md` for the `Medium_Tablet` AVD facts
and the rail-vs-drawer gotcha recorded there.

**Next:** queue is empty. Option 2 (adaptive navigation chrome) is now fully implemented — steps
2–4 done. Option 3 (list-detail two-pane, blocked on a Navigation 3 migration informed by
`wallosmobile`) remains unscoped per IMPLEMENTATION_PLAN.md's "Decision status" — decomposing it
into checklist steps is its own commit when gregory is ready to start it, not automatic follow-on
work here.

## Step 5: Investigate the Navigation 3 migration path, informed by wallosmobile — ✅ done 2026-08-15

Design/investigation-only, as scoped — no code changes. Researched `wallosmobile`'s Nav3 shell
(`core/navigation/Navigator.kt`/`NavigationState.kt`, dual back-stack-of-stacks; built on Nav3 from
inception, not migrated — its `core:navigation` commit is the first navigation commit in its
history), the `android-skills:navigation-3` skill (read directly from
`~/.claude/plugins/marketplaces/android-skills/navigation/navigation-3/` after the `Skill` tool
failed to resolve the plugin name from a fork — see `docs/frictions.md`), and this repo's own
`MainNavHost.kt`/`nav/*.kt`. Findings written to IMPLEMENTATION_PLAN.md's new "Navigation 3
migration investigation" section: this repo has none of the migration guide's "unsupported"
blockers (no nested nav graphs, no deep links, no `dialog<T>` destinations); the real porting cost
is 15 ViewModels moving off `savedStateHandle.toRoute<T>()` to constructor-parameter injection, and
the `UPDATE_DATA_ON_BACK` result-passing convention needing the event-bus recipe — both mechanical
but not zero-risk. Confirmed via JetBrains' own KMP Nav3 doc (WebFetch,
`kotlinlang.org/docs/multiplatform/compose-navigation-3.html`) that Nav3 fully supports this
project's three targets since Compose Multiplatform 1.10, and that non-JVM targets require an
explicit `SerializersModule`/`SavedStateConfiguration` for `NavKey` (wallosmobile's
`NavKeySerializers.kt` already implements this pattern) since Android's reflection-based
serialization isn't available on iOS. Confirmed the KMP `adaptive-navigation3` artifact
(`org.jetbrains.compose.material3.adaptive:adaptive-navigation3`, `1.3.0-beta02`) exists, but did
**not** confirm it exports `ListDetailSceneStrategy` with the same API as the Android-only recipe —
flagged as an open question, same shape as step 4's artifact-split surprise.

**Verify:** IMPLEMENTATION_PLAN.md records the findings, the recommended all-at-once migration
approach, and a proposed (not yet decomposed) 7-step breakdown — done. No build/test verification
applies to a design-only step.

**Next:** queue is empty again. The proposed 7-step Nav3 migration breakdown in
IMPLEMENTATION_PLAN.md is **not yet decomposed into CHECKLIST.md** — that decomposition, and the
list-detail pane-layout design that follows it, are gated on gregory reviewing that section's "Open
questions" (KMP `ListDetailSceneStrategy` API parity unconfirmed; which screens get list-detail;
whether the migration ships as one contiguous initiative or with review points between steps).
Decomposing is its own commit once gregory answers those, not automatic follow-on work here.

## Step 6: Add Nav3 dependencies — ✅ done 2026-08-15

Added `jetbrainsNav3 = "1.1.1"` plus `jetbrains-navigation3-ui`
(`org.jetbrains.androidx.navigation3:navigation3-ui`) and `jetbrains-lifecycle-viewmodel-navigation3`
(`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`, reusing the already-pinned
`jetbrainsAndroidxLifecycle = "2.11.0"`) to `gradle/libs.versions.toml`. Wired all three
(`jetbrains.navigation3.ui`, `jetbrains.lifecycle.viewmodel.navigation3`, and the
already-catalogued-but-previously-unused `jetbrains.androidx.savedstate`) into
`composeApp/build.gradle.kts`'s `commonMain` dependencies, next to the other `jetbrains.compose.*`
lines, with a comment repeating wallosmobile's warning: never the plain `androidx.navigation3:*`
coordinates for `navigation3-ui` — same package names, Android-only build.

**Note (scope correction from the original step text):** the step as originally decomposed also
called for writing `NavKeySerializers.kt` — a `SerializersModule` registering all route classes as
`polymorphic(NavKey::class) { subclass(RouteClass::class) }`. That does **not** compile yet:
kotlinx.serialization's `PolymorphicModuleBuilder<Base>.subclass<T : Base>` requires `T` to be a
compile-time subtype of `Base` (`NavKey`), and confirmed by decompiling
`androidx.navigation3.runtime.NavKey` from the now-fetched jar
(`navigation3-runtime-desktop-1.1.1.jar`) that it's a bare marker interface with no members — so
every route class must `implement NavKey` *before* the serializers file can be written. Route
classes live across ~15 different feature modules, not just `composeApp`, so making them implement
`NavKey` means adding the Nav3 dependency to each of those modules too — that's real work
(step 7's "mechanical, one line per file" already describes it), not something that belongs in a
dependencies-only step. **Moved the whole `NavKeySerializers.kt` deliverable into step 7**, which
already owned "make routes implement `NavKey`" — see CHECKLIST.md's updated step 7. Also counted
the actual route classes while investigating: 39, not the ~31 estimated in the original
investigation (step 5) — grepped every `composable<T>` in `composeApp/src/commonMain` plus
`WikiNavDestination` (used only as a `DrawerDestination` marker, never itself a `composable<T>`,
per `DrawerDestination.kt:30`) and the 3 `Scrum*Destination` objects (named `*Destination`, not
`*NavDestination`, so they didn't match the investigation's naming-based estimate). Full list for
step 7 to consume: `AttributesScreenNavDestination`, `CreateTaskNavDestination`,
`DashboardNavDestination`, `EpicDetailsNavDestination`, `EpicsNavDestination`,
`IssueDetailsNavDestination`, `IssuesNavDestination`, `KanbanNavDestination`, `LoginNavDestination`,
`ModulesNavDestination`, `ProfileNavDestination`, `ProjectDetailsNavDestination`,
`ProjectSelectorNavDestination`, `ProjectValuesMenuNavDestination`, `ProjectValuesNavDestination`,
`ScrumBacklogDestination`, `ScrumClosedSprintsDestination`, `ScrumOpenSprintsDestination`,
`SettingsAboutScreenRouteNavDestination`, `SettingsInterfaceScreenNavDestination`,
`SettingsNavDestination`, `SettingsUserScreenNavDestination`, `SprintNavDestination`,
`TagsScreenRouteNavDestination`, `TaskDetailsNavDestination`, `TeamNavDestination`,
`TrustedCertificatesNavDestination`, `UserStoryDetailsNavDestination`,
`WikiCreateLinkNavDestination`, `WikiCreatePageNavDestination`, `WikiLinksNavDestination`,
`WikiNavDestination`, `WikiPageNavDestination`, `WikiPagesNavDestination`,
`WorkItemEditDescriptionNavDestination`, `WorkItemEditEpicNavDestination`,
`WorkItemEditSprintNavDestination`, `WorkItemEditTagsNavDestination`,
`WorkItemEditTeamMemberNavDestination`.

**Verify:** `./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`,
`:composeApp:compileKotlinIosArm64 --rerun-tasks`, `:androidApp:compileFdroidDebugKotlin
--rerun-tasks`, `:composeApp:compileKotlinJvm` — all four green; the new dependencies resolve and
sit unused, so nothing else could have broken.

**Next:** step 7 — implement `NavKey` on the 39 route classes above, write
`NavKeySerializers.kt`, and build the `Navigator`/`NavigationState` shell.

## Step 7: Implement `NavKey` + `NavKey` serializers + build the `Navigator`/`NavigationState` shell — ✅ done 2026-08-15

All 39 route classes (CHECKLIST-DONE.md step 6's Note) now `implement NavKey` — one line per file,
mechanical per the step's own description; the two plain `object` routes (`ModulesNavDestination`,
`ProjectDetailsNavDestination`, as opposed to `data object`) needed the same treatment, missed by
an initial grep pass keyed on `data object`/`data class` only.

**Dependency wiring — reused `core:navigation` instead of touching 15 build files individually.**
`core:navigation` already existed (Nav2 extension functions, `NavigationExtensions.kt`) and was
already an `implementation` dependency of every feature `*/ui` module owning a route class except
one (`feature/workitem/ui`, fixed here). Rather than adding `libs.jetbrains.navigation3.ui`
per-module as the step's text suggested, it went on `core:navigation`'s own `build.gradle.kts` as
`api(libs.jetbrains.navigation3.ui)` (plus `api(libs.jetbrains.androidx.savedstate)` for
`SavedStateConfiguration`, part of `rememberNavigationState`'s public signature, and
`implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)` for the internal-only
`rememberViewModelStoreNavEntryDecorator`) — `api` because `NavKey` needs to be visible to every
module that has `implementation(projects.core.navigation)`, not just to `core:navigation` itself.
Confirmed by the full four-target compile matrix passing with `NavKey` resolved in all 15 feature
modules. This is the "slimmer dependency that transitively pulls in `NavKey`" the step's own text
flagged as worth looking for — it turned out to already exist as this repo's module graph, not as
a smaller Maven artifact.

**`Navigator`/`NavigationState` — ported verbatim into `core:navigation`, no dependency cycle.**
wallosmobile's `Navigator.kt`/`NavigationState.kt` (read directly from
`~/proj/grappim/wallosmobile/core/navigation/`) are already fully generic over `NavKey` — no
concrete route imports, `topLevelKeys`/`configuration` passed in by the caller — so they ported
with only the package rename, no adaptation needed. Included `NavigatorTest.kt` (wallosmobile's,
same rename), 10 cases, all passing.

**`NavKeySerializers.kt` — put in `composeApp`, not `core:navigation`, despite the step's text
suggesting the latter as one option.** It has to reference all 39 concrete route classes across 15
feature modules, and those modules depend on `core:navigation` — putting the serializers file
there would be a real dependency cycle (Gradle would reject it), not just an apparent one like the
`:testing`-module case in CLAUDE.md's Testing section (that one works because test/main source
sets are separate compilations; this one is a real `commonMain`-to-`commonMain` edge). `composeApp`
already imports every route class (`MainNavHost.kt`/`nav/*.kt`), so it's the only module that can
host this file without restructuring the dependency graph. Followed wallosmobile's
`NavKeySerializers.kt` shape exactly: `navKeySerializersModule` (the `SerializersModule`) plus
`navSavedStateConfiguration` (the `SavedStateConfiguration` wrapping it, for step 10's
`rememberNavBackStack` calls). Added `NavKeySerializersTest.kt` (`composeApp/src/commonTest/`) —
adapted from wallosmobile's version, which checks against its `DrawerDestination` enum (no
equivalent exists in this repo yet); here it asserts all 39 fully-qualified class names resolve via
`navKeySerializersModule.getPolymorphic(NavKey::class, serializedClassName)` instead.

Neither `Navigator`/`NavigationState` nor `NavKeySerializers.kt` is wired into the running app yet
— `MainNavHost.kt` is untouched, still on classic `NavHost`, as scoped.

**Verify:** four-target compile matrix (`:composeApp:compileKotlinIosSimulatorArm64
--rerun-tasks`, `:composeApp:compileKotlinIosArm64 --rerun-tasks`,
`:androidApp:compileFdroidDebugKotlin --rerun-tasks`, `:composeApp:compileKotlinJvm`) all green.
`./gradlew jvmTest` (full repo run) green, including the 10 new `NavigatorTest` cases and the new
`NavKeySerializersTest` case. `./gradlew ktlintCheck` green — two rounds of
`ktlintCommonMainSourceSetFormat` needed (one for the composeApp/feature-module `NavKey` import
insertions, a second after adding `navSavedStateConfiguration`'s `SavedStateConfiguration` import to
`NavKeySerializers.kt`), both auto-fixed import ordering only, per CLAUDE.md's
`standard:import-ordering` note.

**Next:** step 8 — port the 15 affected ViewModels off `savedStateHandle.toRoute<T>()` to
constructor-parameter injection via Koin `@InjectedParam` + `parametersOf(route)`.

## Step 8: Port ViewModels off `savedStateHandle.toRoute<T>()` — ✅ done 2026-08-15

All 15 ViewModels moved from `SavedStateHandle.toRoute<T>()` to `@InjectedParam private val route:
XNavDestination` — the whole route object as a single injected param, not wallosmobile's per-field
`@InjectedParam`s (deliberate simplification, matches the step text's own `parametersOf(route)`
wording; full rationale in IMPLEMENTATION_PLAN.md's "Step 8 notes"). Each affected Screen composable
gained a `route: T` parameter and `koinViewModel { parametersOf(route) }`; each NavGraph's
`composable<T> { }` call gained a `backStackEntry ->` parameter and `route =
backStackEntry.toRoute()`. Touched all 15 ViewModel files, their Screens, and 8 NavGraph files
(`MainNavHost.kt` + 7 `composeApp/.../nav/*.kt` graphs — `WorkItemEditsNavGraph.kt` alone covers 5
routes). Also updated the 15 `*ViewModelTest.kt` files, 2 `*ScreenTest.kt` Compose-UI-test pilots,
and `KoinGraphTest.kt` (removed its now-dead `single { SavedStateHandle() }` registration and stale
doc comment) to match.

**Note (deviation caught during verify, not scoped in the original step text):**
`NavBackStackEntry.toRoute<T>()` has no `typeMap` parameter — unlike
`SavedStateHandle.toRoute<T>(typeMap = ...)`, which the old ViewModel code used for routes with a
non-primitive field (`CreateTaskNavDestination`'s `CommonTaskType`, the 5
`WorkItemEdit*NavDestination`'s `TaskIdentifier`). First-pass NavGraph edits copied the old
`typeMap = ...` call shape and failed to compile across 6 call sites; fixed by reading
`navigation-common-desktop-2.9.2-sources.jar` directly — the entry-level `toRoute` already reads the
typeMap off `destination.arguments`, populated by the enclosing `composable<T>(typeMap = ...)`, so a
bare `backStackEntry.toRoute()` is correct. Full detail in IMPLEMENTATION_PLAN.md's "Step 8 notes".

**Verify:** `./gradlew jvmTest` (full repo run, green — no test changes needed for `KoinGraphTest`
itself; it already tolerates the 15 ViewModels' new `DefinitionParameterException` the same way it
tolerated their old `toRoute()` failures, confirmed by running it unchanged before touching it) and
`ktlintCheck` green (one `standard:function-expression-body` violation auto-fixed via
`ktlintCommonTestSourceSetFormat` in `ProjectSelectorViewModelTest.kt`). Four-target compile matrix
(`:composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`, `:composeApp:compileKotlinIosArm64
--rerun-tasks`, `:androidApp:compileFdroidDebugKotlin --rerun-tasks`, `:composeApp:compileKotlinJvm`)
all green. `KoinGraphTest` run explicitly — confirms the `@InjectedParam` wiring resolves (all 147
definitions checked, the 15 route-taking ViewModels report the expected tolerated
`DefinitionParameterException`, none report `NoDefinitionFoundException`).

**Next:** step 9 — port `UPDATE_DATA_ON_BACK` result-passing to the Nav3 event-bus recipe. The one
genuinely non-mechanical piece of the migration.

## Step 9: Port `UPDATE_DATA_ON_BACK` result-passing to the Nav3 event-bus recipe — ✅ done 2026-08-15

**Blocker discovered mid-step, resolved by asking gregory rather than picking silently:** the real
Nav3 event-bus recipe (`ResultEventBus`/`LocalResultEventBus`/`ResultEffect`/
`rememberResultEventBusNavEntryDecorator`, `androidx.navigation3.runtime.result`) does not exist in
`androidx.navigation3:navigation3-runtime` until `1.2.0-alpha04` — confirmed by pulling the actual
jars, `1.1.1` through `1.1.6` have no `result` package at all. This project is pinned to the stable
`1.1.1` line (`jetbrainsNav3` in `gradle/libs.versions.toml`, step 6), and the JetBrains
KMP-published `navigation3-ui` (needed for iOS/desktop) only reaches `1.2.0-alpha02`, which itself
pulls `navigation3-runtime:1.2.0-alpha04` — so the official recipe is unreachable without adopting
an alpha dependency across the whole Nav3 stack. Both documented recipes (event-based and
state-based) depend on the same missing classes; there is no stable-version fallback. Presented
three options (hand-roll, bump to alpha, defer to step 10); gregory chose hand-roll.

**`ResultBus`** (`core/navigation/src/commonMain/.../ResultBus.kt`) is a from-scratch class that
mirrors the upstream `ResultEventBus`'s shape exactly — same method names (`sendResult`,
`getResultFlow`), same buffered-`Channel`-per-key semantics (`Channel.BUFFERED` +
`BufferOverflow.SUSPEND`, one channel per result key, created lazily) — so call sites read the same
as the real recipe and swapping to it later, once this project's Nav3 pin moves off 1.1.x, is a
search-and-replace. `LocalResultBus` (`staticCompositionLocalOf<ResultBus>`, top-level `val`, not
nested in an object — matches this repo's existing `LocalScreenReadySignal`/`LocalOfflineState`
convention, not androidx's own nested-object pattern) and `ResultEffect` (both the
`resultKey: String` and `reified T` overloads) round out the API. `ResultBus`'s own logic
(`sendResult`/`getResultFlow`) is unit-tested in `ResultBusTest.kt`, following `NavigatorTest.kt`'s
precedent of testing only the non-`@Composable` core — the `@Composable` wrappers are exercised via
emulator instead, same split as `NavigationState.kt`.

**Wiring**: `MainNavHost.kt` wraps its `NavHost { }` call in
`CompositionLocalProvider(LocalResultBus provides rememberResultBus())`, so every `composable<T>`
destination underneath can read `LocalResultBus.current` — this works today, before Nav3's
`NavDisplay` exists, because `CompositionLocalProvider` is a plain Compose primitive with no
dependency on which navigation library owns the tree. A single shared marker object,
`UpdateDataOnBack` (`MainNavHost.kt`, replacing the old `UPDATE_DATA_ON_BACK` string constant),
is sent by every detail screen's `goBack` (`IssueDetailsNavDestination`, `EpicDetailsNavDestination`,
`TaskDetailsNavDestination`, `UserStoryDetailsNavDestination`, `SprintNavDestination`) and consumed
by every list/board screen via `ResultEffect<UpdateDataOnBack> { updateData = true }` writing into a
local `var updateData by remember { mutableStateOf(false) }` — preserving each Screen's existing
`updateData: Boolean` parameter unchanged, so no feature-module code needed touching, only the 6
nav-graph files (`MainNavHost.kt`, `IssueNavGraph.kt`, `EpicNavGraph.kt`, `ScrumNavGraph.kt`,
`TaskNavGraph.kt`, `UserStoryNavGraph.kt`) plus the new `core:navigation` file. One shared signal
type is a faithful port of the old convention (`UPDATE_DATA_ON_BACK` was itself one global string
key for every screen) and is safe under Nav2's current NavHost because only the top (currently
visible) destination's `composable<T>` body is actively composed at a time — no cross-screen
collision is possible even though the channel key is global.

**Note (ktlint traps hit while wiring this in, not scoped in the original step text):** adding
`LocalResultBus` tripped `compose:compositionlocal-allowlist` — this repo's ktlint compose-rules
config allowlists `CompositionLocal`s by name via `.editorconfig`'s
`compose_allowed_composition_locals` (`LocalTopBarConfig, LocalFilePicker, LocalScreenReadySignal,
LocalOfflineState`), not detekt's own `CompositionLocalAllowlist` (which is `active: false` and
irrelevant here). Added `LocalResultBus` to that list — `.editorconfig` is one of the files
`.github/workflows/guardrails.yml` gates, so the commit carries a `Gate-change:` line. Separately,
`var updateData by mutableStateOf(false)` without `remember` tripped `compose:remember-missing-check`
across all 4 files that use the pattern — CLAUDE.md's Testing section already documents
`standard:function-signature`'s "would fit on one line" trap for test code; this is the same shape
of trap for production Composable code, worth remembering for future `mutableStateOf` usage inside
a `composable<T> { }` body.

**Verify:** `./gradlew jvmTest` (full repo run, green, including the 3 new `ResultBusTest` cases)
and `ktlintCheck` (green after the `.editorconfig` allowlist addition and `ktlintCommonMainSourceSetFormat`
auto-fixes). Four-target compile matrix
(`:composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`, `:composeApp:compileKotlinIosArm64
--rerun-tasks`, `:androidApp:compileFdroidDebugKotlin --rerun-tasks`, `:composeApp:compileKotlinJvm`)
all green. Emulator verification on `Medium_Phone_API_36.1` (`emulator-testing` skill): changed user
story #11's status from Kanban, backed out, confirmed the "NEW" column count dropped 4→3 with the
card gone (not stale); changed task #31's status from Sprint 1's taskboard, backed out, confirmed
the "NEW" column lost that card too. No crash either time (`adb logcat` checked for
`FATAL EXCEPTION`/`AndroidRuntime`, none found).

**Next:** step 10 — replace `NavHost`/`composable<T>` with `NavDisplay`/`entry<T>`. Gated — confirm
with gregory before starting given the blast radius (no supported Nav2/Nav3 coexistence path).

## Step 10: Replace `NavHost`/`composable<T>` with `NavDisplay`/`entry<T>` — the cutover — ✅ done 2026-08-21

All 27 `fun NavController.navigateToX()` extensions across `feature/*/ui` + `composeApp` became
`fun Navigator.navigateToX()`; all 8 `NavGraphBuilder` files became `EntryProviderScope<NavKey>`
files (`composable<T> { backStackEntry -> ... toRoute() }` → `entry<T> { route -> ... }`);
`MainNavHost.kt`'s `NavHost` became `NavDisplay` fed by `navigationState.toEntries(entryProvider)`.
Deleted `core/navigation/NavigationExtensions.kt` (Nav2-only `popUpToTop`/`navigateAndPopCurrent`,
replaced by two new `Navigator` methods — `resetTo`/`replaceCurrent`, see Note below) and the whole
`utils/ui` `JsonSerializableNavType`/`typeMapOf` file family (commonMain + 3 platform actuals + 2
test files — Nav2 `NavType` machinery with no callers left once every `composable<T>(typeMap =
...)` became a plain `entry<T>`). Removed Nav2 itself
(`org.jetbrains.androidx.navigation:navigation-compose`) from `KmpCompose.kt`'s convention plugin
and the version catalog, once a repo-wide grep for `androidx.navigation.` imports (excluding the
different `navigation3`/`navigationevent` artifacts) came back empty.

**Note: step 11 landed in this same commit, not as its own step** — see
IMPLEMENTATION_PLAN.md's "Step 10 notes" for why they can't be separated (`MainAppState.kt`'s
`currentTopLevelDestination`/`isTopBarVisible` read the Nav2 `NavController` directly, and that
type stops existing the moment this step deletes `NavController` — there's no compiling
intermediate state between the two). `TaigaDrawerWidget.kt` needed no changes; all the Nav2-specific
selection logic lived in `MainAppState.kt` alone.

**Note: step 7's `Navigator`/`NavigationState` had two related bugs, both fixed here** — full
mechanism in IMPLEMENTATION_PLAN.md's "Step 10 notes". Short version: top-level key comparison was
by `equals()`, which silently breaks the moment a top-level route carries a payload
(`ProjectSelectorNavDestination(isFromLogin: Boolean)`, whose two real call sites always pass
`isFromLogin = true`, never matching the `isFromLogin = false` instance that seeded
`NavigationState.subStacks` at startup). Fixed by keying `subStacks` on `KClass<out NavKey>` instead
of the `NavKey` instance, comparing by `::class` throughout `Navigator`, and having `goToTopLevel`/
`resetSubStackTo` write the fresh key into the target section's sub-stack root — `toEntries()`
renders from `subStacks`, not `topLevelStack`, so the class-comparison fix alone would have reported
the right section while still rendering the wrong (stale-payload) screen instance. Four new
`NavigatorTest` cases cover it.

**Note: `navigateToUserStory`'s `popUpToRoute` parameter was NOT dead code** — an earlier pass at
scoping this step read only part of `TaskNavGraph.kt` and concluded it had no live callers; the
real call site (`goToUserStory`, popping `TaskDetailsNavDestination` when drilling from a task into
its parent user story) was further down the same file. Ported to `Navigator.replaceCurrent(key)`
(pop the current sub-stack entry, then push) rather than dropped — the same primitive also backs
`WikiPageNavDestination.navigateToWikiPage`'s `popUpToRoute` (a real, two-call-site usage in
`WikiNavGraph.kt`). Re-read a file's full contents before deleting a parameter on the strength of a
partial grep.

**Verify:** `./gradlew jvmTest` (full suite including 4 new `NavigatorTest` cases), `ktlintCheck`
(one `standard:function-signature` hit — the same over-120-char-unwrapped trap CLAUDE.md's Testing
section documents for test code, turns out to bite production `fun Navigator.xxx()` one-liners too
— fixed by `ktlintCommonMainSourceSetFormat`), `koverXmlReport`/`:koverVerify` (floor holds), and
all four target compiles (`:composeApp:compileKotlinJvm`,
`:composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`, `:composeApp:compileKotlinIosArm64
--rerun-tasks`, `:androidApp:compileFdroidDebugKotlin --rerun-tasks`). Full emulator walkthrough
(`emulator-testing` skill) on `Medium_Phone_API_36.1`: cold-start-already-logged-in landed on
Dashboard directly (`resetTo` cold-start path), back from Dashboard exits to the launcher rather
than reaching Login/ProjectSelector, every drawer section opened and highlighted correctly (Board,
Epics, Issues, Kanban, Team, Wiki Bookmarks, Wiki All Pages + a page detail, Scrum Backlog, Open
Sprints + a sprint detail, Settings + Modules), Kanban → task detail → switch to Team → switch back
to Kanban landed on the task detail rather than the board root (sub-stack survives a section
switch), changing that task's status and backing out updated the Kanban board's column count
(`UpdateDataOnBack` still fires through `NavDisplay`/`Navigator.goBack()`), and logout landed
cleanly on Login with back exiting to the launcher — no crashes in logcat. `Medium_Tablet`
(abbreviated, doubling as step 11's own scenario): `NavigationRail` renders every section, Board
highlighted on launch, tapping Epics switched both the rail highlight and the content correctly.

**Gate-change note:** this commit touches `build-logic/` (`KmpCompose.kt`, removing the Nav2
dependency) — the guardrails wire trips on the path regardless of content, so the commit carries a
`Gate-change:` line even though nothing about detekt/ktlint/kover actually changed.

**Next:** step 12 — add `ListDetailSceneStrategy` for list-detail two-pane layouts. Gated — three
open questions in its own CHECKLIST.md entry needed answers from gregory before it could be
scoped; all three resolved 2026-08-21 (see step 12a below, and step 12's own decomposition into
12a/12b/12c).

## Step 12a: Fix the `ResultBus` collision for the Issues pairing — ✅ done 2026-08-21

Added `private data object IssueListUpdateDataOnBack` at the top of `IssueNavGraph.kt`. Changed
two of the three sites the checklist named: `IssuesNavDestination`'s list-refresh listener
(`ResultEffect<IssueListUpdateDataOnBack>`) and `IssueDetailsNavDestination`'s `goBack`
(`resultBus.sendResult(IssueListUpdateDataOnBack)`) — a matched send/receive pair fully contained
in this file. `IssueDetailsNavDestination`'s own self-refresh listener was **left on the shared
`UpdateDataOnBack` key**, unchanged: its sender is `UserStoryNavGraph.kt`/`TaskNavGraph.kt`'s
`goBack`, both out of this MVP's scope, so it has to keep matching whatever key those files still
send. Full reasoning folded into IMPLEMENTATION_PLAN.md's "Step 12 pre-scoping" section.

**Note: the manual regression check surfaced that `goToUserStory` from Issue detail is not a
general "linked story" link** — it only fires via the promote-to-user-story action
(`IssueDetailsViewModel.promoteToUserStory()`). Corrected mid-session after gregory caught the
wrong assumption; the domain fact and its consequence for step 12c's test plan are now recorded in
IMPLEMENTATION_PLAN.md alongside the fix above, so 12c doesn't have to re-derive it.

**Verify:** `./gradlew jvmTest` and `ktlintCheck` both green. Manual regression check on the
desktop build (`:composeApp:run`): opened an Issue's detail, edited its description, went back —
the Issues list picked up the change, confirming the renamed key still delivers the signal
end-to-end in today's single-pane mode.

**Also found (not fixed, logged to `docs/revisit.md` #42–44):** the nav rail renders on the Login
screen at wide window widths (`MainScreen.kt:182-230`, ungated on login state); the Issues list has
no row dividers on desktop; the desktop build has no non-touch equivalent for pull-to-refresh.

**Next:** step 12b — wire `ListDetailSceneStrategy` for Issues list-detail. Still gated — do not
start without asking.
