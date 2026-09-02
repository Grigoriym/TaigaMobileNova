# 2026-08-26 — Tablet: no Log out button visible

**Status:** Done
**Link:** none (reported directly by gregory while reviewing PR #362 / branch
`docs/multi-session-process-and-tablet-support`)
**Updated:** 2026-08-26

## Report

gregory, testing the tablet layout on this branch, observed there is no Log out button
on tablet.

No repro steps, window size, or platform were given beyond "on tablet" — filled in
during investigation below.

## Findings

- The tablet/desktop nav chrome is `TaigaNavigationSuiteWidget`
  (`composeApp/src/commonMain/kotlin/com/grappim/taigamobile/TaigaDrawerWidget.kt:147-184`),
  used instead of the phone's `TaigaDrawerWidget` modal drawer whenever window width is
  medium/expanded (`MainScreen.kt:182-229`).
- `TaigaNavigationSuiteWidget` renders every item from `flattenForNavigationSuite(drawerItems)`
  via `NavigationSuiteScaffold`'s `navigationSuiteItems { destinations.forEach { item(...) } }`
  (`TaigaDrawerWidget.kt:155-181`) — **with no scroll container around that list.**
- The phone drawer (`TaigaDrawerWidget`, same file, lines 48-53) wraps its equivalent item
  `Column` in `Modifier.verticalScroll(rememberScrollState())` explicitly. The tablet rail/permanent-drawer
  path has no equivalent — it relies entirely on `NavigationSuiteScaffold`'s default rendering,
  which does not scroll when content overflows the available height.
- `DrawerItemsBuilder.build()` (`DrawerItemsBuilder.kt:47-190`) appends `Settings` then `Logout`
  as the **last two items**, after every activated feature section. For a project with Epics,
  Issues, Kanban, Wiki (2 sub-items) and Backlog/Sprints (3 sub-items) all active, that's 13 flattened
  items: ProjectSelector, Dashboard, Epics, Issues, Kanban, Team, WikiLinks, WikiPages, ScrumBacklog,
  ScrumOpenSprints, ScrumClosedSprints, Settings, Logout.
- **Reproduced on the desktop build** (`:composeApp:run`, same `commonMain` code path as
  Android tablet — `MainScreen.kt`'s width-gated branch isn't platform-specific), logged in
  against the local Taiga instance with a project that has all sections active:
  - At window size 1280×900, all 13 items fit and "Log out" is fully visible at the bottom of
    the rail.
  - At 1280×750, "Log out" is clipped to a sliver at the very bottom edge — present but not
    clickable/readable.
  - At 1280×700, "Log out" is entirely off-screen; "Settings" (the item just above it) is also
    partially clipped. There is no scrollbar, scroll gesture, or overflow affordance — the item
    is simply unreachable.
  - Screenshots taken during this session (not committed): `shot1.png` (900, full), `shot2.png`
    (750, clipped), `shot3.png` (700, gone).
- This is exactly the constrained-height case a tablet in landscape is prone to: enough width to
  cross into the rail/permanent-drawer breakpoint, but not enough height to fit every flattened
  item without scrolling — worse the more sections a project has active.
- `docs/architecture/tablet-form-factor-support/IMPLEMENTATION_PLAN.md:73-75` and
  `CHECKLIST-DONE.md:36-64` already discuss the `DrawerItem` → `NavigationSuiteScope` mapping
  (header/group-label/divider dropped, Logout kept as a flat item) but don't mention scrolling —
  the decision covered *which items* survive flattening, not *whether they fit on screen*. So this
  is a genuinely new gap, not a re-flag of the known chrome-parity tradeoff.
- `FlattenForNavigationSuiteTest.kt` and `DrawerItemsBuilderTest.kt` both assert Logout survives
  in the *data* (the flattened list), which is correct and still true — the bug is purely in
  layout/rendering, not in what items exist.

## Root cause

`TaigaNavigationSuiteWidget` (`TaigaDrawerWidget.kt:145-184`) passes an unbounded list of
`navigationSuiteItems` into `NavigationSuiteScaffold` with no scrollable wrapper, unlike the phone
drawer's explicit `verticalScroll`. When the flattened item count × item height exceeds the
available window/screen height — which a landscape tablet's limited height combined with a
project that has several optional sections (Wiki, Backlog/Sprints) active makes easy to hit — the
trailing items (Settings, and especially Logout, the very last item) render past the bottom edge
and are not reachable by any means.

## Impact

Any tablet (or desktop window) user whose project has enough active sections, or whose window/
screen height is short enough, cannot log out through the drawer at all. No workaround inside the
app exists (no scroll, no overflow menu) — the only way out is external (clearing app data,
force-stop, or resizing/rotating the window past the point of coincidentally fitting everything).
Severity: high for correctness (a used, expected action becomes completely inaccessible) but
narrow blast radius (only the medium/expanded-width nav chrome; phone is unaffected).

## Open questions

- What's the real Medium_Tablet AVD height in dp, to confirm this reproduces at that exact
  device's landscape resolution (not just an arbitrarily shrunk desktop window)? Not yet checked
  this session — the desktop repro is a reasonable proxy per CLAUDE.md's `expect`/`actual`
  testing guidance (same `commonMain` code, real layout engine), but device-height confirmation
  is still open.
- Does this also affect `NavigationRail` at the medium-width breakpoint specifically, or only the
  `PermanentNavigationDrawer` at expanded width? Not distinguished in this investigation — both
  share the same unscrolled `navigationSuiteItems` call, so both are suspected, but only one
  layout type was actually observed in the screenshots (whichever `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo`
  picked for 1280dp width).

## Options

1. **Wrap `navigationSuiteItems` content in a scrollable container**, mirroring the phone
   drawer's `verticalScroll`. Material3's `NavigationSuiteScaffold` doesn't expose a built-in
   scroll option for the item list itself (it's a fixed slot API), so this likely means composing
   the rail/drawer manually instead of via `NavigationSuiteScaffold` — a larger change to
   `TaigaNavigationSuiteWidget`.
   - Pros: preserves every item, fixes the bug at the root, matches phone behavior (nothing is
     ever unreachable).
   - Cons: gives up `NavigationSuiteScaffold`'s automatic rail/permanent-drawer breakpoint
     switching — would need to reimplement or heavily customize; larger, riskier diff on an
     experimental API (`ExperimentalMaterial3AdaptiveNavigationSuiteApi`, material3 `1.10.0-alpha05`).
   - Risk: scrolling a `NavigationRail`/`PermanentNavigationDrawer` is not how Material3 expects
     these components to be used (they're designed for a small, fixed destination count) — a
     custom scroll wrapper may fight the component's own sizing/ripple/selection-indicator
     behavior.

2. **Reduce the flattened item count on tablet** so it fits within realistic tablet heights —
   e.g. move `Settings` (and maybe `Logout`) out of the rail entirely into the top app bar's
   overflow menu or a profile/account menu, instead of listing every project section flat.
   - Pros: addresses the root cause (too many items, not just missing scroll) without fighting
     `NavigationSuiteScaffold`; likely a smaller, more idiomatic Material3 pattern (nav
     rails/permanent drawers are meant for primary destinations only, not admin actions).
   - Cons: diverges further from phone's drawer content parity; needs a placement decision
     (top bar overflow? a persistent account icon?) — new UI surface, not just a fix.
   - Risk: moderate — touches `MainScreen.kt`'s top bar as well as the drawer widget.

3. **Cap/collapse optional groups** (Wiki, Scrum) on tablet the way `DrawerItem.Group` labels
   are already dropped, e.g. collapse each group into a single overflow entry instead of flattening
   every sub-item — reduces worst-case item count without moving Settings/Logout anywhere.
   - Pros: keeps Settings/Logout in their current, familiar place; smaller conceptual change than
     option 2.
   - Cons: doesn't fully solve it — a project with Epics+Issues+Kanban+Team+2 groups still adds up
     fast, and this only buys a few items of headroom, not a guarantee of always fitting.
   - Risk: low, but may just delay the same bug to a slightly more-loaded project.

4. **Do nothing / accept as a known limitation.**
   - Pros: zero effort.
   - Cons: Logout being unreachable is not a cosmetic issue — it's a core account action.
     Not acceptable as a permanent state.

**Recommendation:** Option 1, but scoped down — rather than abandoning `NavigationSuiteScaffold`
entirely, first check whether `NavigationSuiteScaffold`'s underlying `NavigationRail`/
`PermanentNavigationDrawer` slot accepts a custom composable via `navigationSuiteColors`/layout
params that already supports scaffolding a scrollable column (needs a spike before committing to
"reimplement it manually"). If that's not viable, fall back to option 2 for `Logout` specifically
(it's the one item whose complete inaccessibility is the actual reported bug) while leaving
Settings and section items as-is — smallest change that guarantees the action reported missing is
always reachable, without redesigning the whole rail.

## Decision

gregory: rather than any of the drawer-scroll/reorganize options above, **remove Logout from the
drawer/rail entirely (both phone and tablet) and move it into the Settings screen instead** — not
just as a fix for the tablet clipping, but because the drawer was never the right place for it
regardless of form factor. This sidesteps the clipping bug by removing its cause rather than
accommodating it, and reduces the flattened tablet rail item count by one everywhere.

Decided 2026-08-26. Proceeding to implementation.

## What landed

- `DrawerDestination.Logout` removed; `DrawerItemsBuilder` no longer appends it.
- `TaigaDrawerWidget`/`TaigaNavigationSuiteWidget`'s `onDrawerItemClick` no longer special-cases
  Logout — it's just `appState.navigateToTopLevelDestination(item)` now.
- The confirm-dialog + `logoutSuspend()` flow moved from `MainScreenState`/`MainViewModel`
  (both deleted/trimmed) into `SettingsState`/`SettingsViewModel`, which now injects
  `AuthStateManager` directly (already reachable via `core.storage`, already a dependency of
  `feature/settings/ui`). `SettingsScreen` renders a "Log out" `ListItem` as the last row and
  reuses `ConfirmActionDialog`, matching the existing row/dialog pattern in that screen.
  `MainScreen`'s `LaunchedEffect` collecting `authStateManager.logoutEvents` and navigating to
  Login was left untouched — it's independent of which screen triggers the logout.
- Tests: `DrawerItemsBuilderTest`/`FlattenForNavigationSuiteTest` updated (Logout fixture swapped
  for Settings); `MainViewModelTest`'s logout test removed; new `SettingsViewModelTest` (init +
  logout coverage) and a `SettingsScreenContentTest` case driving the confirm dialog through the
  real `SettingsViewModel` added.
- Verified: full `./gradlew jvmTest` green, `ktlintCheck` clean, `:androidApp:assembleFdroidDebug`
  builds. Visual verification on-device (phone and tablet) left to gregory rather than a desktop
  GUI check this session.

## Follow-up: crash on logout from Settings (fdroid debug, real device)

gregory hit this immediately when testing the moved button:

```
java.lang.IllegalStateException: The query result was empty, but expected a single row to return
a NON-NULL object of type 'com.grappim.taigamobile.core.storage.db.entities.ProjectEntity'.
    at com.grappim.taigamobile.core.storage.db.dao.ProjectDao_Impl.getProjectById$lambda$0(...)
```

**Root cause:** `SettingsViewModel.init` calls `projectsRepository.getCurrentProjectSimple()`
(`ProjectsRepositoryImpl.kt:77-81`), which does a non-null single-row Room query
(`ProjectDao.getProjectById`, unguarded suspend fun). That call already existed before this task —
untouched by the drawer→Settings move. What's new is that **Logout now lives on the same screen,
in the same `ViewModelScope`**, as that async project load: the init coroutine and `logout()`'s
coroutine are two independent, concurrently-running coroutines on the same `viewModelScope`. If the
user opens Settings and confirms Logout before the init load's Room read has completed,
`authStateManager.logoutSuspend()` can call `databaseWrapper.clearAllTables()` and delete the
project row while the init coroutine is still awaiting (or about to run) that same query — which
then throws instead of returning a row, and nothing catches it. Before this task, Logout lived on a
different screen (`MainScreen`'s drawer) entirely, so this race wasn't reachable by any normal
sequence of taps.

**Fix:** `SettingsViewModel.kt` — (1) wrapped the init's `getCurrentProjectSimple()` call in
`resultOf { }.onSuccess { }.onFailure { logcat(ERROR, ...) }` (the same pattern
`ModulesViewModel`/`ProjectDetailsViewModel` already use) so a race that still gets through is
logged, not fatal; (2) `logout()` now cancels the init's `Job` (`loadCurrentProjectJob?.cancel()`)
before starting `authStateManager.logoutSuspend()`, closing the race window in the common case
instead of just papering over the crash. Added `SettingsViewModelTest`'s
`` `on init - project load throws - canSeeAttributes stays false` `` regression test.

Not touched (out of scope, wider blast radius): `ProjectDao.getProjectById`'s non-null contract
itself, and `GetKanbanDataUseCase`'s other call site — both pre-existing and not implicated in this
specific race.
