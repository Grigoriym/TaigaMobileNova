# 2026-09-11 — Posted comments don't show up (general comment logic, not mention-specific)

**Status:** Awaiting decision
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

## Findings

**Server round-trip is not the problem.** Reproduced live against the local dev Taiga instance via
`taiga-mcp`, with a plain comment (no `@mention`), confirming gregory's update above:

- `POST` (via `comments.add`, i.e. the same `PATCH /api/v1/tasks/{id}` with a `comment` field that
  `WorkItemRepository.patchData` sends) against task id 1 ("Set up authentication middleware",
  project "Main project") returned `total_comments: 1`, `version: 3` (from 2).
- The immediate follow-up `GET /api/v1/history/task/1?type=comment` (exactly what
  `HistoryApiImpl.getCommonTaskComments` calls) returned the new comment straight away — no
  eventual-consistency delay on this Taiga version.
- `CommentDTO` (`feature/workitem/dto/.../CommentDTO.kt:10-24`) field mapping
  (`id`/`user`/`comment`/`created_at`/`delete_comment_date`) matches the live response exactly; the
  extra fields the live payload carries (`type`, `key`, `diff`, `is_hidden`, etc.) are covered by
  the global `ignoreUnknownKeys = true` (`core/api/.../KmpNetworkModule.kt:42`), so they don't throw.
  `HistoryRepositoryImpl.getComments` (`feature/history/data/.../HistoryRepositoryImpl.kt:20-29`)
  sorts/filters/maps correctly against this shape.
- `WorkItemCommentsDelegateImpl.handleCreateComment`
  (`feature/workitem/ui/.../WorkItemCommentsDelegateImpl.kt:32-81`) writes the freshly-fetched
  comments into `_commentsState` on success, structurally correctly — also covered by its own
  passing unit test (`handleCreateComment should update comments on success`).

**Root cause is client-side UI state, not data.** `CommentsSectionWidget`
(`feature/workitem/ui/.../widgets/CommentsSectionWidget.kt:49-86`) wraps the entire comment list in
two nested guards:

```kotlin
if (commentsState.comments.isNotEmpty()) {           // line 49
    ...
    if (commentsState.isCommentsWidgetExpanded) {     // line 59
        commentsState.comments.forEachIndexed { ... } // the actual comment text/author/date
        if (commentsState.areCommentsLoading) { DotsLoaderWidget() }
    }
}
```

`isCommentsWidgetExpanded` defaults to `false` (`WorkItemCommentsDelegate.kt:37`,
`WorkItemCommentsState`) and nothing in the comment-creation path ever sets it to `true`. Checked
all four call sites (`TaskDetailsViewModel.kt:393-417`, `IssueDetailsViewModel.kt`,
`EpicDetailsViewModel.kt:633-657`, `UserStoryDetailsViewModel.kt`) — each `createComment()`'s
`doOnSuccess` only calls `updateVersion(...)` and (conditionally) `refreshWatchers(...)`; none call
`setIsCommentsWidgetExpanded(true)`. So:

- On a work item with **zero prior comments**, posting the first one flips the outer guard
  (line 49) to true for the first time, but the inner guard (line 59) is still `false` — the user
  sees the "Comments (1)" header appear/update, but the comment body, author, date, and even the
  in-flight loading spinner (also gated behind the same `if`) never render, with no further signal
  that anything is wrong.
- On re-navigation (fresh ViewModel per this project's Nav3 pattern — CLAUDE.md's Navigation
  Pattern section), `isCommentsWidgetExpanded` resets to its `false` default every time, so the
  section is collapsed again regardless of how many comments now exist. This matches gregory's
  report that leaving and re-entering the screen didn't help either.
- This reproduces identically for a comment with **zero** `@mentions` — confirmed by inspection,
  since `createComment()`'s success branch only ever touches `isCommentsWidgetExpanded` through
  paths that don't exist; the `containsKnownMention` check only gates the unrelated
  `refreshWatchers()` call. Matches gregory's own conclusion that the mention was incidental.

**This is an app-wide pattern, not unique to comments** — `AttachmentsSectionWidget`
(`areAttachmentsExpanded`, defaults `false`) and `CustomFieldsWidget` (`isCustomFieldsWidgetExpanded`,
defaults `false`) use the identical collapsed-by-default shape via the same `SectionTitleExpandable`
uikit component. Those two are collapsed *and stay collapsed* through their own create/update flows
too — the difference is that a new comment is the direct result of an action the user just took (typed
text, tapped send) and expects to see confirmed immediately, whereas attachments/custom fields aren't
edited through the same "type and submit, expect instant feedback" interaction.

Not GUI-verified on-device this session (a real device was connected, not the project's own AVD, and
driving an unfamiliar/possibly-in-use device wasn't judged worth the risk when the code path is
deterministic and unambiguous — no timing/race component to double-check). The state-flow reasoning
above is a straight read of non-conditional logic, not an inference from a flaky repro.

## Root cause

`CommentsSectionWidget` only renders comment content when `commentsState.isCommentsWidgetExpanded`
is `true`, that flag defaults to `false` and resets on every fresh navigation, and no code path
(across all four work-item types, mention or no mention) ever sets it to `true` after a comment is
successfully created. The comment is correctly persisted and correctly loaded into state — it's
just never displayed because the section stays collapsed. `feature/workitem/ui/.../widgets/CommentsSectionWidget.kt:49-59`,
`feature/workitem/ui/.../delegates/comments/WorkItemCommentsDelegate.kt:37`.

## Impact

Every user who posts a comment on a Task/Issue/Epic/UserStory hits this — the comment appears to
vanish (or, if the item already has comments and the section happens to be expanded in that
ViewModel instance already, it does show correctly, which is presumably why this went unnoticed
for a while: it depends on whether the section was already expanded before the new comment landed).
No data loss — the comment is on the server and reappears the moment the user manually taps the
"Comments (N)" header to expand it. No workaround is discoverable from the UI itself (nothing hints
that the header is tappable or that a comment was, in fact, posted).

## Open questions

- Should the loading spinner during comment submission also become visible (i.e. expand eagerly on
  submit, not just on success)? Affects which option below is preferred — see Options.
- None of the four `*DetailsViewModel`s were GUI-driven this session; the fix should still get a
  real on-device check per CLAUDE.md's Verification standard before being called done.

## Options

**Option A — Auto-expand on success, inside the shared delegate (recommended).** In
`WorkItemCommentsDelegateImpl.handleCreateComment`'s `onSuccess` block
(`WorkItemCommentsDelegateImpl.kt:69-74`), add `isCommentsWidgetExpanded = true` to the same
`_commentsState.update { it.copy(...) }` call that already sets the new comment list.
- Pros: one change point fixes all four work-item types identically (they all share this delegate);
  doesn't touch the collapsed-by-default behavior for viewing a work item that already has old
  comments (unaffected, consistent with Attachments/CustomFields); minimal diff, easy to test at the
  delegate level (existing `WorkItemCommentsDelegateImplTest` already covers this method).
- Cons: the in-flight loading spinner (gated behind the same `isCommentsWidgetExpanded`) still
  won't show *while* the request is in flight — only once it succeeds. If a submission is slow, the
  user still gets no feedback for that window.
- Risk/blast radius: low. Touches one class; the only behavior change is what
  `isCommentsWidgetExpanded` becomes after a successful post, which nothing else reads.

**Option B — Expand eagerly on submit (`doOnPreExecute`) in all four ViewModels.** Call
`setIsCommentsWidgetExpanded(true)` alongside each `createComment()`'s existing `clearError()` in
`doOnPreExecute`.
- Pros: also surfaces the `DotsLoaderWidget` spinner immediately on tapping send, which reads as
  better feedback than Option A for a slow network.
- Cons: four call sites instead of one (`TaskDetailsViewModel`, `IssueDetailsViewModel`,
  `EpicDetailsViewModel`, `UserStoryDetailsViewModel`); on a failed post, the section is left
  expanded even though nothing changed (harmless, but a slightly different behavior to sign off on).
- Risk/blast radius: low-medium — same core change as A, just duplicated across four files instead
  of centralized in the delegate.

**Option C — Won't fix / leave collapsed-by-default.** Treat "tap the header to see your comment"
as consistent with how Attachments/CustomFields already behave.
- Cons: doesn't address the reported symptom at all — a user who just posted a comment has no
  reason to suspect there's a collapsed section to tap, and gets zero feedback (not even a loading
  spinner) that anything happened. Not recommended given how cheap Option A is.

**Recommendation:** Option A. It's a one-place fix, keeps the change surgical (CLAUDE.md's Surgical
Changes guidance), and fully resolves the reported symptom (comment becomes visible immediately
after posting, and stays visible on re-navigation since `isCommentsWidgetExpanded` is now `true` in
the freshly-loaded... actually note: re-navigation still creates a **new** ViewModel/delegate
instance, so `isCommentsWidgetExpanded` starts at `false` again on re-entry regardless of Option
A — Option A only fixes the *immediately after posting, same screen instance* case. Re-entering the
screen from scratch will still show the section collapsed by default, same as it does today for any
work item with pre-existing comments. gregory's report doesn't clearly distinguish these two cases;
worth confirming with him whether "collapsed again after re-navigating in fresh" is considered
in-scope for this fix or acceptable (consistent with every other section in the app defaulting
collapsed on fresh load).

## Decision

Not yet made — awaiting gregory's call on Option A vs. B, and on the open question above (whether
collapsed-on-fresh-navigation is acceptable, given it matches every other section's default).
