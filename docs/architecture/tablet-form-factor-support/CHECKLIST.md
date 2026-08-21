# Tablet and Other Form Factor Support — Checklist

**Progress:** 11/12 done. **Current step:** 12 — add `ListDetailSceneStrategy` (gated, not
started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next, followed by option 3's
Navigation 3 migration (steps 6–12, decomposed 2026-08-15 from the "Recommended path forward" in
IMPLEMENTATION_PLAN.md's "Navigation 3 migration investigation" section). Options 2 and 3's
migration (steps 2–11) are fully implemented; step 12 (the actual list-detail pane layout, option
3's real payoff) is gated — see its entry below. Done steps move to
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 12: Add `ListDetailSceneStrategy` for list-detail two-pane layouts

⛔ **Gated — do not start without asking.** Depends on steps 6–11 (full Nav3 migration) being
done, and on open questions from IMPLEMENTATION_PLAN.md's "Navigation 3 migration
investigation" section:

- ~~Whether the KMP `adaptive-navigation3` artifact (`1.3.0-beta02`) actually exports
  `ListDetailSceneStrategy` with API parity to the Android-only recipe~~ — **resolved 2026-08-21,
  confirmed yes** (decompiled the jar; full API parity, real per-target variants for Android/JVM/
  iosArm64/iosSimulatorArm64, `navigation3-ui` version compatible). See IMPLEMENTATION_PLAN.md's
  "Navigation 3 migration investigation" section for the verification detail.
- Which screens get list-detail treatment — Kanban/Sprint board + task detail is the obvious
  candidate but not decided; this is a design decision (see step 3's precedent), not an
  engineering one.
- ~~Step 9's hand-rolled `ResultBus` (`core/navigation/.../ResultBus.kt`) assumes only one
  destination is ever actively composed at a time~~ — **confirmed 2026-08-21: it breaks.**
  `ListDetailSceneStrategy` keeps the list pane's `ResultEffect<UpdateDataOnBack>` continuously
  alive alongside the detail pane, so a signal sent from two levels deep in the detail column
  (e.g. Kanban[list] → Issue[detail] → UserStory → back) races between two listeners on the same
  global key instead of reaching the one it was meant for. Fix is a call-site change (per-pairing
  result keys instead of one shared marker), not a `ResultBus` rewrite — not yet implemented. See
  IMPLEMENTATION_PLAN.md's "Step 12 pre-scoping: the `ResultBus` collision, investigated
  (2026-08-21)".

This is option 3's actual payoff — everything in steps 6–11 is infrastructure with no user-visible
change.

**Verify:** TBD once scoped — will need both `jvmTest`/`ktlintCheck` and emulator verification on
a tablet/wide-desktop AVD, per the pattern of every UI-visible step above.
