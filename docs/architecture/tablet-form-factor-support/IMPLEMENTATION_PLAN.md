# Tablet and Other Form Factor Support — Plan

Reference doc for this initiative: architecture, rationale, tradeoffs. See
[CHECKLIST.md](CHECKLIST.md) for the executable steps.

## Current state (evidence, gathered 2026-08-15)

The app has **no form-factor adaptation at all** — it is built single-pane, phone-shaped, on
every platform:

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
   (`material3-adaptive-navigation-suite`) whose current KMP/multiplatform target coverage needs
   verifying before committing to it — not confirmed yet.

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

**Not yet decided which of options 2–4 (if any) to pursue.** Option 1 is a safe, useful common
prefix regardless of which later option gets picked, so it's the only one decomposed into a
checklist step so far — see [CHECKLIST.md](CHECKLIST.md). Steps for 2/3/4 are gated on gregory
choosing a target scope; don't decompose or start them without asking first.
