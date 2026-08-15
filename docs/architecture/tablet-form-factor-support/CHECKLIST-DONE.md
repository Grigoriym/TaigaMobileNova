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
