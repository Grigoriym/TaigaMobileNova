# Navigation Regression Checklist — PR #362

Manual verification checklist for the Nav2 → Nav3 migration (steps 6–10) and the width-responsive
nav chrome (steps 2–4, 13) landed in PR #362. Not a multi-session `CHECKLIST.md` step — a one-time
QA pass to run (e.g. via the **emulator-testing** skill) before treating the PR's navigation
behavior as verified. Grouped by risk area, not screen-by-screen.

Priority if time-constrained: **A → C → E** — that's where the Nav3 rewrite (single-top
sub-stacks, `ResultBus`) and this PR's new nav-rail gating logic actually live. D and F exercise
`Navigator`/`NavigationState` primitives that are lower-risk but foundational.

## Bugs found (fix after the full checklist pass, same PR)

- [x] **Drawer swipe gesture stayed enabled on detail screens (regression from Nav2).**
      Production disables the swipe-to-open-drawer gesture once you've pushed past a top-level
      section's root screen (e.g. Issue Details). On this branch it stayed enabled everywhere
      inside a section. Root cause: `MainAppState.currentTopLevelDestination`
      (`MainAppState.kt:72-78`) was built from `NavigationState.currentTopLevelKey`, which
      identifies the *active section*, not the *currently visible screen* — so it stayed non-null
      even on pushed detail screens. Nav2's equivalent used the actual visible
      `currentDestination`, so it correctly went null there. Fixed by switching the source to
      `NavigationState.currentKey` (the visible screen) instead. Fixed and pushed.
- [ ] **Logout doesn't clear the login form.** After logging out, the Login screen's
      username/password fields still show the previously-entered values. gregory suspects this is
      the ViewModel/state surviving because login+logout happened in one session (no process
      death) rather than the fields being persisted — needs confirming, not yet root-caused.
- [ ] **Open/Closed Sprints flash "no sprints found" before loading.** Navigating to Scrum Open
      Sprints or Closed Sprints briefly shows the empty state (text + button) for a moment, then
      the actual sprint list — looks like the empty state isn't gated on a loading flag.

## A. Top-level section switching (the `TOP_LEVEL_KEYS` sub-stack model)

- [x] From Dashboard, open each drawer/rail item once: Epics, Issues, Kanban, Team, Wiki Pages,
      Wiki Links, Settings, Scrum Backlog, Scrum Open Sprints, Scrum Closed Sprints — each loads
      correctly. Note: surfaced the two bugs above, tracked separately — not blocking.
- [ ] Push a detail screen in one section (e.g. open an Issue), switch to a different top-level
      section, switch back — **the first section's sub-stack should still show the detail
      screen**, not reset to its root list
- [ ] Repeat that for at least 2 more sections — this is the single-top-per-section behavior
      that's easiest to regress

## B. Push/pop within a section (back button + system back)

- [ ] Dashboard → Epics list → Epic details → back → back — lands correctly at each step
- [ ] Issues list → Issue details → back
- [ ] Kanban → Task/UserStory details → back
- [ ] Scrum Backlog/Sprints → Task details → back
- [ ] Wiki Pages → a wiki page → back
- [ ] Settings → Attributes → Tags → back → back (nested settings sub-screens)
- [ ] Team → Profile → back

## C. Cross-section pushes (non-top-level destinations reached from multiple places)

- [ ] Open a Task from Kanban, then open the *same* task type from Scrum — confirm no stale state
      bleeds across (this is exactly what the `ResultBus` collision bug in step 12 was about —
      same risk class applies to task/epic/issue details reached from different sections)
- [ ] `CreateTaskNavDestination` flow from at least two different entry points (e.g. Kanban "+"
      and Backlog "+")
- [ ] Work-item edit sheets (tags, description, epic, sprint, team member) opened from a Task and
      from a UserStory — save and cancel both, confirm the underlying detail screen refreshes
      correctly on return (this is the `UPDATE_DATA_ON_BACK`/`ResultBus` path — highest-risk area
      from the migration)

## D. `resetTo` / `replaceCurrent` transitions

- [ ] Login → successful auth → Project Selector → pick project → Dashboard (should wipe all
      sub-stacks, not leave Login/ProjectSelector reachable via back)
- [ ] Logout from Settings → back to Login (same full-reset check)
- [ ] Wiki create-page → save → confirm it hands off to the created page (`replaceCurrent`) and
      back from there goes to Wiki list, not back to the create form

## E. Width-responsive chrome (steps 2–4, 13)

- [ ] Resize the desktop window across the medium/expanded breakpoint — nav chrome switches
      between rail and drawer without losing current screen/back stack
- [ ] Nav rail is gated on login state (step 13) — confirm it does **not** show on the
      Login/ProjectSelector screens even at wide width, only after landing on a top-level section
- [ ] On a tablet AVD, same check: nav rail at wide, drawer at compact, no chrome flash on
      rotation

## F. Process death / restore

- [ ] Navigate a few levels deep (e.g. Dashboard → Epics → Epic details), kill the app process
      (not just background), reopen — `navSavedStateConfiguration` should restore the same
      screen and back stack
- [ ] Same check after a config change (rotation on a phone AVD, or resize on desktop) — should
      NOT be treated as process death, no navigation state loss

## G. Desktop refresh shortcut (last commit, unrelated to Nav3 but same PR)

- [ ] Ctrl+R / F5 refreshes the current screen's data on desktop, on at least 2 different screens
      that support pull-to-refresh
- [ ] Navigate away and back, confirm the shortcut still fires (this is the exact regression
      called out in the code comment — focus-stealing broke it before)
