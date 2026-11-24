# Tablet and Other Form Factor Support — Checklist

**Progress:** 16/17 done. **Current step:** 17, fixing the two bugs agent-qa found on PR #500
before it merges. Steps 12c and 16 landed together on 2026-11-24.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and how
the initiative went: adaptive navigation chrome (steps 2–4), the Navigation 3 migration (steps
6–11), and the list-detail payoff (step 12). Every step, with its notes, is in
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

A new tablet/large-screen step goes here, following CLAUDE.md's Multi-Session Work rules.

## Step 17: Fix the two agent-qa bugs on PR #500 (branch `feat/tablet-rail-and-list-detail`)

Both bugs, their root causes and the fix are in the "Agent-qa trial 3" section of
`docs/issues/2026-11-23-tablet-nav-rail-and-list-detail.md`.
1. Phone in landscape: no two-pane, and the ≡ comes back (`maxHorizontalPartitions = 1` for compact
   height).
2. Tablet: the keyboard pops up when two-pane appears (`shouldAutoFocusCurrentDestination`).
   **Ask gregory (a)/(b)/(c) first if the issue doc doesn't record his answer.**

**Verify:** rebuild, then:
- `Pixel_10a` in landscape: Issues has the ≡ and is single-pane.
- `Pixel_Tablet`: rotating into landscape with an issue open doesn't open the keyboard, and
  neither does tapping New issue's back arrow.
- Re-run the trial scenarios with `agentic-grappim/agent-qa/trials/3-taigamobilenova/run-exec.sh`.
- Full `jvmTest` + ktlint + detekt.
- Update PR #500's body.

