# 2026-11-23 — Tablet nav rail overflow + list-detail two-pane: what's the right setup today

**Status:** Awaiting decision
**Link:** none. Covers `docs/revisit.md` entry 45 and tablet checklist step 12/12c
(`docs/architecture/tablet-form-factor-support/CHECKLIST.md`)
**Updated:** 2026-11-23

## Report

gregory (2026-11-23), picking up revisit entry 45: "I remember we had an issue with tablets using
navrail and using the correct android way of showing list-details at the same time, so let's
investigate that."

That covers three earlier findings, all still open:

1. **Revisit 45**: the medium/expanded-width rail has no scrolling. Trailing items get clipped
   when the window is short (`docs/issues/2026-08-26-tablet-nav-rail-logout-clipped.md`).
2. **Step 12c crash**: the two-pane Issues list-detail layout (`ListDetailSceneStrategy`) crashed
   with a Compose runtime error ("Missed recording an endGroup") when `UserStoryDetails` was pushed
   from the detail pane (`docs/issues/2026-08-21-listdetail-scaffold-crash-on-detail-column-push.md`).
3. **Step 12c app-bar bug**: in two-pane mode, after a back navigation, the top bar showed the list
   pane's "+" instead of the detail pane's "⋮". Never root-caused.

Items 2 and 3 got the two-pane wiring reverted on 2026-08-22. Issues has been single-pane since.

## Findings

### Library state has moved since August

- **Then:** step 12b pinned `adaptive-navigation3` `1.3.0-beta02`, which was the newest build at
  the time (crash doc, Option 3).
- **Now:** Maven Central lists `org.jetbrains.compose.material3.adaptive:adaptive-navigation3`
  **`1.3.0` (stable)**, `1.3.0-rc01` and `1.4.0-alpha01` (`maven-metadata.xml`, checked
  2026-11-23). This repo is also on Compose Multiplatform `1.12.2`, `adaptive` `1.3.0`, Nav3 `1.2.0`
  and lifecycle `2.11.0` (`gradle/libs.versions.toml:83-91`). The crash happened inside the Compose
  runtime itself (zero app frames). **Nobody has tried to reproduce it on these versions.**
- `material3` / `material3-adaptive-navigation-suite` are pinned at `1.10.0-alpha05`
  (`libs.versions.toml:85`). The newest JetBrains stable is `1.9.0`; `1.13.0-rc01` is the newest
  overall. This doesn't block anything here, but bumping it is its own decision.

### The nav rail (revisit 45)

- The rail now lives in **grappim-kit**, not this repo: `NavigationSuiteWidget`
  (`grappim-kit/uikit/.../widgets/drawer/NavigationSuiteWidget.kt`), called from
  `composeApp/.../main/MainScreen.kt:226-233`. Entry 45's `file:line` (`TaigaDrawerWidget.kt`) is
  stale. **Any fix ships through a kit release**, and it also changes wallosmobile's rail.
- The kit widget uses the older `NavigationSuiteScaffold(navigationSuiteItems: NavigationSuiteScope.() -> Unit, …)`
  overload. The library's own KDoc says: *"It is recommended to use the NavigationSuiteScaffold
  function with the navigationItems param that accepts NavigationSuiteItems instead of this one"*
  (material3-adaptive-navigation-suite `1.10.0-alpha05` sources, `NavigationSuiteScaffold.kt:~280`).
- **None of Material3's vertical navigation components scroll.**
  - `WideNavigationRailLayout` (`material3/WideNavigationRail.kt:214-420`) measures every item at
    the rail's full height and places them with an `Arrangement`. There is no scroll modifier, so
    overflow is clipped.
  - `NavigationRail` and `PermanentDrawerSheet` have no scroll either (grepped
    `NavigationRail.kt`/`NavigationDrawer.kt` for `verticalScroll`/`scroll`: no hits).
  - So the August doc's Option 1 assumption holds: the library has no scroll option to switch on.
- **The newer overload makes the fix much smaller than the August doc assumed.** August's Option 1
  said a scrollable rail meant *"giving up NavigationSuiteScaffold's automatic switching"*. That's
  no longer true:
  - `NavigationSuiteScaffold(navigationItems: @Composable () -> Unit, navigationSuiteType, …)`
    (`NavigationSuiteScaffold.kt:196`) takes the items as plain composable content. The kit can
    wrap them in its own `Column(Modifier.verticalScroll(...))`.
  - `NavigationSuiteScaffoldLayout(navigationSuite: @Composable () -> Unit, navigationSuiteType, state, …)`
    (`:441`, not deprecated) goes further: it keeps the scaffold's layout, insets and show/hide
    animation, and lets us supply the whole rail.
  - **Inference, unverified:** wrapping the items in one scroll column turns them into a single
    measurable inside `WideNavigationRailLayout`. The rail's per-item spacing
    (`itemVerticalSpacedBy`) and minimum height would then stop applying, so the column has to add
    them back. A spike has to confirm this renders correctly.
- **`calculateFromAdaptiveInfo` never returns a permanent drawer.** Its whole body
  (`NavigationSuiteScaffold.kt:1250-1262`) returns `NavigationBar` if the posture is tabletop, the
  height is compact or the width is compact. Otherwise it returns `NavigationRail`. So entry 45's
  "rail/permanent drawer" is really "rail", which matches the AVD observation in
  `docs/EMULATOR_TESTING.md:75`.
- **A related gap nobody has looked at (inference from code, not reproduced):**
  - `MainScreen.kt:186-188` picks the rail path whenever the *width* is at least medium (600dp).
  - But `calculateFromAdaptiveInfo` returns **`NavigationBar`** when the *height* is compact
    (under 480dp).
  - A phone in landscape (around 900×410dp) matches both. It would get a **bottom navigation bar
    with up to 12 items**.
  - The newer `navigationSuiteType()` (`:1223`) has the same split: it returns
    `ShortNavigationBarMedium` there.
  - Material's bottom bars are designed for 3–5 destinations.
- **Worst-case item count today is 12:** ProjectSelector, Dashboard, Epics, Issues, Kanban, Team,
  WikiLinks, WikiPages, ScrumBacklog, ScrumOpenSprints, ScrumClosedSprints, Settings
  (`DrawerItemsBuilder.kt:53-176`). In August, 13 items clipped at 750dp of window height on
  desktop. `Medium_Tablet` in landscape is 1280×800dp before the system bars take their share, so
  12 items is likely right at the edge on the real AVD. This is inference; it hasn't been measured.

### The app-bar bug (step 12c, item 3): root cause from code

- `TopBarController` (`grappim-kit/uikit/.../topbar/TopBarController.kt:15-26`) holds **one**
  `config`. Whoever called `update()` last wins.
- Every screen calls it from a `LaunchedEffect`:
  - the list at `IssuesScreen.kt:67-68` (sets "+")
  - the detail at `IssueDetailsScreen.kt:114` (sets "⋮")
- In single-pane mode only one entry is composed at a time, so this works.
- In two-pane mode both are composed:
  - Going back from the pushed `UserStoryDetails` puts both panes back into the composition.
  - Both `LaunchedEffect`s fire again, and whichever runs second owns the bar.
  - August saw the list win.
  - **Inference:** that order comes from the order effects run in, not from anything the app
    controls.
- **This is structural, not Issues-specific.** gregory's August suspicion holds: every screen
  that uses `TopBarController` has the same race once it shares a scene with another one.

### The crash (step 12c, item 2)

Nothing new since August's four ruled-out hypotheses. The one new fact is the version change
above. The crash came from Compose runtime internals with no app frames, so a newer runtime might
well have fixed it — **it needs a retest, not more theorizing.**

### What Android and Material guidance says (checked 2026-11-23)

gregory asked for the tablet work to follow Android/Material practice, so each option below is
checked against these sources. m3.material.io renders nothing without JavaScript, and its
list-detail canonical-layout page returned 404. So the Material quotes come from
material-components-android's docs on GitHub, which hold the same component guidance.

**Navigation components**
- **Rail, collapsed:** *"displays three to no more than seven app destinations when collapsed"*
  ([NavigationRail.md](https://github.com/material-components/material-components-android/blob/master/docs/components/NavigationRail.md)).
- **Rail, expanded:** *"There is no limit of items shown when expanded"* and *"When expanded, all
  menu items will be shown, including submenu items"* (same doc).
  - Its scrolling attribute (`app:scrollingEnabled`) defaults to `false`.
- **Bar:** *"Navigation bars can have three to five destinations"*
  ([BottomNavigation.md](https://github.com/material-components/material-components-android/blob/master/docs/components/BottomNavigation.md)).
- **Drawer:** for large screens it points to the expanded rail instead: *"use an expanded
  navigation rail ... which has mostly the same functionality of the navigation drawer"*
  ([NavigationDrawer.md](https://github.com/material-components/material-components-android/blob/master/docs/components/NavigationDrawer.md)).
- **Android's default component choice:** *"Navigation bar if the width or height is compact ...
  Navigation rail for everything else"*, and the type may be customized via `layoutType`
  ([build-adaptive-navigation](https://developer.android.com/develop/ui/compose/layouts/adaptive/build-adaptive-navigation)).
- **Size classes, not device type:** *"Window size classes are not intended for isTablet‑type
  logic"*. Compact height is a case where *"two pane layouts are not practical"*
  ([use-window-size-classes](https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes)).
  `MainScreen.kt` already decides by size class, not by device.

**List-detail**
- **Nav3 path:** *"the adaptive-navigation3 artifact provides a ListDetailSceneStrategy"*, with
  `listPane()`/`detailPane()`/`extraPane()` set as entry metadata
  ([navigation-3/custom-layouts](https://developer.android.com/guide/navigation/navigation-3/custom-layouts)).
  This is exactly what step 12b wired up, so the approach was already the official one.
- **Opening something related from a detail pane:** the official sample opens it in the
  **extra pane** (`backStack.add(Profile)` with `Profile` marked `extraPane()`, same page). In step
  12b, `UserStoryDetails` had no pane metadata at all.
- **When the window resizes:**
  - Narrowing from two-pane: *"the detail pane remains visible and the list pane is hidden"*.
  - Widening from detail-only: *"the list indicates that the item corresponding to the content in
    the detail pane is selected"*.
  - Widening from list-only: list plus a placeholder detail pane.
  - Source: [canonical-layouts](https://developer.android.com/develop/ui/compose/layouts/adaptive/canonical-layouts).
- **Back:** the recommended default is `PopUntilScaffoldValueChange`
  ([list-detail](https://developer.android.com/develop/ui/compose/layouts/adaptive/list-detail)).
- **Top app bars in two-pane mode:** **not stated** by any page that loaded. The closest is Now in
  Android's `NiaApp.kt`. It has one shell top bar, shown *"only ... on top level destinations"*
  (`if (appState.navigationState.isAtTopLevel)`), and non-top-level screens render their own
  header. The interests list-detail screen itself 404'd, so whether NiA's detail pane has its own
  bar is unconfirmed.

**What this changes in the plan**
1. **Scrolling a 12-item collapsed rail is not the Material pattern.** The prescribed shape for
   more than 7 destinations is the **expanded rail**: up to 7 primary destinations when collapsed,
   and everything (including submenu items) when expanded. In Compose that's `WideNavigationRail`,
   expandable through its header, which is the component the library's own non-deprecated
   overload uses. Scrolling stays only as a fallback for when even the expanded rail runs out of
   height.
2. **A bar for compact-height windows doesn't fit this app.** Android's default would give phone
   landscape a bar, but Material caps bars at 5 and we have up to 12. Overriding the type is
   allowed (`layoutType` customization, same page), so compact height should stay on the modal
   drawer the phone already uses.
3. **Two-pane top bars have no official rule.** NiA's "shell bar only at top level, screens own
   their headers below that" leans toward B2-style pane-owned bars, but it's a reference app, not a
   spec.
4. **`UserStoryDetails` opened from an Issue detail should be an `extraPane()`**, following the
   official sample. A retest of the crash should include this setup too, since the August repro
   pushed it without pane metadata.

## Root cause

- **Rail overflow:** Material3's rails don't scroll, and the kit's `NavigationSuiteWidget` passes
  up to 12 items to one without its own scroll container
  (`grappim-kit/uikit/.../NavigationSuiteWidget.kt:30-55`).
- **Two-pane app-bar bug:** the shell has one top bar (`TopBarController`, last writer wins).
  That can't represent two panes that are visible at the same time.
- **Two-pane crash:** not known. Untested on current library versions.

## Impact

- **Rail:** a tablet or desktop window that's short enough, with a project that has Wiki and Scrum
  enabled, loses its last items (Settings first). Nothing can bring them back short of resizing
  the window or rotating the device.
- **Possible phone-landscape bottom bar:** if the inference holds, a 12-item bottom bar on every
  phone in landscape. This would be more widespread than the rail clipping.
- **List-detail:** a missing feature, not a regression. Issues works fine single-pane on every
  form factor today.

## Open questions

1. **Does the crash still reproduce** on `adaptive-navigation3` `1.3.0` with CMP `1.12.2`?
   Blocks any two-pane plan. Answer: a throwaway spike that re-adds the three reverted pieces
   (dependency, `rememberListDetailSceneStrategy` on `NavDisplay`, `listPane()`/`detailPane()` on
   the two Issues entries), then runs the Promote scenario on `Medium_Tablet`.
2. **What does a phone in landscape actually show today?** Answer: a desktop window around 900×420,
   or the phone AVD rotated. Decides whether the bar case belongs in the rail fix.
3. **Does a scrollable column inside `NavigationSuiteScaffold(navigationItems = …)` render
   correctly** (item spacing, selection indicator, collapsed rail width)? Answer: a spike in
   grappim-kit.
4. **In two-pane mode, who owns the top bar?** A product call for gregory; see the options below.

## Options

### A. Rail overflow (revisit 45)

**A1. Kit: move to the newer `navigationItems` overload and wrap the items in a scroll column.**
(Recommended.) Also pass the type ourselves rather than relying on defaults: rail at medium and
wider, never a bar.

- Pros:
  - Fixes overflow at the root while keeping `NavigationSuiteScaffold`'s layout and insets.
  - Moves off an overload the library tells us not to use.
  - Small: one composable in the kit.
- Cons:
  - Needs a kit release plus a version bump here, and wallosmobile picks it up too.
  - Scrolling a rail isn't the Material-blessed shape. Spacing and min-height may need adding back
    by hand (open question 3).
- Risk/blast radius: the rail in two apps. Visual regressions are possible; the logic is unchanged.

**A2. Kit: render the rail ourselves via `NavigationSuiteScaffoldLayout(navigationSuite = …)`.**

- Pros: full control (e.g. a `WideNavigationRail` with a scrolling body and a pinned Settings
  footer).
- Cons: more code to own, and we'd be re-implementing what `NavigationSuite` already does.
- Use it only if A1's spike shows the single-column approach fights the rail's layout.

**A3. Fewer items: collapse the Wiki/Scrum groups on wide layouts.**

- Pros: no scroll needed.
- Cons: only buys room for a few items, and changes navigation for every wide-screen user.
  August's Option 3 had the same weakness.

**A4. Do nothing.**

- Cons: Settings becomes unreachable on short windows. The same reason August rejected it.

### B. List-detail two-pane (step 12)

**B0. Spike first (open question 1).** Required before B1/B2. It costs about one session and
decides everything else.

**B1. If the crash is gone: re-enable two-pane for Issues, and make the top bar belong to one
navigation entry.**

- `TopBarController` stores configs keyed by `NavKey` (`update(key, config)`), and the shell shows
  `configs[navigationState.currentKey]`. In two-pane mode that's the detail entry, the top of the
  stack.
- Pros:
  - Deterministic.
  - Small kit change plus mechanical call-site edits.
  - Single-pane behavior is unchanged, because the current key is the only screen.
- Cons:
  - In two-pane mode the list's "+" isn't visible while a detail is open; the user has to close the
    detail first.
  - Touches every `topBarController.update` call site (repo-wide grep).

**B2. Each pane gets its own top bar in two-pane mode.** List and detail each render their own bar
inside their pane. The shell bar either hides or shows only the section title.

- Pros: both panes keep their actions, and it's the natural shape for a two-pane layout.
- Cons: much larger. Every list-detail screen needs a "render my own bar" mode, and the shell needs
  to know when it's in a multi-pane scene.

**B3. If the crash persists: minimal repro, file upstream, keep single-pane.** This was August's
Option 1. Steps 12b/12c stay gated.

**B4. Drop two-pane for now.** Close step 12 as won't-do until something upstream changes.

- Pros: zero cost.
- Cons: tablet users keep full-screen push, which is what they have today.

**A5. Kit: switch to Material's expanded-rail pattern.** (Recommended, revised after the guidance
check above; replaces A1 as the primary fix.)

- `NavigationSuiteScaffold(navigationItems = …)` with `WideNavigationRail` types.
- Collapsed: up to 7 primary destinations.
- A menu button in the rail header expands it to show every destination, Settings included.
- Compact height stays on the modal drawer, never a bar.
- A scroll container only as a fallback for very short windows.
- Pros:
  - Matches Material's documented limits (7 collapsed, unlimited expanded).
  - Uses the component the library now recommends.
  - Fixes the clipping and the possible phone-landscape bar together.
- Cons:
  - Needs a product decision on which ≤7 destinations stay visible when collapsed.
  - Bigger change to the kit widget than A1; `DrawerItem` needs a "primary" marker or equivalent.
  - Changes the rail look for wallosmobile too.
- Risk: `WideNavigationRail` is newer API (M3 Expressive), so expect some API churn.

### Recommendation

1. **A5 first.** Settle open question 2 (phone landscape) at the start, since it's part of the
   same change.
2. **Then B0**, the crash spike on current versions, with `UserStoryDetails` marked `extraPane()`
   as the official sample does.
3. **If B0 is clean:** two-pane Issues following the documented behavior: placeholder detail pane,
   selection kept across resize, `PopUntilScaffoldValueChange`-style back.
   - **Top-bar ownership (B1 vs B2) is gregory's call.** No official guidance covers it. B1 is
     smaller. B2 matches what NiA does for non-top-level screens.
4. **If B0 still crashes, do B3.**

## Decision

gregory, 2026-11-23:

- **Everything in one TaigaMobileNova PR:** the rail (A5) plus the list-detail work (B0, then
  B1/B2 or B3).
- **Rail follows Material's expanded-rail pattern (A5).**
  - **The kit change is done in grappim-kit by its own session.** The request was sent to the
    grappim-kit session on 2026-11-23. TaigaMobileNova's PR picks up the released version.
  - **Destinations shown when the rail is collapsed:** ProjectSelector, Dashboard, Epics, Issues,
    Kanban, ScrumBacklog, Settings.
  - **When expanded:** everything else too (Team, Wiki links/pages, Open/Closed sprints).
- **Two-pane top-bar ownership (B1 vs B2): deferred.** gregory will decide after the crash retest
  (B0), once there's a running two-pane screen to look at.

**Status:** Approved. Waiting on the grappim-kit release before the rail part starts here. B0 (the
crash retest) doesn't depend on the kit and can start first.
