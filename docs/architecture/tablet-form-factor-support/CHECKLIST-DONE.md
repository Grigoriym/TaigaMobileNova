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
