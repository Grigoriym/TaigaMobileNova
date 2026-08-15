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
