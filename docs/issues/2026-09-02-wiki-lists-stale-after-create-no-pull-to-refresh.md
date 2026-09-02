# Wiki "All Pages" and Bookmarks lists don't refresh after creating an item; no pull-to-refresh

**Status:** Done
**Link:** none (reported directly by gregory during 2.2.0 release testing)
**Updated:** 2026-09-02

## Report

"Once the wiki page/bookmark is created the lists in both are not updated and we
don't have a pull to refresh as well, so to see a new page I need to reopen the app."

Two symptoms bundled together:
1. After creating a wiki page (or a bookmark), the "All Pages" list (or Bookmarks
   list) doesn't show the new item.
2. There's no pull-to-refresh on those lists to force a reload by hand.

Environment: not stated; desktop app was in use this session for 2.2.0 release
testing, but nothing found below is platform-specific.

## Findings

**Both list screens already have a working, unused `refresh()` callback.**
`WikiPagesState.refresh` / `WikiBookmarksState.refresh` are both wired to their
ViewModel's `loadData()` (confirmed earlier this session:
`WikiPagesViewModel`/`WikiBookmarksViewModel`), and both screens already call it from
one place: the error-state "Retry" button (`onRetry = state.refresh` in
`WikiListContentWidget`) and from `onDeleteSuccess`
(`WikiPagesScreen.kt:65-67`, `WikiBookmarksScreen.kt`). Nothing else invokes it.

**`WikiListContentWidget` has no pull-to-refresh** — confirmed by reading the whole
file (`feature/wiki/ui/.../widgets/WikiListContentWidget.kt`): no
`PullToRefreshBox`, no swipe handling at all, list/loading/error/empty states are a
plain `when` inside a `Column`.

**This is a real gap relative to the rest of the app, not a missing feature
overall.** `grep -rln "PullToRefresh"` across the repo finds it wired into 11 other
list screens (Epics, Issues, Sprints, Kanban, Dashboard, Team, Project Selector,
Tags, Project Values, Scrum Backlog, Sprint) — `feature/wiki` is the only list-style
feature missing it. `EpicsScreen.kt:143-149` is the reference shape:
```kotlin
PullToRefreshBox(
    modifier = Modifier.fillMaxSize(),
    onRefresh = { epics.refresh(); state.retryLoadFilters() },
    isRefreshing = epics.isLoading() || state.isFiltersLoading
) { ... }
```

**Desktop has its own established equivalent, also absent from Wiki.**
`buildDesktopRefreshTopBarAction` + `DesktopRefreshEffect`
(`uikit/.../topbar/DesktopRefreshRegistry.kt`) add a top-bar refresh icon and a
Ctrl+R/F5 shortcut on desktop, since there's no swipe gesture there — the doc
comment on that file explains exactly why (`docs/revisit.md` #44 tracked this as a
design gap until `tablet-form-factor-support` step 15 built it; it's finished
infrastructure now, not an open question). `EpicsScreen.kt:84-89,137-142` shows both
wired alongside `PullToRefreshBox`. Wiki uses neither.

**The actual "doesn't refresh after create" symptom has its own established fix
already built and used by four other features, also absent from Wiki.** `ResultBus`
(`core/navigation/.../ResultBus.kt`) plus a shared `data object UpdateDataOnBack`
(`composeApp/.../main/MainNavHost.kt:301`) is the app's existing "tell the list
screen to refresh when the user leaves a detail screen" mechanism — used by
`EpicNavGraph.kt`, `IssueNavGraph.kt`, `TaskNavGraph.kt`, `UserStoryNavGraph.kt`.
The shape (`EpicNavGraph.kt:27-40`):
```kotlin
entry<EpicsNavDestination> {
    var updateData by remember { mutableStateOf(false) }
    ResultEffect<UpdateDataOnBack> { updateData = true }
    EpicsScreen(..., updateData = updateData, ...)
}
entry<EpicDetailsNavDestination> { route ->
    ...
    goBack = {
        resultBus.sendResult(UpdateDataOnBack)
        navigator.goBack()
    },
    ...
}
```
and the list screen turns that boolean into a reload
(`EpicsScreen.kt:101-105`):
```kotlin
LaunchedEffect(updateData) {
    if (updateData) { epics.refresh() }
}
```
Because `WikiCreatePageScreen`/`WikiCreateBookmarkScreen` hand off to
`WikiPageNavDestination` with `replaceCurrent = true` (`WikiNavGraph.kt:21-38`), the
create flow becomes indistinguishable from "opened this page normally" the moment
creation succeeds — from then on, leaving via the details screen's back arrow is
the only exit, exactly the point where every other feature sends `UpdateDataOnBack`.
`WikiNavGraph.kt`'s `WikiPageNavDestination` entry currently just does
`goBack = { navigator.goBack() }` (no `resultBus.sendResult`), and neither
`WikiPagesNavDestination` nor `WikiLinksNavDestination` observes
`ResultEffect<UpdateDataOnBack>` at all.

**No screen in the app auto-refreshes purely on becoming visible again** (confirmed:
`grep -rln "ON_RESUME\|LifecycleEventEffect\|LifecycleResumeEffect"` across the repo
returns nothing) — the app's only two refresh triggers, everywhere they exist, are
pull-to-refresh/desktop-refresh (manual) and the `UpdateDataOnBack` signal sent
specifically by a details screen's back action (semi-automatic: fires once per
"visited a detail screen and came back", not on every recomposition). Wiki has
neither, which is why only a full app restart (a fresh ViewModel, hence a fresh
`loadData()` in `init`) currently shows a newly created page.

## Root cause

`feature/wiki`'s two list screens (`WikiPagesScreen`, `WikiBookmarksScreen`) were
never wired into either of this app's two established list-freshness mechanisms:
pull-to-refresh (+ its desktop equivalent), used by every other list screen in the
app, and the `ResultBus`/`UpdateDataOnBack` "refresh when returning from a detail
screen" signal, used by every other create-then-view flow in the app
(Epic/Issue/Task/UserStory). Both are missing purely by omission — nothing about
Wiki's data flow is special, and both mechanisms already exist, tested, in shared
code (`uikit`'s `DesktopRefreshRegistry`, `core/navigation`'s `ResultBus`).

## Impact

Every wiki page or bookmark created (or deleted from the details screen, though
delete isn't in the report) is invisible in its list until the process is killed and
the app is relaunched — the only workaround today. No data loss (unlike the sibling
bug in `2026-09-02-wiki-bookmark-edit-description-save-no-op.md`), just staleness.

## Open questions

- None blocking. The two mechanisms to adopt are both already used elsewhere in this
  exact shape, so there's no design decision left to make — just applying the
  existing pattern to two more screens.

## Options

1. **Adopt both existing mechanisms, matching `EpicsScreen`'s shape exactly.**
   - `WikiPagesScreen`/`WikiBookmarksScreen`: add `PullToRefreshBox` +
     `buildDesktopRefreshTopBarAction`/`DesktopRefreshEffect` around/alongside the
     existing `WikiListContentWidget` content, wired to the existing `state.refresh`
     — no new ViewModel code needed, the callback already exists and is already
     tested indirectly via the delete-success and retry paths.
   - `WikiNavGraph.kt`: `WikiPageNavDestination`'s `goBack` sends
     `resultBus.sendResult(UpdateDataOnBack)` before `navigator.goBack()`, matching
     `EpicNavGraph.kt`; `WikiPagesNavDestination`/`WikiLinksNavDestination` entries
     each add `ResultEffect<UpdateDataOnBack> { updateData = true }` and pass
     `updateData` through to their screens, which each get a
     `LaunchedEffect(updateData) { if (updateData) state.refresh() }`.
   - Pros: reuses fully-built, already-used-elsewhere primitives; both symptoms in
     the report resolved (manual refresh via swipe/desktop-icon, and automatic
     refresh on returning from a newly created or visited page); minimal risk since
     the same shape already ships in four other features; small diff (two screen
     files touched for pull-to-refresh, one nav-graph file + two screen files for
     `UpdateDataOnBack`, no ViewModel changes).
   - Cons: `WikiPageNavDestination`'s `goBack` lambda needs `LocalResultBus.current`
     threaded in (one extra line, same as `EpicDetailsNavDestination`'s entry
     already does) — trivial but touches a shared nav-graph file. `WikiListContentWidget`
     currently renders loading/error/empty/list as one `Column` with no scrollable
     container in the loading/error/empty branches — `PullToRefreshBox` wraps
     whatever's passed to it, so the swipe gesture will visually work over those
     states too (matches `EpicsScreen`, not a new problem, but worth confirming on
     device since Wiki's error/empty states are custom widgets not used by Epics).
   - Blast radius: `WikiPagesScreen.kt`, `WikiBookmarksScreen.kt`, `WikiNavGraph.kt`.
     No domain/data-layer changes.

2. **Pull-to-refresh only, skip `UpdateDataOnBack`.** Gives the user a manual escape
   hatch (matches the literal "we don't have a pull to refresh" part of the report)
   but leaves "once ... created ... not updated" genuinely unfixed — the user would
   still see a stale list immediately after creating a page and have to know to pull
   down. Not recommended: the report explicitly calls out both, and the
   `UpdateDataOnBack` half is not meaningfully more work once pull-to-refresh is
   already being added (it's the same pattern, one more nav-graph file).

3. **Do nothing.** Leaves the app restart as the only way to see new wiki content —
   the only Wiki-specific paper cut of this kind in the app (11 other list screens
   already have pull-to-refresh; 4 other create/detail flows already have
   `UpdateDataOnBack`). Not recommended.

**Recommendation: Option 1.** Both halves of the report are real, both already have
a proven, low-risk fix shape used elsewhere in this exact codebase, and adopting
them doesn't require any new design decisions or ViewModel changes — `state.refresh`
already exists and is already exercised by the retry/delete paths.

## Decision

Approved: Option 1, fix in the current release branch (`release/v2.2.0`, PR #379).

## What landed

- `WikiPagesScreen.kt`/`WikiBookmarksScreen.kt`: added `PullToRefreshBox` +
  `buildDesktopRefreshTopBarAction`/`DesktopRefreshEffect` around/alongside the
  existing `WikiListContentWidget` content, wired to the existing `state.refresh`.
  Added an `updateData: Boolean` parameter with a `LaunchedEffect(updateData) { if
  (updateData) state.refresh() }`, matching `EpicsScreen`.
- `WikiListContentWidget.kt`: changed the full-screen-loader branch from `isLoading`
  to `isLoading && items.isEmpty()` — otherwise every refresh (pull-to-refresh or
  `UpdateDataOnBack`) would blank the whole list behind `PullToRefreshBox`'s own
  spinner instead of keeping it visible underneath, the opposite of how every other
  `PullToRefreshBox` screen in the app behaves.
- `WikiNavGraph.kt`: `WikiPageNavDestination`'s `goBack` now sends
  `resultBus.sendResult(UpdateDataOnBack)` before `navigator.goBack()`;
  `WikiPagesNavDestination`/`WikiLinksNavDestination` entries each add
  `ResultEffect<UpdateDataOnBack> { updateData = true }` and pass `updateData`
  through, matching `EpicNavGraph`.
- `WikiPageScreen.kt`: **also** had to add `onBackClick = { goBack() }` to its
  `NavigationIconConfig.Back()` — found while implementing, not in the original
  investigation. Without it, the top-bar back arrow (the primary way users leave this
  screen) doesn't call the screen's own `goBack` param at all; it falls through to a
  `defaultGoBack` wired centrally in `MainScreen.kt`, bypassing the `UpdateDataOnBack`
  signal entirely. `EpicDetailsScreen` already does this; Wiki's details screen didn't.

**Verified:**
- `./gradlew :feature:wiki:ui:jvmTest jvmTest`, `ktlintCheck`, `koverXmlReport` +
  `:koverVerify` all green, no regressions.
- Desktop (`:composeApp:run`): confirmed the app boots and renders with these
  changes, the Bookmarks list and the new desktop refresh icon render correctly.
  Click-driven interaction (create → back → confirm refresh) could not be completed
  on desktop — this machine's `xdotool` click/type delivery to the Compose Desktop
  window is unreliable (documented separately, e.g. memory `local-taiga-instance`,
  since 2026-08-09/2026-08-22/2026-08-29) — not a sign of a problem with the fix.
- **Android emulator (`Medium_Phone_API_36.1`, fdroid debug build) — full flow
  confirmed working:**
  - Bookmarks: created a bookmark ("refresh-fix-check") → landed on details → tapped
    the top-bar back arrow → the new bookmark appeared in the Bookmarks list
    immediately, no manual refresh needed.
  - All Pages: created a page ("refresh-page-check") → landed on details → tapped
    back → the new page appeared in the All Pages list immediately.
  - Pull-to-refresh: a swipe-down gesture on the Bookmarks list fired a real
    `GET /api/v1/wiki-links` request (confirmed via `adb logcat`), not just a visual
    spinner.
  - Both test items were deleted afterward to avoid leaving clutter in gregory's
    shared local Taiga test data.

**Incident during verification (disclosed and resolved with gregory):** a coordinate-
scaling mistake in two `adb shell input tap` calls (forgot the screenshot-to-device
1.2× multiplier) accidentally deleted gregory's pre-existing test wiki page `plplpl`
(id 17, content `"jiij"`) instead of the intended cleanup target. Confirmed via the
`DELETE /api/v1/wiki/17` line in logcat. Disclosed immediately; gregory confirmed it's
a testing instance and to proceed without recreating it.
