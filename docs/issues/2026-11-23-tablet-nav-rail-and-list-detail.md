# 2026-11-23 — Tablet nav rail overflow + list-detail two-pane: what's the right setup today

**Status:** Done
**Link:** none. Covers `docs/revisit.md` entry 45 and tablet checklist step 12/12c
(`docs/architecture/tablet-form-factor-support/CHECKLIST.md`)
**Updated:** 2026-11-24

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

## B0 retest results (2026-11-23)

**Setup:**
- Re-added the three reverted pieces:
  - `adaptive-navigation3` `1.3.0` in the catalog and `composeApp`
  - `rememberListDetailSceneStrategy()` on `MainNavHost`'s `NavDisplay`
  - `listPane()`/`detailPane()` on the Issues entries
- CMP `1.12.2`.
- `Pixel_Tablet` AVD (API 36, 2560×1600 @ 320dpi, so 1280×800dp landscape). `Medium_Tablet`
  doesn't exist on this machine; `docs/machine.local.md` names `Pixel_Tablet` as its stand-in.
- Local Taiga.

**Pass 1, `UserStoryDetails` with no pane metadata (the August shape): no crash, 2 of 2.**
- Two-pane renders: list on the left, an empty detail area until an issue is selected, then the
  issue in the right pane.
- Tapping an already-promoted issue's user-story link (#3 → user story #18) opened the user story
  full-width.
- **Promote** on #16 (`POST /issues/2/promote_to_user_story`) created and opened user story #23.
- For both: same process id throughout, empty `logcat -b crash`.
- August crashed on both of these paths, 2 of 2.
- **Conclusion:** the August crash doesn't reproduce on current library versions. Most likely
  fixed upstream between `1.3.0-beta02` and `1.3.0` / the newer Compose runtime. It was never
  root-caused, so that's inference.

**Top-bar bug reproduces.** After going back from the user story to the two-pane view, issue #3
was still open in the right pane, but the shell bar showed the list's "Issues" title with "Menu"
and "Add". That's exactly August's symptom and the last-writer-wins `TopBarController` mechanism
in Findings.

**Pass 2, `UserStoryDetails` marked `extraPane()` (the official sample's shape): no crash.**
- From issue #3's user-story link, three panes rendered side by side: Issues list, Issue #3, and
  User story #18.
- Back closed the extra pane (list + issue), and back again closed the detail (list only). That
  matches Android's documented list-detail behavior.
- No crash entries for the whole session.
- One interruption: the extra pane first showed "Connection error". The tablet AVD had lost its
  route to the host again, which is the documented gotcha. A wifi toggle plus Retry loaded it.

**The top bar is now the only blocker, and three panes make it worse.** With list, issue and user
story all visible, the shared shell bar still showed "Issues" with the list's "+". Neither detail
screen's "⋮" menu (Promote, Delete, Copy link, …) could be reached in any state. So B1 vs B2 has to
be settled before two-pane can ship: as wired, it hides every detail screen's actions.
Screenshot of the three-pane state taken this session (scratchpad, not committed).

**Code state:** the branch keeps both passes' wiring:
- the `adaptive-navigation3` dependency
- the strategy on `NavDisplay`
- `listPane()`/`detailPane()` on Issues
- `extraPane()` on `UserStoryDetails`

## Two panes max, and the rail (2026-11-24)

**Two panes max (gregory: "fine with two panes"):**
- Material's `calculatePaneScaffoldDirective` (`adaptive-layout` `1.3.0`) gives one pane below
  840dp, two at 840–1200dp, and three at 1200dp and wider. That's why the 1280dp test tablet showed
  three.
- `UserStoryDetails` is now marked `detailPane()`, so it replaces the issue in the right pane.
- Checked on `Pixel_Tablet`:
  - Landscape: list + user story, and back returns to list + issue.
  - Portrait (800dp): one pane, keeping the open issue.

**Rail (step 16), kit `0.1.9`:**
- Secondary destinations marked `primary = false`: Team, Wiki links/pages, Open/Closed sprints.
- `MainScreen` passes `WideNavigationRailCollapsed` and uses the modal drawer when either the width
  or the height is compact.
- Checked on `Pixel_Tablet` landscape with every section enabled:
  - Collapsed shows exactly the 7 primary destinations.
  - The menu button expands it to all 12.
  - The expanded rail scrolls to Settings, which opens.
- `Pixel_10a` in landscape (923×411dp): the modal drawer, not a bar.
- `DrawerItemsBuilderTest` pins the primary/secondary split.
- **Not checked:** a short desktop window.

**Noticed, not changed:** on wide layouts the shell top bar still shows its own "Menu" (≡) icon next
to the rail's expand button. That's two hamburgers side by side. Whether the bar's icon belongs on
wide layouts at all is part of the open top-bar decision.

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

**2026-11-24, gregory:**
- **Two panes max.** Three side by side felt uncommon, and Material only allows three at 1200dp
  and wider.
- **Option B2:** each pane draws its own top bar. Chosen from the user's point of view: nothing
  disappears, and each button sits over what it acts on.
- **No second ≡ next to the rail.**

## What landed

- **Rail (A5):**
  - grappim-kit `0.1.9`'s expandable `WideNavigationRail`.
  - `DrawerItemsBuilder` marks Team, Wiki links/pages and Open/Closed sprints `primary = false`.
  - `MainScreen` passes `WideNavigationRailCollapsed` and keeps the modal drawer when the width or
    the height is compact.
- **List-detail:**
  - `ListDetailSceneStrategy` (`adaptive-navigation3` `1.3.0`) on `NavDisplay`.
  - Issues entries marked `listPane()`/`detailPane()`.
  - `UserStoryDetails` marked `detailPane()`, so it replaces the issue in the right pane.
- **Pane top bars (B2):** `PaneTopBarHost` (`composeApp/.../main/PaneTopBars.kt`) wraps every
  pane-capable entry.
  - Inside a list-detail scene (`LocalListDetailSceneScope` non-null) it gives the screen its own
    `TopBarController` and `TopBar`.
  - The shell hides its own bar while any pane registers one.
  - Screens are unchanged; their `topBarController.update` calls just reach the pane's controller.
  - Top bars drop the ≡ icon on wide layouts.
- **Tests:** a new `DrawerItemsBuilderTest` case pins the primary set.
  - Full `./gradlew jvmTest`: 1977 tests, 0 failures.
  - `ktlintCheck`, `:composeApp:detekt`, `koverXmlReport` and `:koverVerify` pass.
- **Emulator:**
  - `Pixel_Tablet`: landscape two-pane bars, user story → back keeps the issue's ⋮ (the August
    bug), portrait single pane, rotation both ways, Dashboard unchanged.
  - `Pixel_10a`: portrait and landscape unchanged.
- **Not covered:**
  - A short desktop window (no `xdotool`/Xvfb on this machine).
  - A window resized without recreating the activity (desktop, Android free-form) might leave a
    pane's bar empty until the screen updates it. Screens set their bar from `LaunchedEffect`s
    that aren't keyed on the controller, so a pane switch inside one composition won't re-run
    them. Rotation recreates the activity, so it's fine. Tracked as `docs/revisit.md` entry 66.

## Agent-qa trial 3 (2026-11-24): two bugs to fix before merge

**Setup:**
- Planner + executor hand-run on PR #500, per `agentic-grappim/agent-qa/DESIGN.md`. Run files are
  in `agentic-grappim/agent-qa/trials/3-taigamobilenova/`.
- 4 scenarios, all with "no app crash" as the deterministic check:
  - T1 rail: 4/4 passed.
  - T2 two-pane: 7/7 passed.
  - T3 portrait: 6/6 passed.
  - P1 phone: 4/5, step 4 failed.
- No app crashes. Total about $1.15.

**Bug 1 (major): on a phone in landscape, Issues is two-pane and the ≡ is gone, so the drawer can't
be opened from Issues.**
- Pixel_10a in landscape is about 923×411dp. `calculatePaneScaffoldDirective` decides panes from
  width alone: 840dp and up gives 2 (`adaptive-layout` 1.3.0, `PaneScaffoldDirective.kt`).
- `MainScreen` keeps the modal drawer for compact height, but the pane bars
  (`PaneTopBarHost` → `withoutMenuIcon()`) strip the ≡.
- Android guidance: compact height is where "two pane layouts are not practical".
- **Fix:** in `MainNavHost.kt`, pass `rememberListDetailSceneStrategy(directive = ...)` built from
  `calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())`, with `maxHorizontalPartitions = 1`
  when the window height is below `HEIGHT_DP_MEDIUM_LOWER_BOUND`. Then two-pane needs both width
  ≥840dp and a non-compact height, and phone landscape is single-pane with the shell bar and ≡.

**Bug 2 (minor): on a tablet the keyboard pops up whenever the two-pane view appears.**
- Repro: rotating into landscape with an issue open, or tapping "New issue"'s back arrow (3 of 3
  runs). A back swipe doesn't trigger it, and single-pane never does.
- Root cause: `ThreePaneScaffold.kt:219-222` calls `requestFocus()` on the current pane when
  `PaneScaffoldDirective.shouldAutoFocusCurrentDestination` is true, which is the default.
  In touch mode only text fields take focus, so it lands on the Comment field.
- `copy()` has no parameter for the flag. Use the `PaneScaffoldDirective(...)` constructor, which
  has `shouldAutoFocusCurrentDestination`.
- **Decided 2026-11-24 (gregory): (a)**, off for touch only. The options were:
  - (a) off for touch only: autofocus = `LocalInputModeManager.current.inputMode == InputMode.Keyboard`.
    Recommended, since keyboard/D-pad users land on a button, not the text field.
  - (b) off for everyone.
  - (c) leave as is.

**Not bugs:**
- Issue #16 "Connection error" in T2 was the emulator dropping its network (the app's offline
  banner was showing).
- "Block" in the ⋮ menu is a normal item the scenario didn't list.

**Fixed (2026-11-24, step 17):** `listDetailPaneDirective()` in `MainNavHost.kt` builds the
strategy's `PaneScaffoldDirective` from `calculatePaneScaffoldDirective`, with:
- `maxHorizontalPartitions = 1` at compact height (bug 1)
- `shouldAutoFocusCurrentDestination` true only for `InputMode.Keyboard` (bug 2, option (a))

Verified:
- Pixel_10a in landscape: Issues is full-width with ≡, the drawer opens, the issue opens
  full-screen, no keyboard.
- Pixel_Tablet: no keyboard in 5 of 5 tries (3 via "+" then the back arrow, 2 via rotation),
  where it was 3 of 3 and 2 of 2 before.
- Trial scenarios re-run on the fixed build: T2 7/7, T3 6/6, P1 5/5, no app crash, no focus
  findings.
- Full `jvmTest` (1977 tests), ktlint, detekt, kover pass.

Not verified on device: the keyboard-input half of (a). `adb input keyevent` left the emulator in
touch mode, so Compose never reported `InputMode.Keyboard`.
