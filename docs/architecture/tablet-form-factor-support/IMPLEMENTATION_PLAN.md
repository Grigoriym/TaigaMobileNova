# Tablet and Other Form Factor Support — Plan

Reference doc for this initiative: architecture, rationale, tradeoffs. See
[CHECKLIST.md](CHECKLIST.md) for the executable steps.

## Current state (evidence, gathered 2026-08-15)

This is the pre-initiative baseline — the survey that motivated the scope options below. Option 2
(steps 2–4) has since shipped a width-gated `NavigationSuiteScaffold`/`ModalNavigationDrawer`
split, so the "drawer is always modal" and "no window-size-class awareness" bullets no longer hold;
see [CHECKLIST-DONE.md](CHECKLIST-DONE.md) steps 2–4 for what changed. The rest of this section
(single-pane `NavHost`, unconstrained desktop window, no content width cap beyond step 1's
unwired `TaigaAdaptiveContent`, no iOS device-family check) is still accurate.

The app originally had **no form-factor adaptation at all** — it was built single-pane,
phone-shaped, on every platform:

- **Navigation is single-pane only.** `MainNavHost` (`composeApp/.../main/MainNavHost.kt`) is one
  `NavHost` with a linear back stack — no list-detail or two-pane split anywhere in the codebase.
- **The drawer is always modal.** `TaigaDrawerWidget.kt:37` uses `ModalNavigationDrawer`
  unconditionally — it never becomes a permanent side rail, regardless of available width.
- **No window-size-class awareness exists anywhere.** Zero hits in the repo for
  `WindowSizeClass`, `NavigationSuiteScaffold`, `NavigationRail`, `PermanentNavigationDrawer`, or
  `material3-adaptive*`. None of the adaptive Material3 artifacts are in `gradle/libs.versions.toml`.
- **Desktop opens at a phone-shaped window.** `TaigaMobileDesktop.kt:48`:
  `rememberWindowState(width = 600.dp, height = 800.dp)` — a portrait phone aspect ratio, even
  though desktop windows are freely resizable and users commonly run apps much wider. Nothing
  reacts if the user resizes the window.
- **Android manifest declares no large-screen posture.** `androidApp/src/main/AndroidManifest.xml`
  has no `resizeableActivity` override (defaults to `true` on API 24+, so split-screen/multi-window
  already works at the OS level) and no size-specific `<layout>` block — but nothing in the UI
  tailors itself to the extra space multi-window or a tablet grants.
- **iOS `Info.plist` has no device-family override** (`UIDeviceFamily`) — whether iPad is even an
  enabled target device needs checking in the Xcode target's build settings, not just the plist,
  before iPad-specific work is worth planning.
- **Content isn't width-constrained.** 47 files call bare `fillMaxWidth()` with no cap; only two
  widgets in the whole repo (`ClickableBadge.kt`, `WorkItemClickableBadgeWidget.kt`) use
  `widthIn(max = ...)`, and neither is a screen-level layout constraint. On a wide tablet or
  desktop window, list rows, forms, and body text stretch edge-to-edge.
- **The top bar is a single global bar with no reserved rail region** — confirmed via
  [top-app-bar.md](../top-app-bar.md); `TaigaTopAppBar` is a `CenterAlignedTopAppBar`, nothing more.

In short: a tablet, a Chromebook in multi-window, or a wide desktop window all just render the
phone layout stretched wider. Nothing is broken, but nothing takes advantage of the space either.

## Scope options

"Tablet and other form factor support" spans a wide range of actual work. These build on each
other (later options assume the earlier ones), but each is independently shippable, and the cost
and risk rise steeply from top to bottom:

1. **Cosmetic width cap.** Cap content width on large screens (`widthIn(max = X.dp)` + center) so
   text/forms/lists don't stretch edge-to-edge. Small, low-risk, touches layout code only — no
   navigation or architecture change. Fixes the worst visual artifact but doesn't use the extra
   space for anything.

2. **Adaptive navigation chrome.** Swap the always-modal `ModalNavigationDrawer` for
   `NavigationSuiteScaffold` so wide screens (tablet landscape, Android large-screen multi-window,
   desktop) get a permanent rail/drawer instead of a modal overlay. Medium: touches
   `MainScreen.kt`, `TaigaDrawerWidget.kt`, adds a new dependency
   (`material3-adaptive-navigation-suite`). **KMP target coverage confirmed 2026-08-15**: the
   artifact was *not* multiplatform originally (`JetBrains/compose-multiplatform#4952`), fixed via
   `compose-multiplatform-core#1539` and published since `1.7.0-beta02`; at `1.10.0-alpha05`
   (matching this repo's pinned `jetbrainsComposeMaterial3`), its `.module` metadata lists Android
   (embedded debug/release variants), Desktop/JVM (`-desktop`), and iOS (`-uikitarm64`,
   `-uikitsimarm64` — CMP's internal names for `iosArm64`/`iosSimulatorArm64`) variants, covering
   all three of this project's targets.

   **Open design gap found the same day, not yet resolved:** `NavigationSuiteScaffold`'s
   `navigationSuiteItems: NavigationSuiteScope.() -> Unit` only supports flat `item()` calls
   (icon/label/selected/onClick) — there is no header, group-label, or divider slot. The current
   `TaigaDrawerWidget` (`composeApp/.../TaigaDrawerWidget.kt`) renders an app-name header, grouped
   sections via `DrawerItem.Group` with a label per group, `DrawerItem.Divider`s, and a Logout
   entry — none of which has a direct equivalent in the rail/permanent-drawer item model. Porting
   this 1:1 isn't possible; a compromise has to be picked. See CHECKLIST.md step 3.

3. **List-detail two-pane layouts.** E.g. Kanban/Sprint board shown side-by-side with task detail
   on wide screens. Large — and structurally blocked today: the `android-skills:adaptive` skill's
   multi-pane guidance (its Step 3, list-detail via `ListDetailSceneStrategy`) is built entirely on
   **Jetpack Navigation 3**'s `NavDisplay`/`SceneStrategy` model. This app uses classic Navigation
   Compose (`androidx.navigation.compose` — `NavHost` + `composable<T>` route objects, see
   `MainNavHost.kt`), not Navigation 3. Getting a real two-pane list-detail scaffold means either
   migrating navigation to Nav3 first (a separate, large, independently-scoped project — see the
   `android-skills:navigation-3` skill) or hand-rolling a pane split without the SceneStrategy
   machinery. This option's true cost is dominated by that navigation-model question, not by the
   pane layout itself.

4. **Full responsive redesign per screen.** Kanban board columns, sprint board, dashboard grids
   reflow at breakpoints, adaptive grid counts for lists (`LazyVerticalGrid` +
   `GridCells.Adaptive`), etc. Largest scope — screen-by-screen work across every feature module.

## Platform-specific notes

- **Desktop** already hands users a resizable window today, so (1) alone is an immediate,
  low-cost win there — the app currently looks worst on desktop precisely because nothing
  constrains width on the platform most likely to be run wide.
- **Android tablets, Chromebooks, and split-screen/multi-window** are exactly what (2) targets, and
  are also what Play Store's large-screen quality guidelines check for.
- **iPad** needs a target-device-family check first (Xcode build settings) — if iPad isn't even an
  enabled device family today, that's a smaller, separate prerequisite before any of this matters
  on iOS.

## Decision status

**Decided 2026-08-15: pursue option 2 (adaptive navigation chrome) next.** gregory expects option 3
(list-detail two-pane) to follow eventually, and specifically wants the Navigation 3 migration it's
blocked on informed by `wallosmobile` (`~/proj/grappim/wallosmobile/`) — a sibling project built on
the same architecture as this one but already built on Nav3 from inception (`jetbrainsNav3` /
`org.jetbrains.androidx.navigation3:navigation3-ui` in its version catalog). **Started 2026-08-15**
as option 3's step 5, the Nav3 migration investigation — see "Navigation 3 migration investigation"
below for findings, sizing, and the proposed (not yet decomposed) step breakdown. Option 4 remains
undecided and isn't blocking anything.

Option 2 is decomposed into CHECKLIST.md steps 2–4.

## Step 3 decision (2026-08-15)

**Chosen mapping: dual-path, width-gated.**

- **Compact width** (phone, narrow multi-window): unchanged. `TaigaDrawerWidget`'s current
  `ModalNavigationDrawer` keeps rendering `DrawerItem` exactly as today — app-name header,
  `DrawerItem.Group` labels, `DrawerItem.Divider`, the full grouped experience.
- **Medium/expanded width** (tablet, wide multi-window, desktop): `NavigationSuiteScaffold`
  renders a *flattened* item list built from the same `ImmutableList<DrawerItem>` —
  `DrawerItem.Group` is unwrapped to its inner `Destination`s with no group-label text,
  `DrawerItem.Divider` is dropped, and there's no app-name header (the `navigationSuiteItems`
  API has no slot for any of the three). Item order is otherwise preserved.
- gregory picked this over flattening uniformly at all widths (rejected: changes the phone
  drawer's current grouped look, which is used far more than tablet/desktop today) and over
  investigating a multi-section API (rejected: the 2026-08-15 investigation already found no
  header/group-label/divider slot in the flat `item()` API — likely a dead end not worth a
  session).
- Step 4 implements this as two rendering paths sharing one `DrawerItemsBuilder` output — likely
  a `flattenForNavigationSuite(items: ImmutableList<DrawerItem>): List<DrawerItem.Destination>`
  helper (unit-testable in `commonTest`) plus a width check (`currentWindowAdaptiveInfo()` →
  `NavigationSuiteType`) choosing which of the two widgets renders.

## Step 4 notes (2026-08-15) — implementation and a dependency gap the earlier steps missed

Implemented as described above. Two things worth recording for future adaptive-navigation work in
this repo:

**`material3-adaptive-navigation-suite` alone is not enough to call `NavigationSuiteScaffold` with
its own default `layoutType`.** Its `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(...)`
and the `currentWindowAdaptiveInfo()`/`WindowAdaptiveInfo` types it needs live in a **separate**
artifact, `org.jetbrains.compose.material3.adaptive:adaptive` — own release train (`1.3.0-beta02`
at time of writing), unrelated to `jetbrainsComposeMaterial3`'s `1.10.0-alpha05`. Step 2's `.module`
inspection (see the scope-options entry above) checked the navigation-suite artifact's own platform
variants but not its dependency graph, so this didn't surface until step 4 tried to compile against
`currentWindowAdaptiveInfo()`. Added as `jetbrainsComposeMaterial3Adaptive` /
`jetbrains-compose-material3-adaptive` in `gradle/libs.versions.toml`. If a future adaptive-API
addition in this repo hits an unresolved reference from a `androidx.compose.material3.adaptive.*`
type, check whether it's actually two JetBrains artifacts before assuming a version mismatch.

**Use the V2/breakpoint APIs, not the ones the M3 docs lead with.** `currentWindowAdaptiveInfo()`
(no suffix) and `WindowSizeClass.windowWidthSizeClass`/`WindowWidthSizeClass` are all deprecated in
favor of `currentWindowAdaptiveInfoV2()` and `WindowSizeClass.isWidthAtLeastBreakpoint(dp)` /
`isAtLeastBreakpoint(w, h)` — the compiler flags all three with a deprecation warning, but the code
still compiles, so it's easy to ship the deprecated path unnoticed. `MainScreen.kt` uses
`currentWindowAdaptiveInfoV2()` + `windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.
WIDTH_DP_MEDIUM_LOWER_BOUND)` for the compact/non-compact split.

**`calculateFromAdaptiveInfo` does not guarantee a permanent drawer at expanded width.** Verified
on `Medium_Tablet` (2026-08-15): a genuinely EXPANDED-width AVD (landscape, ~1280dp — well past the
840dp expanded breakpoint) still resolved to `NavigationSuiteType.NavigationRail`, not
`NavigationDrawer`. Don't assume "expanded width" in a future design doc or bug report implies a
permanent drawer actually rendered — check the resolved `NavigationSuiteType`, or just treat "rail
or drawer" as one outcome the way step 3's design decision already does.

## Navigation 3 migration investigation (step 5, 2026-08-15)

gregory confirmed starting option 3, gated on this investigation. No code changed — findings only.

### What wallosmobile actually does

wallosmobile was built on Nav3 **from inception**, not migrated (`core:navigation` is its very
first navigation commit, "1.7 — core:navigation" — there is no pre-Nav3 history to compare
against). It does **not** use `ListDetailSceneStrategy` anywhere (confirmed by grep — zero hits for
`SceneStrategy`/`ListDetail` in the repo); its Nav3 usage is single-pane `NavDisplay` only. So it's
useful for the *shell/state* pattern below, but tells us nothing about the pane-splitting piece
option 3 actually needs — that came from the Android skill (next section).

**Artifacts** (`gradle/libs.versions.toml`): `jetbrainsNav3 = "1.1.1"` →
`org.jetbrains.androidx.navigation3:navigation3-ui`, plus
`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`.

**Dual back-stack shell**, `core/navigation/` (`Navigator.kt`, `NavigationState.kt`):
- `NavigationState` holds one `topLevelStack: NavBackStack<NavKey>` (which drawer section is
  active, itself a stack so re-entering a section restores its position) and a
  `subStacks: Map<NavKey, NavBackStack<NavKey>>` — one independent back stack per top-level
  section (`Dashboard`, `Subscriptions`, `Settings`, ... — `DrawerDestination.kt`). Switching
  sections doesn't lose either section's history.
- `Navigator.navigate(key)` (`Navigator.kt:14`) branches three ways: re-tapping the active section
  resets it to root, tapping another top-level section switches to it, anything else pushes onto
  the active section's sub-stack.
- `NavigationState.toEntries()` (`NavigationState.kt:69`) decorates every sub-stack on every
  composition (`rememberSaveableStateHolderNavEntryDecorator` +
  `rememberViewModelStoreNavEntryDecorator`) and flattens only the *active* stacks into the list
  `NavDisplay` renders.
- **Route args are not read via `SavedStateHandle` at all.** `MainNavHost.kt`'s `entry<T> { route ->
  ... }` lambda hands the route object straight to the screen (`SubscriptionsEntryProvider.kt:24`,
  passing `route.subscriptionId`), and the ViewModel receives it as a plain constructor parameter
  via Koin `@InjectedParam` + `parametersOf` (`SubscriptionDetailViewModel.kt:39` and, further up
  the call chain, `koinViewModel<T> { parametersOf(key) }`) — there is no `toRoute<T>()` step at
  all. **This is the biggest single change this migration would force on every ViewModel in this
  repo's Navigation Pattern section** (currently `savedStateHandle.toRoute<T>()`).
- **KMP-required workaround, not optional**: `NavKeySerializers.kt` builds a `SerializersModule`
  registering every route as a `polymorphic(NavKey::class) { subclass(...) }`, passed to
  `rememberNavBackStack` via a `SavedStateConfiguration`. This isn't a wallosmobile design choice —
  it's required on every non-JVM target (see the KMP doc findings below); a route missing from this
  list is a silent failure that only breaks back-stack restore after process death.
- `RouteConfig.kt`/`RouteConfigProvider` is a `when (route)` returning per-route chrome settings
  (drawer-gestures-enabled, FAB) — the same shape this repo would need for width-gated chrome to
  keep working per-route (see coexistence, below).
- No nested navigation graphs anywhere — every `*EntryProvider.kt` is a flat
  `EntryProviderScope<NavKey>` extension function, same shape as this repo's `NavGraphBuilder`
  extensions (`epicNavGraph`, `issueNavGraph`, etc., see gap analysis below).

### What the `android-skills:navigation-3` skill says

Read directly from `~/.claude/plugins/marketplaces/android-skills/navigation/navigation-3/` (the
`Skill` tool didn't resolve `android-skills:navigation-3` from a fork's context — worth a
`docs/frictions.md` line if a future session hits the same thing; read the `SKILL.md` +
`migration-guide.md` + recipe files directly as a fallback).

**Migration guide's own stated assumptions/scope** (`migration-guide.md`) — checked against this
repo:
- Assumes one atomic migration, not incremental Nav2/Nav3 coexistence. ✅ matches how this repo
  would need to do it (no partial state).
- Assumes one or several top-level routes, each with its own back stack, state retained across
  switches — this is exactly wallosmobile's dual-stack shape and is also what this repo's
  drawer-based navigation already conceptually is (top-level sections: Dashboard, Kanban/Sprint,
  Team, Epics, Wiki, Settings, ...), it's just not modeled as a stack-of-stacks today — `MainNavHost`
  is one flat `NavHost`/back stack for everything.
- **Unsupported: "more than one level of nested navigation."** Checked — this repo has **none**.
  Every `nav/*NavGraph.kt` file (`EpicNavGraph.kt`, `IssueNavGraph.kt`, `TaskNavGraph.kt`,
  `UserStoryNavGraph.kt`, `ScrumNavGraph.kt`, `SettingsNavGraph.kt`, `WikiNavGraph.kt`,
  `WorkItemEditsNavGraph.kt`) is a flat `NavGraphBuilder` extension adding `composable<T>` calls
  directly — no `navigation<T> { }` nesting anywhere. This maps 1:1 onto the guide's own recommended
  refactor (`NavGraphBuilder` extension → `EntryProviderScope<NavKey>` extension, same file
  structure, same per-feature split this repo already has).
- **Unsupported: deep links, custom destination types.** Grepped — this repo has neither (no
  `navDeepLink`, no custom `NavType`). Not a blocker.
- Dialogs: guide has a `dialog<T>(metadata = DialogSceneStrategy.dialog())` recipe. This repo has
  **no** `dialog<T>` navigation destinations (grepped — the "BottomSheet" hits found are all
  `ModalBottomSheet`/`rememberModalBottomSheetState`, in-place Compose state, not nav destinations).
  Not a blocker, nothing to migrate here.

**`ListDetailSceneStrategy`** (`recipes/material-listdetail.md`) — this is the actual target for
option 3: `rememberListDetailSceneStrategy<NavKey>()` passed to `NavDisplay`'s `sceneStrategies`;
each destination tagged via `entry<T>(metadata = ListDetailSceneStrategy.listPane(...) /
.detailPane() / .extraPane())`. The strategy watches the back stack and decides 1/2/3-pane layout
from `calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())` — same `V2` width-breakpoint
API step 4 already uses for `NavigationSuiteScaffold`, not the deprecated one. **Navigation to open
the detail pane is unchanged** — pushing `ConversationDetail` onto the back stack as normal; the
scene strategy decides whether that renders as a full-screen replace (compact) or a second pane
(expanded) without the screen code knowing which.

The recipe's own artifact is `androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy`
— **Android-only in the doc example**. Confirmed separately (WebFetch,
`kotlinlang.org/docs/multiplatform/compose-navigation-3.html`, the official JetBrains KMP doc) that
a KMP-published equivalent exists: `org.jetbrains.compose.material3.adaptive:adaptive-navigation3`,
pinned in that doc at `1.3.0-beta02` (matching this repo's already-pinned
`jetbrainsComposeMaterial3Adaptive`), and per that same doc, "starting with Compose Multiplatform
1.10, Navigation 3 is supported for all supported platforms" (Android/iOS/Desktop/Web). **Not yet
verified**: whether `adaptive-navigation3`'s KMP build actually exports `ListDetailSceneStrategy`
with the same API as the Android-only recipe — the JetBrains KMP doc doesn't show a
`ListDetailSceneStrategy` example at all, only says the artifact exists. Step 4's dependency-graph
lesson applies directly here: check the `.module` metadata / decompile the jar before assuming API
parity, the way step 4 had to when `NavigationSuiteScaffoldDefaults` turned out to live in a
separate artifact than expected.

**KMP-specific requirement, confirmed by the same JetBrains doc, independent of wallosmobile**:
Android Nav3 uses reflection-based polymorphic serialization for `NavKey`, which doesn't exist on
iOS/other non-JVM targets. The multiplatform build requires an explicit `SerializersModule` passed
via `SavedStateConfiguration` to `rememberNavBackStack` — exactly the pattern wallosmobile's
`NavKeySerializers.kt` already implements. This repo would need the equivalent: one file listing
all 39 route classes as `polymorphic(NavKey::class) { subclass(...) }` — this can only be written
once every route implements `NavKey`, since `subclass<T : Base>` requires that compile-time bound;
see CHECKLIST-DONE.md's step 6 Note for why that pushed the file itself into step 7.

### Gap analysis: sizing the migration in this repo

**Current shape** (`MainNavHost.kt`, `composeApp/.../nav/*.kt`):
- One flat `NavHost` (`androidx.navigation.compose`), `composable<T>` destinations, no nesting.
- 8 `*NavGraph.kt` `NavGraphBuilder` extensions (`epicNavGraph`, `issueNavGraph`, `taskNavGraph`,
  `userStoryNavGraph`, `scrumNavGraph`, `settingsNavGraph`, `wikiNavGraph`,
  `workItemEditsNavGraph`) totaling 30 `composable<T>` destinations, plus 8 more defined directly
  in `MainNavHost.kt` (`LoginNavDestination`, `ProjectSelectorNavDestination`,
  `DashboardNavDestination`, `TeamNavDestination`, `KanbanNavDestination`, `SprintNavDestination`,
  `ProfileNavDestination`, `CreateTaskNavDestination`) — **38 distinct route classes reachable via
  `composable<T>`, 39 total** counting `WikiNavDestination` (a `DrawerDestination` marker never
  itself registered as a `composable<T>`) — full list in CHECKLIST-DONE.md's step 6 Note.
- **15 ViewModels** use `savedStateHandle.toRoute<T>()` (per CLAUDE.md's Navigation Pattern
  section) — every one of these needs to move to a constructor-parameter pattern
  (`@InjectedParam` + `parametersOf`, wallosmobile's approach) since Nav3 entries hand the route
  object to the screen directly, not through a `SavedStateHandle`.
- **`UPDATE_DATA_ON_BACK` result-passing** (`MainNavHost.kt:261`, used by
  `KanbanNavDestination`/`SprintNavDestination`/`EpicDetailsNavDestination` and others via
  `navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK]` / `setUpdateDataOnBack()`) is Nav2's
  `previousBackStackEntry.savedStateHandle` result-passing idiom. Nav3 has no back-stack-entry
  `SavedStateHandle` at all; the guide's own replacement is the event-bus recipe
  (`ResultEventBus`/`rememberResultEventBusNavEntryDecorator`/`ResultEffect`,
  `recipes/results-event.md`) or a `CompositionLocal`-based state variant
  (`recipes/results-state.md`). This is a real, non-mechanical porting task — 4+ call sites use the
  current mechanism.
- `CreateTaskNavDestination`'s `typeMap = typeMapOf(listOf(typeOf<CommonTaskType>()))`
  (`MainNavHost.kt:223`) exists only because Nav2's type-safe routes need a `NavType` for
  non-primitive serializable fields stored in a `Bundle`. Nav3 entries are plain in-memory objects
  handed straight to the screen — this typeMap machinery has no Nav3 equivalent and would simply be
  deleted, a net simplification, not a porting cost.
- No deep links, no nested `navigation<T>` graphs, no `dialog<T>` destinations — the three biggest
  items on the migration guide's own "unsupported" list are all non-issues here.

**Coexistence with steps 2–4's `NavigationSuiteScaffold`/width-gated chrome — confirmed
non-conflicting, but wiring changes:**
- Nav3 replaces the back-stack/`NavController` plumbing, not the width-based chrome decision in
  `MainScreen.kt` (`currentWindowAdaptiveInfoV2()` → compact vs. medium/expanded) — that check is
  orthogonal to which navigation library owns the back stack, so step 4's rail/drawer split
  survives unchanged in shape.
- What *does* change: `TaigaDrawerWidget.kt`'s `selected = currentTopLevelDestination ==
  destination.destination` (`TaigaDrawerWidget.kt:81,106,163`) and `MainScreen.kt`'s
  `appState.currentTopLevelDestination` (`MainScreen.kt:193,228`) currently derive from
  `NavController`'s back stack; under Nav3 these become
  `navigationState.currentTopLevelKey`-driven instead (wallosmobile's
  `MainAppState.currentDrawerDestination`, `MainAppState.kt:64`, is the direct precedent for this
  rewrite) — same shell-level file, different data source, no chrome-visible behavior change if
  done correctly. This repo doesn't currently have a `RouteConfig`-style per-route drawer-gestures
  setting; if that's wanted post-migration, wallosmobile's `RouteConfig.kt`/`RouteConfigProvider` is
  the pattern, but nothing today requires it — the modal drawer's own gesture handling is unaffected
  by this migration either way.
- `NavigationBackHandler` (compact-only, noted in step 4's CHECKLIST-DONE entry) intercepts the
  drawer's open/close state, not back-stack navigation — orthogonal to Nav3's own `onBack` wiring on
  `NavDisplay`, no conflict expected, not verified against real code.

### Recommended path forward

**Migrate all at once, not incrementally** — this matches the migration guide's own stated
assumption (no supported Nav2/Nav3 coexistence path), and this repo's scope (39 route classes, 15
ViewModels, one non-mechanical result-passing rewrite) is smaller than wallosmobile's own from-
scratch build, not larger — there's no phase boundary inside "swap NavHost for NavDisplay" that
would leave the app in a working state halfway through.

**Proposed breakdown for a future CHECKLIST.md decomposition** (proposed only, not added — a future
decomposition commit per this repo's process):

1. Add Nav3 dependencies + the KMP `NavKeySerializers`-equivalent (all ~31 routes registered) —
   compiles alongside existing Nav2 code, no behavior change yet. Small, mechanical, low-risk —
   same shape as step 2.
2. Convert `NavDestination` route classes to implement `NavKey` (mechanical, one-line-per-file) and
   build the `Navigator`/`NavigationState` shell (`core:navigation`-equivalent module or a
   `composeApp`-local file, wallosmobile's exact pattern) — no wiring into the running app yet.
3. Port ViewModels off `savedStateHandle.toRoute<T>()` to constructor-parameter injection —
   15 ViewModels, mechanical but not risk-free (Koin `@InjectedParam` + `parametersOf`
   wiring per ViewModel); could split further if this proves too large for one context.
4. Port the `UPDATE_DATA_ON_BACK` result-passing call sites to the event-bus recipe — the one
   genuinely novel piece, worth its own step and its own emulator verification (kanban/sprint
   "did the board change" refresh behavior is user-visible).
5. Replace `NavHost`/`composable<T>`/`NavGraphBuilder` extensions with
   `NavDisplay`/`entry<T>`/`EntryProviderScope<NavKey>` extensions across all 8 nav-graph files +
   `MainNavHost.kt`, delete Nav2 dependencies. This is the "big bang" cutover step — everything
   from steps 1–4 lands together here, since the guide's migration model doesn't support partial
   coexistence. Needs full emulator verification across every top-level section, not just a sample.
6. Rewire `TaigaDrawerWidget.kt`/`MainScreen.kt`'s `currentTopLevelDestination` to
   `navigationState.currentTopLevelKey`, confirm rail/drawer selection highlighting still works
   (step 4's emulator scenarios, re-run).
7. *(Separate, later phase — not part of the migration itself)* Add `ListDetailSceneStrategy` for
   the actual list-detail two-pane layouts option 3 was chasing — this is deliberately last, since
   everything above is pure infrastructure migration with no user-visible change, and this step is
   where option 3's real payoff (and its own design decisions — which screens get list-detail, what
   the placeholder pane shows) starts.

### Open questions for gregory (gate any future decomposition on these)

- **Confirm the KMP `ListDetailSceneStrategy` API parity** before committing to step 7 above — this
  investigation found the artifact exists for KMP but did not confirm the class itself is exported
  with the same shape as the Android-only recipe. Worth a short spike (add the dependency, try to
  reference the type on all three targets) before scoping step 7 in detail.
- **Which screens actually get list-detail treatment?** Not decided — Kanban/Sprint board +
  task detail is the obvious candidate (named in the original scope-options survey), but confirming
  the actual pane split (list = board, detail = task? or something else) is a design decision, not
  an engineering one, and belongs in a future step similar to this initiative's step 3.
- **Scope of the migration itself**: is gregory's intent step 1–6 above as one contiguous initiative
  before any list-detail work starts, or does gregory want it broken up with review points between
  each (given the guide's "atomic migration" assumption limits how much can ship independently)?

## Step 7 notes (2026-08-15) — where the Nav3 shell pieces actually live in this repo's module graph

wallosmobile's flat-ish module layout doesn't map 1:1 onto this repo's ~15 separate `feature/*/ui`
modules; two placement decisions from step 7 matter for steps 8–11 too:

**`core:navigation` (pre-existing, previously Nav2-only) is now the home for `NavKey` visibility
and the `Navigator`/`NavigationState` shell.** It already sat in the dependency graph between
`core:domain` and every feature `*/ui` module (added there originally for Nav2's
`NavigationExtensions.kt`), so declaring `libs.jetbrains.navigation3.ui` as `api` on it — rather
than adding the dependency to all 15 feature modules individually, as the original step 7 text
assumed — makes `NavKey` visible everywhere a route class lives, for free. `Navigator.kt`/
`NavigationState.kt` also live here since they're fully generic over `NavKey` (no concrete route
imports) — wallosmobile's versions ported with only a package rename.

**`NavKeySerializers.kt` cannot live in `core:navigation`** — it must reference all 39 concrete
route classes, and those classes live in modules that depend on `core:navigation`, so the reverse
edge would be a real cycle (not the apparent `:testing` one CLAUDE.md's Testing section describes,
where separate test/main compilations make it a non-issue). It lives in `composeApp` instead,
which already imports every route class for `MainNavHost.kt`. **Any future file needing to
reference every route class app-wide belongs in `composeApp`, not `core:navigation`** — this
applies directly to step 10's `NavDisplay`/`entry<T>` cutover and step 12's `ListDetailSceneStrategy`
wiring, both of which also need the full route set.

## Step 8 notes (2026-08-15) — one `@InjectedParam route: T`, not wallosmobile's per-field params

**Deviation from wallosmobile's own pattern, chosen deliberately.** wallosmobile's ViewModels take
each route field as its own `@InjectedParam` (`SubscriptionDetailViewModel(@InjectedParam private
val subscriptionId: Int, ...)`, `koinViewModel { parametersOf(subscriptionId) }` at the call site) —
confirmed by reading `SubscriptionDetailViewModel.kt`/`SubscriptionDetailScreen.kt` and four other
editor ViewModels there, all following the same per-field shape, some with `parametersOf(id, name,
email)`-style multi-arg calls. This repo instead injects the **whole route object** as a single
`@InjectedParam route: XNavDestination`, `koinViewModel { parametersOf(route) }` — matching the
checklist step 8 text's own `parametersOf(route)` wording. Simpler at this repo's scale (some routes
carry 2–5 fields, e.g. `CreateTaskNavDestination`) since it's one param per ViewModel regardless of
route field count, and every route class is already a plain data class — no serialization concern
for an in-memory constructor call. `route.field` reads inside the ViewModel body are otherwise
identical to the old `savedStateHandle.toRoute<T>().field` pattern, so callers changed less than the
per-field approach would have required.

**The three-file shape per ViewModel:** the ViewModel takes `@InjectedParam private val route: T`
(replacing `savedStateHandle: SavedStateHandle` + `private val route = savedStateHandle.toRoute<T>()`
inside the body); the Screen composable gains a `route: T` parameter and changes `koinViewModel()`
to `koinViewModel { parametersOf(route) }`; the NavGraph's `composable<T> { }` lambda gains a
`backStackEntry ->` parameter and passes `route = backStackEntry.toRoute()` into the Screen call.
15 ViewModels, but the NavGraph edits concentrate into 8 files (`MainNavHost.kt` plus 7
`composeApp/.../nav/*.kt` graphs) since several routes share a graph file (`WorkItemEditsNavGraph.kt`
alone covers 5).

**`NavBackStackEntry.toRoute<T>()` takes no `typeMap` parameter — do not carry over the
`SavedStateHandle.toRoute<T>(typeMap = ...)` call shape.** Confirmed by reading
`navigation-common-desktop-2.9.2-sources.jar`'s `NavBackStackEntry.kt`: the entry-level `toRoute`
reads `destination.arguments.mapValues { it.value.type }` — the typeMap the enclosing
`composable<T>(typeMap = ...)` call already registered on the destination — so a bare
`backStackEntry.toRoute()` is correct even for routes with non-primitive fields
(`CreateTaskNavDestination`'s `CommonTaskType`, the 5 `WorkItemEdit*NavDestination`'s
`TaskIdentifier`). Passing `typeMap` explicitly at the call site doesn't compile — no such overload
exists on `NavBackStackEntry`, only on `SavedStateHandle`. This cost a first-pass compile failure
across 6 call sites before the sources-jar read caught it.

**Existing tests needed the same mechanical update, and it simplified them.** All 15
`*ViewModelTest.kt` files (plus 2 `*ScreenTest.kt` Compose-UI-test pilots and `KoinGraphTest.kt`)
built a `SavedStateHandle(mapOf(...))` by hand, several JSON-encoding a `TaskIdentifier`/`CommonTaskType`
field (`Json.encodeToString(...)`) to match the Bundle-string shape `SavedStateHandle.toRoute<T>()`
expects. Constructing the route data class directly and passing it as `route = ...` needs no
encoding step — the object goes in as-is. `KoinGraphTest.kt`'s `single { SavedStateHandle() }`
registration (added so the 15 ViewModels' old `savedStateHandle: SavedStateHandle` constructor
param would resolve) is now dead code, since `@InjectedParam` isn't a graph-resolved dependency —
removed, along with the stale doc comment describing the old failure mode. The test now reports
`DefinitionParameterException` for those 15 (no `parametersOf(route)` supplied to a whole-graph
resolution pass) instead of the old `toRoute()` "no SavedStateHandle entry" failure — same
tolerated-non-`NoDefinitionFoundException` category, confirmed by running the test unchanged before
touching it.

## Step 9 notes (2026-08-15) — the real `ResultEventBus` doesn't exist at this project's pinned Nav3 version

**The gap analysis above (line 302-304) named the real recipe; it turned out to be unreachable.**
Pulled the actual jars: `androidx.navigation3:navigation3-runtime`'s `result` package (
`ResultEventBus`/`LocalResultEventBus`/`ResultEffect`/`rememberResultEventBusNavEntryDecorator`)
first appears at `1.2.0-alpha04` — `1.1.1` through `1.1.6` (the whole stable line) have nothing
under that package at all. This project pins `jetbrainsNav3 = "1.1.1"` (step 6). The JetBrains
KMP-published `navigation3-ui` — the artifact this project actually depends on for iOS/desktop,
since plain `androidx.navigation3:navigation3-ui` is Android-only (step 6's note) — only reaches
`1.2.0-alpha02` on Maven Central, and its own POM pulls `navigation3-runtime:1.2.0-alpha04`. So
reaching the real recipe means adopting an alpha release across the whole Nav3 stack, not a minor
bump. Both documented recipes (`recipes/results-event.md` and `recipes/results-state.md`) depend on
the same missing classes — there's no stable-version variant of either to fall back to.

**Asked gregory rather than picking an approach silently** (three options: hand-roll a stand-in,
adopt the alpha dependency, or defer the actual port to step 10). Chose hand-roll — stays on the
stable dependency line, and this project already has precedent for hand-porting Nav3 infrastructure
verbatim rather than depending on it prematurely (step 7's `Navigator`/`NavigationState`, ported
from wallosmobile rather than waiting on an upstream API).

**`ResultBus` (`core/navigation/.../ResultBus.kt`) mirrors the upstream `ResultEventBus`'s shape
exactly** — method names, buffered-`Channel`-per-key semantics — read directly from the real
implementation (`navigation3-runtime-1.2.0-alpha07-sources.jar`, `ResultEventBus.kt`/
`ResultEffect.kt`) so the port is a faithful copy of the mechanism, not a reinvention. Left out the
`conflateAsState` state-variant half of the upstream API (`recipes/results-state.md`'s approach) —
only the event-based half (`recipes/results-event.md`) is needed for `UPDATE_DATA_ON_BACK`'s
one-shot "refresh happened" signal, and CLAUDE.md's Simplicity First guidance argues against porting
the unused half speculatively. `LocalResultBus` is a plain top-level
`val = staticCompositionLocalOf<ResultBus> { error(...) }`, matching this repo's own
`LocalScreenReadySignal`/`LocalOfflineState` convention (`uikit`) rather than androidx's own
nested-object wrapper (`object LocalResultEventBus { ... }`) — ktlint's `compose:compositionlocal-*`
rules flagged the nested-object shape (naming + allowlist) before this was caught; the flat-`val`
form needed only an `.editorconfig` allowlist addition (`compose_allowed_composition_locals`,
gated by `guardrails.yml`, `Gate-change:` line in the commit).

**Wired in ahead of the NavDisplay cutover, on purpose.** `CompositionLocalProvider` is a plain
Compose primitive, not something `NavHost` or `NavDisplay` owns — so `MainNavHost.kt` can provide
`LocalResultBus` around its still-Nav2 `NavHost { }` call today, and every `composable<T>` under it
already sees `LocalResultBus.current`. This means step 9's call-site porting (all 4+ `goBack`
senders, all 9 list/board-screen consumers) is complete now, not deferred to step 10 — step 10 only
has to carry the same `CompositionLocalProvider` wrap over to `NavDisplay`, not build the result
mechanism from scratch.

**One shared marker (`UpdateDataOnBack`, a `data object`) replaces one shared string constant
(`UPDATE_DATA_ON_BACK`) — deliberately not per-destination-typed signals.** The old convention was
already a single global key used by every detail screen and every list screen; `ResultBus`'s channel
is keyed globally by type too, so a single marker is a faithful port, not a simplification that
changes behavior. This is safe under Nav2's current `NavHost` specifically because only the
currently-visible destination's `composable<T>` body is ever actively composed — any `ResultEffect`
listening for `UpdateDataOnBack` that isn't the one screen currently on top simply isn't running, so
there's no cross-screen collision despite the shared key. **This assumption needs re-checking at
step 10** if `NavDisplay`'s scene strategy ever keeps more than one entry actively composed at once
(list-detail two-pane, step 12's eventual payoff, is exactly that shape) — worth a note in step 12's
own scoping when it starts.
