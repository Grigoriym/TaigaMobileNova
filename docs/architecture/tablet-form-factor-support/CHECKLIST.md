# Tablet and Other Form Factor Support — Checklist

**Progress:** 4/4 done. **Current step:** 5.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next. Option 2 (steps 2–4) is
now fully implemented. gregory confirmed 2026-08-15 that option 3 (list-detail two-pane) starts
now, beginning with the Navigation 3 migration investigation it's blocked on — step 5. Done steps
move to [CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 5: Investigate the Navigation 3 migration path, informed by wallosmobile

Design/investigation-only — no code changes. Research `wallosmobile`
(`~/proj/grappim/wallosmobile/`, a sibling project on the same architecture already built on
Navigation 3) and the `android-skills:navigation-3` skill, then size what migrating this repo's
`MainNavHost.kt` / `@Serializable` route-destination pattern to Nav3 would actually take —
module by module, not Nav3 in the abstract — and how it interacts with the
`NavigationSuiteScaffold` chrome steps 2–4 just shipped. Write the findings as a new section in
IMPLEMENTATION_PLAN.md, including a recommended migration approach (all-at-once vs. incremental)
and a proposed breakdown into future CHECKLIST.md steps — proposed only; adding those steps is
its own future decomposition commit, not part of this one.

**Verify:** IMPLEMENTATION_PLAN.md records the findings, the recommended approach, and the
proposed next steps — done. No build/test verification applies to a design-only step.
