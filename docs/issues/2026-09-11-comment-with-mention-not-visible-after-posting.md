# 2026-09-11 — Posted comments don't show up (general comment logic, not mention-specific)

**Status:** Reported — investigation not started. Pick up with the `investigate-issue` skill next
session, in this same PR/branch (`docs/issue-414-mention-investigation`), before closing it out.
**Link:** reported by gregory in conversation (no GitHub issue)   **Updated:** 2026-09-11

## Report

gregory (2026-09-11), right after `containsKnownMention`/watchers-refresh landed
(commit `21ddad74`, this PR): posted a comment that mentions a user. The mentioned user was
correctly added to the watchers list (the fix from this PR working as intended). **The comment
itself was not visible afterward** — not immediately, and not after leaving the work-item details
screen and navigating back into it (which tears down and recreates the ViewModel, per this
project's Nav3 pattern — see CLAUDE.md's Navigation Pattern section — so this should be a fresh
`loadTask()` fetch, not a stale client cache).

gregory's own hypothesis: "I have a feeling it was there before our changes" — i.e. likely a
pre-existing bug in comment posting/loading, not something this PR's `containsKnownMention` change
introduced.

**Update (2026-09-11, same report, gregory):** "I think the issue is not about mention, but a
general comment logic, the mention just helped me to notice that with the user being added to
watchers." So the mention is almost certainly incidental — it's what drew attention to the missing
comment (via the now-correctly-added watcher), not a contributing cause. Treat this as a
**general comment-visibility bug**, reproducible with a plain comment with no `@` in it at all;
don't spend investigation time on anything mention-specific unless a plain-comment repro fails to
reproduce it.

Not stated: which work item type (Task/Issue/UserStory/Epic — all four share the same
`WorkItemCommentsDelegateImpl` code path) or platform.

## Starting points for the investigation (not verified, just pointers)

- `WorkItemCommentsDelegateImpl.handleCreateComment`
  (`feature/workitem/ui/src/commonMain/.../delegates/comments/WorkItemCommentsDelegateImpl.kt:32-81`)
  patches the comment (`workItemRepository.patchData`), then immediately re-fetches via
  `historyRepository.getComments(...)` and writes the result into `_commentsState`. Structurally
  this looks correct — the question is whether the *server* actually has the new comment by the
  time that immediate re-fetch happens, or whether something in the fetch/map/filter path drops it.
- `HistoryRepositoryImpl.getComments`
  (`feature/history/data/src/commonMain/.../HistoryRepositoryImpl.kt:20-29`) sorts by
  `postDateTime`, filters `deleteDate == null`, then maps each DTO through `CommentsMapper.toDomain`
  — any of the three steps could silently exclude a comment that exists server-side but doesn't
  match what the mapper/filter expects.
- Reload-on-reentry path: `loadTask()` in the four *DetailsViewModel.kt files calls
  `setInitialComments(result.comments)` from the same `taskDetailsDataUseCase`/equivalent load — if
  the comment is still missing after a full re-navigation, the fresh load is getting the same
  (wrong) answer, which points at the server response or the mapper rather than anything
  comment-creation-specific.
- gregory has since clarified this is general comment logic, not mention-specific (see Update
  above) — deprioritize checking whether taiga-back treats `@username` comments differently on the
  create/read path; only worth a look if a plain-comment repro doesn't reproduce the bug.

## Next steps

Run the `investigate-issue` skill from a fresh session: reproduce with a **plain comment, no
mention**, against the local dev Taiga instance (`taiga-mcp`) to confirm this is general comment
logic as gregory suspects, trace root cause with real evidence (a live Taiga round-trip, not just
reading source), then write findings/options into this doc before any fix.
