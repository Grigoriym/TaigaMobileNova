# Tablet and Other Form Factor Support — Checklist

**Progress:** 11/12 done (12a and 12b of step 12's three sub-steps also done). **Current step:**
12c — emulator-verify the Issues two-pane layout on a tablet AVD (gated, not started).

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

⛔ **Gated — do not start without asking.** Depends on 12b being done.

Using the **emulator-testing** skill and this project's tablet/wide AVD (see
`docs/EMULATOR_TESTING.md`): confirm the Issues list and detail panes render side by side on a
wide screen, single-pane on a phone-width screen/AVD, and that the scenario which originally
exposed the `ResultBus` race — Kanban/Issues[list] → Issue[detail] → UserStory[pushed in the
detail column] → back — now refreshes deterministically instead of racing.

**Verify:** manual pass on the tablet AVD (or a connected tablet) plus a phone-width AVD;
screenshots of both pane arrangements attached to the step's `Note:` when archived.
