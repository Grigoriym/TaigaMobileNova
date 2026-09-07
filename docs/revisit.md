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
| 52 | No way for a user to send debug logs when filing a bug report | M–L | this file |
| 53 | Broader excessive-requests audit: possible filter-driven double list-fetch, no search debounce anywhere | S–M | this file |

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

## 52. No way for a user to send debug logs when filing a bug report

**What:** raised by gregory (2026-09-07), inspired by Symfonium's pattern — a "debug mode" toggle
that a user can turn on, reproduce a bug, then send the resulting log file to the developer for
investigation. TaigaMobileNova has no equivalent today. Open questions gregory raised: how the log
gets from the user to the developer (email? pasted into a GitHub issue?), and how that interacts with
the privacy policy.

**Current logging state (`core/logger`), confirmed by reading each backend:**
- **Desktop/JVM** — already has almost the whole mechanism except the "send" step:
  `FileLogger` (`core/logger/src/jvmMain/.../FileLogger.kt`) writes every `logcat()` call to
  `taigamobile.log` in the per-user app-data dir, rotating to `<name>.old` past 5 MB
  (`MAX_LOG_FILE_BYTES`, line 8) — always on, not gated behind a debug-mode toggle. A "reveal in file
  manager" / "copy path" Settings action would need very little new code.
- **Android** — `TimberLogger` (`core/logger/src/androidMain/.../TimberLogger.kt`) uses a `DebugTree`
  (debug builds only — Logcat, ephemeral, not exportable from a release build a real user would run)
  and, Gplay only, a `CrashlyticsTree` that forwards `ERROR`-priority `logcat()` calls with throwables
  to Firebase automatically on crash — not a full session log, not user-triggered, and not present on
  F-Droid at all. **No persistent, user-exportable log file exists on Android today.**
- **iOS** — `NSLogLogger` (`core/logger/src/iosMain/.../NSLogLogger.kt`) writes to `NSLog` only, no
  persistence at all.

**Privacy policy precedent already exists to extend, not invent from scratch:** `PRIVACY_POLICY.md`
(F-Droid/base) and `PRIVACY_POLICY_GPLAY.md` (adds a Crashlytics section) already disclose what's
collected, name an opt-out path by its exact Settings menu location, and are tracked in
`docs/security/masvs.md`'s MASVS-PRIVACY-3 row (see that row and its confirmation note for the
disclosure shape a debug-log feature would need to match — what's collected, exclusions like
credentials/tokens/project content, and the exact in-app path to trigger/disable it).

**Why deferred:** a real feature investigation, not a bug — spans three platforms with three
different starting points (Desktop nearly there, Android has no persistent log at all, iOS has
nothing), a privacy-policy amendment on both `PRIVACY_POLICY.md` and `PRIVACY_POLICY_GPLAY.md`, and a
`docs/security/masvs.md` register update once shipped. Not something to scope inline here.

**Questions a real investigation needs to answer** (not decided — options only):
- **Collection scope:** always-on rotating file (like Desktop today) vs. an explicit "debug mode"
  toggle a user enables only while reproducing a bug (Symfonium's model — smaller privacy footprint,
  matches what gregory described).
- **What's in the log:** `logcat()` calls already exclude secrets by convention (see CLAUDE.md's Error
  Handling section on `ExceptionSanitization.kt`), but a full-session export is a broader surface than
  today's ERROR-only Crashlytics forwarding — needs its own audit before shipping, same shape as the
  MASVS-PRIVACY-3 Crashlytics review.
- **Get-it-out-of-the-app mechanism:** Android/iOS have native share sheets
  (`Intent.ACTION_SEND`/`UIActivityViewController`) that can hand a file to whatever app the user
  picks (email, GitHub's own app, Files/saved-to-clipboard, etc.) without the app choosing a
  destination or embedding any credential — this avoids the "how does it get to me" question being
  the app's problem at all. Desktop's answer is likely just "reveal file location" and let the user
  attach it manually. **Do not** have the app itself post to GitHub or email anything automatically —
  that would need an embedded credential (GitHub token / SMTP creds), which is its own security
  problem this project has deliberately avoided elsewhere (see Settled Decisions).
- **Where the user is told to send it:** almost certainly "attach the exported file to the GitHub
  issue" (this repo's actual bug-report channel) rather than email, once the share-sheet approach
  above makes GitHub's own app/web upload a normal share-sheet target — worth confirming against
  gregory's actual issue-triage workflow before committing to that framing in the UI copy.

**Trigger:** pick this up as its own multi-session initiative (per CLAUDE.md's Multi-Session Work
section — `docs/architecture/debug-logging/` with `CHECKLIST.md` + `IMPLEMENTATION_PLAN.md`) once
gregory wants to prioritize it; the platform-parity gap above (Android/iOS have no persistent log at
all) is probably the first real design decision, before UI or privacy-policy wording.

---

## 53. Broader excessive-requests audit: possible filter-driven double list-fetch, no search debounce anywhere

**What:** raised by gregory (2026-09-07) right after commit `0c34a570` fixed Kanban firing
`filters_data` twice on load (resolving #49/#50) — the underlying problem (the app making more
Taiga API calls than it needs to) is probably not unique to Kanban. This entry is a lead list from
a quick grep-based survey, not a diagnosis — nothing here should be fixed without first confirming
it with real network traffic (see "How to confirm" below).

**Lead 1 — filter-load may double-fetch the paginated list on `IssuesViewModel`/
`EpicsViewModel`/`ScrumBacklogViewModel`, but only when the user already has saved filters:**
- `feature/issues/ui/.../list/IssuesViewModel.kt:53-69`,
  `feature/epics/ui/.../list/EpicsViewModel.kt:55-75`,
  `feature/scrum/ui/.../backlog/ScrumBacklogViewModel.kt:53-71` — each exposes its paginated list as
  `combine(session.xFilters, searchQuery).flatMapLatest { ... xRepository.getXPaging(...) }`, and
  each `init` also calls `loadFiltersData()`, whose `onSuccess` calls
  `session.changeXFilters(_state.value.activeFilters.updateData(result))` — writing to the exact
  `StateFlow` the paging `combine` watches.
- Traced `FiltersStorageImpl` (`core/storage/.../FiltersStorageImpl.kt`): `xFilters` is
  `dataStore.data.map { ... }.stateIn(scope, Eagerly, FiltersData())`, and `FiltersData.updateData`
  (`feature/filters/domain/.../FiltersData.kt`) maps over `this` (the *current* `activeFilters`),
  so on a **fresh session with no filters selected**, `activeFilters` is the all-empty default and
  `updateData` produces another structurally-equal all-empty `FiltersData` — StateFlow's built-in
  conflation (drops consecutive `equals()`-equal values) means this write likely does **not**
  re-emit, so no second list fetch happens in the common cold-start case. But once a user has
  previously selected any filter (assignee, status, tag…), `activeFilters` is non-empty at init,
  and `updateData` refreshes each selected item's `name`/`color`/`count` from the freshly-fetched
  server data — genuinely changing the value in the common case (counts drift as project data
  changes) — which *would* re-trigger `combine` → `flatMapLatest` → a second call to the paginated
  list endpoint. **Not yet confirmed** — this is inference from reading the conflation contract, not
  an observed trace.
- A background survey agent checked whether `loadFiltersData()` itself is called more than once per
  screen (it isn't, in Issues/Epics/Scrum — each calls it exactly once) and whether
  `DashboardViewModel.loadAll()`'s 4 parallel `init` launches overlap endpoints (they don't — each
  hits a distinct use case). Neither of those is a lead; the StateFlow-driven re-fetch above is the
  one worth tracing.

**Lead 2 — no debounce anywhere in the codebase** (`grep -rn debounce --include=*.kt` — zero hits).
`IssuesViewModel`, `EpicsViewModel`, `ScrumBacklogViewModel`, and
`feature/projectselector/ui/.../ProjectSelectorViewModel.kt:45` all wire `searchQuery` straight into
a `combine(...).flatMapLatest` that drives paging — every keystroke starts a new request and
`flatMapLatest` cancels whatever was still in flight, so a fast typist likely initiates one HTTP
request per keystroke (mostly aborted, not completed) rather than one per pause. Adding
`.debounce(300)` (or similar) before these `flatMapLatest` calls is the standard fix, once this is
confirmed worth doing.

**Resource for verifying any of this: the local `taiga-mcp` server**
(`/home/gregory/proj/grappim/taiga-mcp/`, gregory's own MCP server — `mcp__taiga-mcp__taiga_request`
tool) talks to the same local Taiga instance the app is configured against
(`http://localhost:9000/` per this session's memory). It's a fast way to independently issue the
same raw requests a screen *should* need and compare against what the app's Ktor client actually
sends, without setting up a full proxy — useful as a sanity check once real request logs are
captured, not a replacement for them.

**How to actually confirm any of this (not done here):** capture real per-screen request counts —
either enable Ktor call logging into the desktop `FileLogger` output (`core/logger`, see CLAUDE.md's
Logging section) or run a local proxy — for Issues, Epics, and Scrum Backlog cold loads (with and
without a previously-saved filter selection), and for typing in each screen's search box. Confirm an
actual duplicate before changing any production code.

**Trigger:** pick up as a standalone investigation (`investigate-issue` skill) once gregory wants to
prioritize it.

