# Revisit list

Things noticed while doing something else, deliberately **not** fixed at the time, and worth coming
back to. This exists so that "I'll remember that" stops being the plan.

**The rule:** when work surfaces a real problem outside the current task's scope, add a row here and
keep going. Do not fix it inline (it makes the diff unreviewable) and do not drop it. Every entry
needs enough evidence that a cold session can pick it up without re-deriving anything — a `file:line`
or a doc link, not just a description.

The [testing improvement plan](testing/improvement-plan.md) that used to gate this list closed
2026-08-08. This list is unblocked; nothing here is urgent, nothing here is forgotten.

Resolved and moved-out entries (originally #2–#44) live in
[docs/archive/revisit-resolved.md](archive/revisit-resolved.md), split out 2026-09-06 once this file
passed 2200 lines. This file only tracks what's still open — no separate collapsible index needed
now that the table below covers everything left.

| # | Item | Size | Source |
|---|---|---|---|
| 1 | ViewModels doing I/O in `init` | M–L | [koingraphtest issue](issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md) |
| 45 | Tablet nav rail/permanent drawer still has no scroll safety net | S–M | [tablet nav rail issue](issues/2026-08-26-tablet-nav-rail-logout-clipped.md) |
| 46 | No deep-link readiness plan yet (3 queued Nav3 patterns) | — | [reference-app-scouting.md](../../agentic-grappim/investigations/reference-app-scouting.md) |
| 47 | `guardrails.yml`'s `push` trigger on `master` still diffs the wrong range after a release merge | S | — |
| 51 | Switching drawer sections is recorded on the back stack, so back cascades through prior sections | S–M | this file |

---

## 1. ViewModels doing I/O in `init`

**What:** ten ViewModels start a `viewModelScope.launch` from their `init` block that immediately
hits the repository layer:

```
feature/settings/ui/user/SettingsUserScreenViewModel.kt
feature/wiki/ui/page/details/WikiPageViewModel.kt
feature/wiki/ui/bookmark/list/WikiBookmarksViewModel.kt
feature/wiki/ui/page/list/WikiPagesViewModel.kt
feature/scrum/ui/backlog/ScrumBacklogViewModel.kt
feature/scrum/ui/open/ScrumOpenSprintsViewModel.kt
feature/workitem/ui/screens/sprint/EditSprintViewModel.kt
feature/epics/ui/list/EpicsViewModel.kt
feature/sprint/ui/SprintViewModel.kt
feature/issues/ui/list/IssuesViewModel.kt
```

**Why it came up:** constructing one of these is enough to fire a real network/DB call. That is what
made `KoinGraphTest` leak exceptions into unrelated tests. The leak is fixed at the test end
(`Dispatchers.Main` is replaced by a dispatcher that never runs), but **the fix depends on the
design staying as it is** — the first `viewModelScope.launch(Dispatchers.IO)` written in an `init`
block bypasses it and the flake returns with nothing pointing at the cause.

**The actual question** — worth answering properly, not just mechanically changing all ten:

- Is "construct = start loading" the behaviour we want? It couples object creation to I/O, which is
  why the object is untestable and unmockable without a live dispatcher.
- The alternative is an explicit `onScreenStart()` / `LaunchedEffect` trigger from the UI. That is
  more code per screen and easy to forget, which is presumably why `init` was chosen.
- There may be a middle option: keep `init` but have it collect a flow rather than run a one-shot
  suspend call, so there is nothing to throw at construction time.

**Do not treat this as a mechanical refactor.** Pick the pattern first, apply it to one ViewModel,
confirm the screen still behaves, then decide whether the other nine are worth touching. If the
answer is "the current design is fine, the test fix is sufficient" — that is a legitimate outcome,
but write down *why*, because the question will come back.

**Blocked on nothing.** Best done after the testing plan, since those ViewModels will have tests by
then and the tests are the safety net for changing them.

**Investigated (2026-08-08):** the testing plan is done, so this was picked up. Full options writeup
in [docs/issues/2026-08-08-viewmodel-init-io.md](issues/2026-08-08-viewmodel-init-io.md) — three
options (status quo / explicit `onScreenStart()` trigger / lazy `stateIn(WhileSubscribed)`
collection), recommending the explicit-trigger option. Not yet decided or implemented.

## 45. Tablet nav rail/permanent drawer still has no scroll safety net

**Where:** `TaigaNavigationSuiteWidget` (`composeApp/src/commonMain/kotlin/com/grappim/taigamobile/TaigaDrawerWidget.kt:145-184`).

**What:** investigated 2026-08-26 (`docs/issues/2026-08-26-tablet-nav-rail-logout-clipped.md`) —
the medium/expanded-width rail/permanent drawer passes every flattened `DrawerItem` into
`NavigationSuiteScaffold`'s `navigationSuiteItems` with no scrollable wrapper, unlike the phone's
`TaigaDrawerWidget` which explicitly wraps its item `Column` in `Modifier.verticalScroll(...)`.
Reproduced on the desktop build: at 1280×900 all items fit, at 1280×750 the last item is clipped to
a sliver, at 1280×700 it's fully off-screen and unreachable — no scrollbar or overflow affordance.

**What this session did:** removed `Logout` from the drawer/rail entirely (moved to Settings, see
the issue doc) — gregory's call, made independent of the clipping bug ("I didn't like it in the
drawer nevertheless"). That happens to reduce the flattened item count by one everywhere, but it
does **not** fix the underlying scroll gap. `Settings` is now the trailing item and will clip the
same way on a short enough window/screen height or a project with enough active sections (Wiki +
Backlog/Sprints groups both expand into multiple flat items).

**Why deferred:** out of scope for the Logout-relocation task; fixing it means either giving up
`NavigationSuiteScaffold`'s automatic rail/permanent-drawer breakpoint switching for a custom
scrollable layout, or capping/collapsing the flattened item count some other way — see Options 1–3
in the issue doc for the tradeoffs already weighed.

**Fix, if wanted:** pick up Option 1, 2, or 3 from `docs/issues/2026-08-26-tablet-nav-rail-logout-clipped.md`'s
Options section for the item(s) still at risk (Settings, and any project with both optional groups
active).

## 46. No deep-link readiness plan yet (3 Navigation 3 patterns queued for whenever links are added)

**What:** scouting HedvigInsurance's Navigation 3 codebase
(`agentic-grappim/investigations/reference-app-scouting.md`, 2026-09-01) surfaced three related
patterns, none applicable today — checked while writing them up: `core/navigation`'s `Navigator.kt`
has exactly one back primitive (`goBack()`, no deep-link-aware alternative), and
`composeApp/.../main/MainAppState.kt`'s `TOP_LEVEL_KEYS` is the only cross-cutting shell-level
concern that exists today. TaigaMobileNova has no deep-link handling anywhere (not exhaustively
audited, but none found while checking).

1. **Marker interfaces on `NavKey` for cross-cutting shell concerns** (Hedvig's `TopLevelTabRoot`,
   `DeepLinkAncestry`, etc., opted into per-key) — reach for this instead of a shell-level
   `when (key) { ... }` the moment the shell needs to ask "does this screen do X" for more than one
   X. Deep-link ancestry would be the second such concern, after "is this a top-level key."
2. **Reserve a deep-link-aware "up" for the top-app-bar back arrow only.** If `Navigator` ever grows
   a second, "smarter" pop method (to rebuild a synthetic parent stack for a screen reached via deep
   link), don't wire a plain in-content "done"/"close"/"continue" button to it — only the top bar's
   back arrow should get it. Mixing the two makes a button's behavior depend on how the screen was
   reached, and can diverge from predictive/system back.
3. **Deep-link matcher aggregation + a single pending-deep-link slot for the logged-out case.** Each
   feature contributes its own URL-matching patterns into one app-wide aggregated matcher (instead of
   a central registry hardcoding every feature's patterns); a link arriving before auth state has
   resolved is held as at most one pending target and landed after login, rather than dropped or
   raced against the login flow.

**Why deferred:** no deep-link feature is planned or in progress; this is a "don't reinvent it badly
the first time" note, not a bug.

**Trigger:** the moment a deep-link feature is proposed for TaigaMobileNova (push-notification-driven
navigation, universal/app links, "open this task from a link"), read this entry and the fuller
per-pattern writeups in `agentic-grappim/investigations/reference-app-scouting.md`'s HedvigInsurance
section before designing the feature from scratch.

## 47. `guardrails.yml`'s `push` trigger on `master` still diffs the wrong range after a release merge

**Where:** `.github/workflows/guardrails.yml`, "Work out the range to check" step, the `else`
branch (the `push` event path, used for pushes to `dev` and `master`).

**What:** fixed the `pull_request` path for `release/* -> master` PRs in this session (PR #379,
v2.2.0) — it now diffs `origin/dev..HEAD` instead of `origin/master..HEAD`, since master only
advances on releases and the old range re-checked weeks of already-gated dev history. The `push`
path has the identical root problem and was **not** touched: once a release PR merges,
`push: branches: [master]` fires with `github.event.before` = the previous master tip (the last
release) and `HEAD` = the new tip (the whole dev backlog just merged in). That range will re-trip
the same old, already-gated commits this session just fixed the PR-side check for.

**Consequence:** low — the merge has already happened by the time this run fires, so a failure here
doesn't block anything (unlike the PR check). It just leaves a red X on `master`'s history after
every release, which is misleading (it reads as "this release broke guardrails," but the commits it
flags predate the release entirely).

**Why deferred:** out of scope for unblocking PR #379, which only needed the `pull_request` path
fixed. Same fix shape applies (`before` should become `dev`'s tip rather than the previous
`master` tip when the pushed range looks like a release merge), but it needs its own verification —
there's no in-flight release-merge push to test against right now, and `github.event.before` isn't
easily fake-able locally the way `check-guardrails.sh <range>` is.

**Trigger:** next time a release PR merges into `master`, check whether the resulting `push`-triggered
guardrails run on `master` failed. If it did (for the reason above, not a real new violation), fix
the `else` branch the same way: when the event is a push to `master` and `before` is not an ancestor
of `dev`'s current tip reachable within the release-only commits, use `dev`'s merge-base instead.

## 51. Switching drawer sections is recorded on the back stack, so back cascades through prior sections

**Where:** `core/navigation/src/commonMain/kotlin/com/grappim/taigamobile/core/navigation/Navigator.kt`
— `goToTopLevel()` (lines 92-107) and `goBack()` (lines 35-47);
`core/navigation/src/commonMain/kotlin/com/grappim/taigamobile/core/navigation/NavigationState.kt:28-33`
(`topLevelStack: NavBackStack<NavKey>`, `currentTopLevelKey = topLevelStack.last()`).

**What:** reported by gregory (2026-09-07): navigate Epics → Issues via the drawer, then press back —
lands on Epics instead of wherever was open before Epics (or exiting the app), which reads as
unexpected. Traced to the mechanism: `topLevelStack` is a real stack, not a "currently selected
section" pointer. `goToTopLevel(key)` (`Navigator.kt:92-99`) removes any existing entry for that
section's class then `add(key)s` it — so switching sections *pushes*, it doesn't replace. `goBack()`
(`Navigator.kt:35-41`) pops `topLevelStack` whenever the current screen is itself the top of its
section's sub-stack, so a chain of drawer taps (Epics → Issues → Kanban → …) builds a stack of
sections that back walks through one at a time, most-recent-first, before ever reaching whatever was
open before the first drawer tap. `NavigationState.kt:20-21`'s own doc comment confirms this is by
design ("`topLevelStack` records which drawer section is active"), not an oversight — but it's a
different UX model than the typical drawer/bottom-nav pattern (Android's own bottom-nav guidance,
most drawer apps) where switching top-level destinations does *not* grow the back stack and back
either returns to the single previous screen or exits.

**Consequence:** every drawer navigation the user makes silently extends how many back-presses it
takes to leave the app, and the "previous section" back lands on is whichever was tapped most
recently — not necessarily the one the user thinks of as "before this."

**Why deferred:** UX behavior change to a core, deliberately-designed piece of shared navigation
infrastructure (`Navigator`/`NavigationState` back all top-level nav — drawer, rail, permanent drawer
across phone/tablet/desktop per the tablet-form-factor-support work), not something to redesign as a
side note. Needs a decision on the intended model, not just a code change.

**Fix, if wanted:** the conventional alternative is what `resetSubStackTo()` already does for
re-tapping the *active* section (`Navigator.kt:109-116`, `topLevelStack[lastIndex] = key` — replace,
not push) — applying the same replace-not-push shape to `goToTopLevel()` would make switching
sections never grow `topLevelStack` past whatever depth it already had, so back would skip past
previously-visited sections entirely and go straight to wherever the user was before entering the
drawer flow (or exit, if that was the start destination). Confirm with gregory this is the wanted
model before changing it — the current design may be intentional to let users "walk back" through
their drawer navigation history, which is also a defensible choice some apps make deliberately.

