# 2026-09-11 — Comment containing a mention doesn't show up after posting

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
introduced. Not yet confirmed either way.

Not stated: which work item type (Task/Issue/UserStory/Epic — all four share the same
`WorkItemCommentsDelegateImpl` code path), platform, or whether the mentioned username was a real
team member or not (irrelevant to this bug in principle, but worth checking since it's the
condition this PR's own fix cares about).

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
- Since the report happened right after posting a comment *with* a mention specifically, also check
  whether taiga-back does anything different for a comment containing `@username` on the
  create/read path (separate from the watcher side-effect already confirmed working) — e.g. via
  `taiga-mcp` against the local dev Taiga instance, same approach used for the original issue 414
  investigation.

## Next steps

Run the `investigate-issue` skill from a fresh session: reproduce (ideally with a plain,
mention-free comment first, to isolate whether the mention is actually a factor or just what
gregory happened to be testing when he noticed it), trace root cause with real evidence (live
Taiga round-trip via `taiga-mcp`, not just reading source), then write findings/options into this
doc before any fix.
