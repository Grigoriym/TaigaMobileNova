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
