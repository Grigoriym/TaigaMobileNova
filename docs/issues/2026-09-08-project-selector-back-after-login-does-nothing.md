# 2026-09-08 — Back does nothing on Select Project screen right after login

**Status:** Done
**Link:** reported by gregory in conversation (no GitHub issue)   **Updated:** 2026-09-08

## Report

gregory (2026-09-08): "when we log in, we go to select project screen, and here we cannot go
back neither back gesture nor back button do not work. When I am logged in and I go to Select
Project screen the back behavior works, i.e. we leave the app."

Two states of the same screen, different back behavior:

| Path to Select Project | Back gesture / back button |
|---|---|
| Fresh login → Select Project | Nothing happens (no navigation, no exit) |
| Already logged in, switch to Select Project via drawer | App exits, as expected |

Not stated: platform (Android is the only one with a hardware/gesture "back button" in the
literal sense reported, so this investigation assumes Android; the same code path is shared by
desktop/iOS but they have no back gesture to test this way) and app version. Not needed to find
the cause — the difference between the two paths only exists in one place in the code (see
below), and that place changed very recently.

## Findings

- `ProjectSelectorNavDestination` is a **top-level key** (`MainAppState.kt:36`, in
  `TOP_LEVEL_KEYS`), not a leaf screen — it gets its own slot in `Navigator`'s `topLevelStack`.
- `ProjectSelectorScreen.kt:100-107` installs its own `NavigationBackHandler`:
  ```kotlin
  NavigationBackHandler(
      state = rememberNavigationEventState(NavigationEventInfo.None),
      isBackEnabled = state.isFromLogin,
      onBackCompleted = {
          state.onGoingBackAfterLogIn()
          goBack()
      }
  )
  ```
  `isBackEnabled = state.isFromLogin` — enabled only on the fresh-login path, disabled when the
  screen is reached any other way (e.g. the drawer). `goBack` is wired in
  `MainNavHost.kt:158-160` to plain `navigator.goBack()`.
- `state.onGoingBackAfterLogIn()` → `ProjectSelectorViewModel.onGoingBackAfterLogIn()`
  (`ProjectSelectorViewModel.kt:59-63`) calls `dataCleaner.cleanOnGoingBackAfterLogin()` — this
  is a deliberate "abandon the fresh login" feature: back out before picking a project, and the
  session that login just created gets wiped.
- `LoginScreen`'s `onLoginSuccess` (`MainNavHost.kt:144`) calls
  `navigator.navigateToProjectSelector(isFromLogin = true)`. `MainAppState.kt:34-48`'s
  `TOP_LEVEL_KEYS` includes `LoginNavDestination` itself, and `rememberMainAppState()`
  (`MainAppState.kt:53`) seeds `startKey = LoginNavDestination` — so `topLevelStack` starts as
  `[Login]`.
- **Before PR #392** (`fix/nav-drawer-back-stack-cascade`, 2026-09-07): `Navigator.goToTopLevel()`
  pushed onto `topLevelStack` (`removeAll { it::class == key::class }` then `add(key)`).
  Login → ProjectSelector(isFromLogin=true) therefore produced `topLevelStack = [Login,
  ProjectSelector]` (size 2). Pressing back then hit `Navigator.goBack()`'s
  `state.topLevelStack.size > 1` branch, which was **true** — it popped back to `Login`. That
  pop is exactly what `onGoingBackAfterLogIn()`'s session-wipe was designed to precede: "back out
  of project selection, land back on the Login screen, with the half-finished session cleaned
  up."
- **After PR #392**: `goToTopLevel()` now *replaces* the top of `topLevelStack` instead of
  pushing (`Navigator.kt:96-107`, current). Login → ProjectSelector(isFromLogin=true) now
  produces `topLevelStack = [ProjectSelector]` (size 1). `Navigator.goBack()`'s
  `state.topLevelStack.size > 1` check is now **false** — `goBack()` returns `false` (unhandled)
  and does nothing.
- The `ProjectSelectorScreen` `NavigationBackHandler` does not look at `goBack()`'s return value
  — it calls `state.onGoingBackAfterLogIn()` and `goBack()` unconditionally inside
  `onBackCompleted`. Because `isBackEnabled = true` on this path, the handler still **consumes**
  the back gesture/button (per `androidx.navigationevent`'s docs quoted in
  `MainScreen.kt:176-178`, an enabled handler wins over letting the event fall through), so no
  other handler or the system gets a chance to act on it — the event is swallowed and nothing
  visible happens. This matches the report exactly: not "the wrong thing happens," but "nothing
  happens."
  - Aside: `onGoingBackAfterLogIn()`'s side effect (wiping the fresh session) *does* still fire on
    every back press on this path even though nothing else happens, since it runs before the
    now-inert `goBack()` call — not confirmed by a live run, but follows directly from the code
    order in `ProjectSelectorScreen.kt:103-106`. Not part of the reported symptom, but worth
    knowing: every "broken" back press on this screen is quietly deleting the session anyway.
  - Not confirmed live (no emulator run in this investigation) — this trace is static, from
    reading `Navigator.kt`, `ProjectSelectorScreen.kt`, `MainNavHost.kt`, and `MainAppState.kt`
    together. Treat the mechanism as strong inference, not an observed stack trace.
- On the **drawer path** (`isFromLogin = false`): the local `NavigationBackHandler` is disabled,
  so it doesn't intercept. No other handler in the tree calls `navigator.goBack()` for a
  top-level screen (`MainScreen.kt`'s own `NavigationBackHandler`, lines 188-196, only closes the
  drawer and is unrelated). With nothing consuming the event, the platform's own back handling
  takes over — which for a single-activity Compose app with no consumer is exiting. This is the
  behavior the reporter confirmed as correct, and it is unaffected by PR #392 either way (it
  never depended on `topLevelStack` size).

## Root cause

PR #392 (`fix/nav-drawer-back-stack-cascade`) changed `Navigator.goToTopLevel()` from
push-onto-`topLevelStack` to replace-the-top-of-`topLevelStack`, to fix drawer-section back
cascading (revisit #51). That fix was correct for the drawer case it targeted, but
`ProjectSelectorScreen`'s login-abandon feature (`isBackEnabled = state.isFromLogin`,
`ProjectSelectorScreen.kt:100-107`) depended on the *old* push behavior: it relies on
`navigator.goBack()` actually popping `topLevelStack` from `[Login, ProjectSelector]` back down
to `[Login]`. Under the new replace-only invariant, `topLevelStack` never grows past size 1
outside `resetTo()`, so that pop never had anything to do — `goBack()` now always returns `false`
here, and the screen's own `NavigationBackHandler` swallows the gesture regardless, since it
doesn't check the return value.

This is a real regression PR #392 introduced, not a pre-existing bug independently rediscovered
— `docs/archive/revisit-resolved.md`#51's own `Fix` note stated "no other file reads
`topLevelStack`/`goToTopLevel`/`currentTopLevelKey`," which was true by grep but missed that
`ProjectSelectorScreen`'s `isBackEnabled`/`goBack()` combination depends on `goBack()`'s *return
value having an effect*, not on reading the stack directly — the grep for direct readers didn't
catch a caller relying on the postcondition of `goToTopLevel()`+`goBack()` together.

## Impact

Every fresh login hits the affected path — this is not an edge case. A user who logs in, sees
the Select Project screen, and wants to back out (wrong server, wrong account, changed their
mind) has no way to do so via back gesture/button; they'd need to force-quit the app. The
session-wipe side effect firing silently on every such back press (see Findings) is a secondary,
less visible concern — it doesn't corrupt anything (the session was going to be abandoned anyway
in intent), but a user mashing back expecting *something* to happen may end up in a logged-out
state without understanding why, if they eventually give up and force-quit/relaunch.

No workaround exists from the Select Project screen itself once `isFromLogin=true` — the only way
out is to complete project selection (pick any project) or force-quit the app.

## Open questions

- Not reproduced live on an emulator in this pass — recommend confirming with the
  **emulator-testing** skill before landing a fix, both for the current broken state and for
  whichever fix is chosen.
- Whether any other top-level screen has a similar "back past the top-level boundary on this
  specific instance" pattern was checked (grepped every `NavigationBackHandler`/`isBackEnabled`
  call site) — `ProjectSelectorScreen` is the only one wired to a plain `navigator.goBack()` where
  the *destination* is itself a top-level key; every other `NavigationBackHandler` site
  (`UserStoryDetailsScreen`, `TaskDetailsScreen`, `EpicDetailsScreen`, `IssueDetailsScreen`,
  `SprintScreen`, the `WorkItemEdit*` screens) is a leaf screen whose `goBack()` pops a sub-stack,
  unaffected by `goToTopLevel()`'s behavior either way.

## Options

**A. Give `Navigator` an explicit "step out of the top-level boundary" primitive**, and have
`ProjectSelectorScreen`'s login-abandon path call it instead of plain `goBack()`. E.g.
`Navigator.exitToStart()` (or similarly named) that always does what the old push-based
`goBack()` did for this one case: land on `startKey` (`Login`). Concretely: set
`topLevelStack[lastIndex] = state.startKey`, mirroring `resetSubStackTo`'s replace shape, but
targeting the start key regardless of what's currently active.
- Pros: keeps revisit #51's fix intact for every drawer section (no reintroduced cascading);
  gives the login-abandon feature a primitive whose contract actually matches what it needs
  ("cancel login, return to Login") rather than piggybacking on generic back-stack popping.
- Cons: adds a second "step back to start" method alongside `resetTo()`/`goBack()` — another
  `Navigator` primitive to document and test. Only one call site needs it today.
- Risk/blast radius: contained to `Navigator.kt` + `ProjectSelectorScreen.kt`; doesn't touch
  `goToTopLevel()`'s general shape, so #51 stays fixed for everything else.

**B. Make `ProjectSelectorScreen`'s `goBack` call site closer to what it actually means**: since
`isFromLogin` only exists to gate this one behavior, replace the `NavigationBackHandler` + plain
`navigator.goBack()` combination with a direct call — e.g. `navigator.resetTo(LoginNavDestination)`
(already exists, already tested) instead of relying on `goBack()`'s stack-popping side effect.
- Pros: no new `Navigator` API; reuses `resetTo()`, which already wipes every section's history —
  arguably more correct than the old behavior, since abandoning login should probably not leave
  stale sub-stack history sitting in other sections either (the old push-based `goBack()` only
  popped `topLevelStack`, it never touched `subStacks` contents).
- Cons: behavior change beyond "restore the old back button" — `resetTo()` wipes *every* section's
  sub-stack, not just `topLevelStack`'s entry; if any section had accumulated state before login
  (unlikely, since Login/ProjectSelector are the only reachable screens pre-project-selection, but
  worth confirming), this is a slightly bigger reset than before.
- Risk/blast radius: contained to `ProjectSelectorScreen.kt`'s `goBack` handling; no `Navigator`
  API change at all.

**C. Do nothing / won't fix.** Not viable — this blocks a real, common user action (backing out
of a fresh login) with no workaround. Listed for completeness only.

## Recommendation

**Option B.** It fixes the regression with zero new `Navigator` surface, reuses an already-tested
primitive (`resetTo()`, covered by `NavigatorTest.kt`'s `resetTo wipes every section...` case),
and its "wipe everything, not just the top-level slot" behavior is arguably the *more* correct
interpretation of "abandon this login" than the old code's narrower pop ever was — there's no
legitimate reason for any section to carry meaningful state before a project has even been
selected. Concretely: in `ProjectSelectorScreen.kt`, change both the `NavigationIconConfig.Back`
`onBackClick` (line 84-89) and the `NavigationBackHandler`'s `onBackCompleted` (line 103-106) from
`{ state.onGoingBackAfterLogIn(); goBack() }` to a new `onCancelLogin: () -> Unit` param that the
`MainNavHost.kt` call site wires to `{ navigator.resetTo(LoginNavDestination) }` (keeping
`state.onGoingBackAfterLogIn()` called first, unchanged) — leaving the existing `goBack` param
free for a future non-login-abandon use, and not overloading it with two meanings.

Option A is the fallback if gregory would rather keep `goBack()` as the single navigation exit
point from this screen and add the primitive at the `Navigator` layer instead.

## Decision

**Option B, chosen by gregory (2026-09-08).** Rewire `MainNavHost.kt`'s `ProjectSelectorScreen`
call site so its `goBack` param calls `navigator.resetTo(LoginNavDestination)` instead of
`navigator.goBack()` — no `ProjectSelectorScreen.kt`/`ProjectSelectorState.kt` change needed,
since `goBack` is only ever invoked on the `isFromLogin` path already (the non-login path shows
`NavigationIconConfig.Menu` and has `isBackEnabled = false`, so `goBack` is otherwise dead there).

One part, no test scaffolding exists for `MainNavHost`'s composable wiring (Nav3 graph, not a
unit-tested layer) — verify live via the **emulator-testing** skill: fresh login → Select Project
→ back gesture/button lands on Login (and the session-wipe still fires, unchanged); already-logged-in
→ drawer → Select Project → back still exits the app (this path is untouched by the fix); and a
quick re-check that ordinary drawer section switching (the PR #392 fix) still doesn't cascade.

## What landed

Changed `MainNavHost.kt`'s `ProjectSelectorNavDestination` entry: `goBack` now calls
`navigator.resetTo(LoginNavDestination)` instead of `navigator.goBack()`, with a comment
explaining why (only fires on the `isFromLogin` path, and `goBack()` has nowhere left to pop to
under PR #392's replace-not-push `topLevelStack`). No change to `ProjectSelectorScreen.kt`,
`ProjectSelectorState.kt`, or `Navigator.kt` — the fix is entirely at the call site.

Verified with `./gradlew jvmTest` (full suite green, no test asserted the old behavior) and
`./gradlew :composeApp:ktlintCommonMainSourceSetCheck` (clean), then live on
`Medium_Phone_API_36.1` (fdroid debug build) against the local Taiga instance:

1. Fresh install → login → Select Project → back button: now lands back on Login (session-wipe
   confirmed indirectly — logging in again required the full flow, including re-accepting the
   unencrypted-connection dialog, rather than resuming a live session).
2. Logged in → drawer → Select Project → back button: still exits the app to the home screen,
   unaffected (this path was never broken).
3. Regression check on PR #392: Dashboard → drawer Epics → drawer Issues → back button: still
   exits the app, does not cascade back to Epics.

Nothing deliberately left out — this was a one-line regression with a one-line fix.
