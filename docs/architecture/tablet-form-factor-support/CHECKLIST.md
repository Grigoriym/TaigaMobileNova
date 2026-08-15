# Tablet and Other Form Factor Support — Checklist

**Progress:** 9/12 done. **Current step:** 10 — replace `NavHost`/`composable<T>` with
`NavDisplay`/`entry<T>` (gated, not started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next, followed by option 3's
Navigation 3 migration (steps 6–12, decomposed 2026-08-15 from the "Recommended path forward" in
IMPLEMENTATION_PLAN.md's "Navigation 3 migration investigation" section). Option 2 (steps 2–4) is
fully implemented. Steps 6–11 are the migration itself (mechanical, no open decisions blocking
them); step 12 (the actual list-detail pane layout, option 3's real payoff) is gated — see its
entry below. Done steps move to [CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 10: Replace `NavHost`/`composable<T>` with `NavDisplay`/`entry<T>` — the cutover

⛔ **Gated — do not start without asking.** Depends on steps 6–9 all being done first, and this is
the "big bang" step where everything lands together (the migration guide has no supported
Nav2/Nav3 coexistence path) — confirm with gregory before starting given the blast radius.

Replace `NavHost`/`composable<T>`/`NavGraphBuilder` extensions with
`NavDisplay`/`entry<T>`/`EntryProviderScope<NavKey>` extensions across all 8 nav-graph files plus
`MainNavHost.kt`. Delete the Nav2 dependencies once nothing references them.

**Verify:** `./gradlew jvmTest`, `ktlintCheck`, all four target compiles, then full emulator
verification across every top-level section (not a sample) per CLAUDE.md's Verification rule —
this step touches navigation for the entire app.

## Step 11: Rewire adaptive-chrome selection state to `NavigationState`

Update `TaigaDrawerWidget.kt`/`MainScreen.kt`'s `currentTopLevelDestination` to read from
`navigationState.currentTopLevelKey` instead of the Nav2 back stack. Re-run step 4's emulator
scenarios (compact modal drawer selection highlighting, medium/expanded rail selection
highlighting) to confirm option 2's work still behaves correctly after the Nav3 cutover.

**Verify:** emulator-testing skill, same two AVDs and scenarios as step 4's `Verify:` entry in
CHECKLIST-DONE.md.

## Step 12: Add `ListDetailSceneStrategy` for list-detail two-pane layouts

⛔ **Gated — do not start without asking.** Depends on steps 6–11 (full Nav3 migration) being
done, and on two open questions from IMPLEMENTATION_PLAN.md's "Navigation 3 migration
investigation" section that gregory hasn't answered yet:

- Whether the KMP `adaptive-navigation3` artifact (`1.3.0-beta02`) actually exports
  `ListDetailSceneStrategy` with API parity to the Android-only recipe — unconfirmed, worth a short
  spike before scoping this step in detail.
- Which screens get list-detail treatment — Kanban/Sprint board + task detail is the obvious
  candidate but not decided; this is a design decision (see step 3's precedent), not an
  engineering one.
- Step 9's hand-rolled `ResultBus` (`core/navigation/.../ResultBus.kt`) assumes only one
  destination is ever actively composed at a time — true under Nav2's `NavHost`, but list-detail
  two-pane is exactly the shape that keeps two entries composed simultaneously. Re-check whether
  the shared `UpdateDataOnBack` signal still lands on the right pane before relying on it here; see
  IMPLEMENTATION_PLAN.md's "Step 9 notes".

This is option 3's actual payoff — everything in steps 6–11 is infrastructure with no user-visible
change.

**Verify:** TBD once scoped — will need both `jvmTest`/`ktlintCheck` and emulator verification on
a tablet/wide-desktop AVD, per the pattern of every UI-visible step above.
