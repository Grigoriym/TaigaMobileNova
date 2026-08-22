# Tablet and Other Form Factor Support — Checklist

**Progress:** 13/15 done (counting steps 13-15 added 2026-08-22). **Current step:** none active —
step 12 (Issues list-detail two-pane) is deferred to a separate PR, not resumed in this one; 12b's
`ListDetailSceneStrategy` wiring was reverted 2026-08-22 after 12c's verification turned up a
blocking crash and a second app-bar bug, and Issues is back to single-pane push navigation (12a's
`ResultBus` key fix was kept — harmless in single-pane mode). See IMPLEMENTATION_PLAN.md's "Step
12b/12c reverted" section for that history. Step 13 (gate the wide-width nav rail on login state)
is done — see CHECKLIST-DONE.md. Step 14 (add row dividers to the Issues list) is also done — it
turned out to be a no-op: Issues already had the same divider mechanism Dashboard uses, confirmed
by a GUI check 2026-08-22; see CHECKLIST-DONE.md. Step 15 is gated — needs a design decision from
gregory (what the desktop refresh affordance should be) before it can start.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next, followed by option 3's
Navigation 3 migration (steps 6–12, decomposed 2026-08-15 from the "Recommended path forward" in
IMPLEMENTATION_PLAN.md's "Navigation 3 migration investigation" section). Options 2 and 3's
migration (steps 2–11) are fully implemented; step 12 (the actual list-detail pane layout, option
3's real payoff) is gated — see its entries below. Done steps move to
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

Step 12 was decomposed 2026-08-21 into 12a/12b/12c (one sub-task per session, per gregory's
preference) once its blocking questions were resolved: API parity confirmed, screen pairing
decided (Issues-only MVP), and the `ResultBus` collision pre-scoped. All three all-scoping notes
below are carried over verbatim from the single step-12 entry — nothing about the underlying
decisions changed, only the step boundaries.

- ~~Whether the KMP `adaptive-navigation3` artifact (`1.3.0-beta02`) actually exports
  `ListDetailSceneStrategy` with API parity to the Android-only recipe~~ — **resolved 2026-08-21,
  confirmed yes** (decompiled the jar; full API parity, real per-target variants for Android/JVM/
  iosArm64/iosSimulatorArm64, `navigation3-ui` version compatible). See IMPLEMENTATION_PLAN.md's
  "Navigation 3 migration investigation" section for the verification detail.
- ~~Which screens get list-detail treatment~~ — **decided 2026-08-21: Issues only, as the MVP**
  (`IssuesNavDestination` + `IssueDetailsNavDestination`). See IMPLEMENTATION_PLAN.md's "Step 12
  screen-pairing decision".
- ~~Step 9's hand-rolled `ResultBus` (`core/navigation/.../ResultBus.kt`) assumes only one
  destination is ever actively composed at a time~~ — **confirmed 2026-08-21: it breaks.**
  `ListDetailSceneStrategy` keeps the list pane's `ResultEffect<UpdateDataOnBack>` continuously
  alive alongside the detail pane, so a signal sent from two levels deep in the detail column
  (e.g. Kanban[list] → Issue[detail] → UserStory → back) races between two listeners on the same
  global key instead of reaching the one it was meant for. Fix is a call-site change (per-pairing
  result keys instead of one shared marker), not a `ResultBus` rewrite. See
  IMPLEMENTATION_PLAN.md's "Step 12 pre-scoping: the `ResultBus` collision, investigated
  (2026-08-21)" — step 12a (done, see CHECKLIST-DONE.md) implements the fix it recommends.

This is option 3's actual payoff — everything in steps 6–11 is infrastructure with no user-visible
change.

## Step 12c: Emulator-verify the Issues two-pane layout on a tablet AVD

⛔ **Deferred — list-detail two-pane work for Issues moved to a separate PR, do not resume without
asking.** Started 2026-08-21; hit a reliably-reproducible crash before the scenario could be
verified, plus a second app-bar-actions bug found in the same pass (both logged below). Rather than
carry a gated, half-working two-pane feature through the rest of this initiative, 12b's
`ListDetailSceneStrategy` wiring was reverted 2026-08-22 — Issues is back to single-pane push
navigation, same as every other screen. Full reasoning and exactly what was reverted/kept:
IMPLEMENTATION_PLAN.md's "Step 12b/12c reverted — Issues back to single-pane" section. The crash
writeup itself stays open for whenever this is picked back up:
[docs/issues/2026-08-21-listdetail-scaffold-crash-on-detail-column-push.md](../../issues/2026-08-21-listdetail-scaffold-crash-on-detail-column-push.md).
Do not restart this step until gregory says otherwise.

Using the **emulator-testing** skill and this project's tablet/wide AVD (see
`docs/EMULATOR_TESTING.md`): confirm the Issues list and detail panes render side by side on a
wide screen, single-pane on a phone-width screen/AVD, and that the scenario which originally
exposed the `ResultBus` race — Kanban/Issues[list] → Issue[detail] → UserStory[pushed in the
detail column] → back — now refreshes deterministically instead of racing.

**Verify:** manual pass on the tablet AVD (or a connected tablet) plus a phone-width AVD;
screenshots of both pane arrangements attached to the step's `Note:` when archived.

**Progress so far (2026-08-21):**
- ✅ List+detail two-pane rendering confirmed on `Medium_Tablet` (list populated, empty detail pane
  before selection; list + populated detail pane after selecting an Issue) — screenshots in this
  session's scratchpad (not committed; not durable, re-capture if needed for a PR).
- ✅ Single-pane on phone width (`Medium_Phone_API_36.1`) confirmed — drawer nav, full-width list,
  full-screen push navigation.
- ❌ The actual race scenario (promote → push into detail column → back) crashes on tablet before
  "back" is reachable. Works cleanly on phone (single-pane) — confirms 12a's `ResultBus` fix still
  delivers correctly there.
- Two incidental AVD gotchas hit and documented in `docs/EMULATOR_TESTING.md`: `Medium_Tablet` can
  cold-boot with no IPv4 default route (fixed by toggling wifi off/on), and a stylus first-run
  tutorial popup intercepts the first tap after any fresh-ish boot.
- ❌ Gplay debug build crashes on startup — noticed 2026-08-21, not yet investigated (no logcat
  captured). To fix later in this PR: build+install `:androidApp:assembleGplayDebug -PgplayBuild`,
  launch, capture the crash stack via `adb logcat` before diagnosing.
- Second session on the crash (2026-08-21): four app-level mitigations tried and ruled out (switch
  top-level nav section before pushing the user story, disable the `NavDisplay` crossfade
  transition, composable/subcomposition weight of the incoming screen, `ViewModel` `init`-block async
  timing) — none change the outcome, identical crash every time. Confirmed the original theory (a
  nested per-screen `Scaffold`) was false. No matching upstream bug report found. Full details in the
  issue doc's "Further investigation (2026-08-21, session 2)" and updated "Options" sections. gregory
  is checking the navigation code directly next — still blocked, do not resume 12c without asking.
- **New bug found (2026-08-22, not yet investigated):** the top app bar's actions button is wired to
  the wrong pane's action set after a back navigation in the two-pane (tablet) layout. Repro: in the
  Issues two-pane layout, open an Issue in the detail pane (detail's "⋮" overflow actions show
  correctly), navigate to the promoted user story, then go back — the app bar now shows the list
  pane's "+" add action instead of the detail pane's "⋮" overflow actions, even though the detail
  pane (Issue) is still the one on screen. gregory believes this is likely applicable to all
  list-detail screens, not just Issues, since it looks like a `TopBarController`/pane-selection
  wiring issue rather than something Issue-specific. Not root-caused yet — needs its own
  investigation (probably via `investigate-issue`) before a fix is scoped. Separate from the
  step-12c crash above; does not block or get blocked by it.

## Step 15: Add a desktop refresh affordance for pull-to-refresh screens

⛔ **Gated — do not start without asking.** This is a design decision (what the desktop-appropriate
trigger should be), not a known fix.

Moved from `docs/revisit.md` #44 (2026-08-22, gregory) — queued as the next active work on this PR,
unrelated to the deferred step 12.

List screens (Issues among them) refresh via swipe/pull-to-refresh, a touch-only gesture. The
desktop build has no mouse/keyboard equivalent — no refresh button, no keyboard shortcut. Not yet
surveyed which screens use pull-to-refresh or whether any already special-case desktop.

**Before starting:** survey pull-to-refresh call sites (`grep -rn "PullToRefresh\|pullRefresh"`)
and bring gregory a short list of candidate desktop affordances (toolbar button, `Ctrl+R`, etc.) —
per the `adaptive` skill's guidance on pointer/keyboard input devices — for a decision, before
writing any UI code.

**Verify:** once a design is picked — manual check on desktop that the chosen affordance actually
triggers a refresh on at least Issues (and any other screen the survey found). `./gradlew jvmTest` +
`ktlintCheck` green.
