# 2026-09-11 — Wiki page descriptions don't render `@mentions` as clickable

**Status:** Awaiting decision
**Link:** reported by gregory in conversation (no GitHub issue)   **Updated:** 2026-09-11

## Report

gregory (2026-09-11): noticed that users are clickable (as `@mention` links) inside Task/Issue/
Epic/UserStory descriptions and comments, but presumed this isn't true for a wiki page's
description/content. Not stated: exact repro steps or platform — this was an observation made
while using the app, not a filed bug with steps.

## Findings

**Mention rendering is opt-in per call site, gated on a non-empty `members` list.**
`MarkdownTextWidget`'s `rewriteMentions` (`uikit/src/commonMain/.../widgets/text/MarkdownTextWidget.kt:88-99`):

```kotlin
internal fun rewriteMentions(text: String, members: ImmutableList<TeamMember>): String {
    if (members.isEmpty()) return text
    ...
}
```

returns the text completely unchanged — no `@username` rewritten into a `mention:`-scheme link —
whenever `members` is empty. `members` defaults to `persistentListOf()` on every layer that passes
it down (`MarkdownTextWidget.kt:33`, `ExpandableMarkdownText.kt:36`, and
`WorkItemDescriptionWidget.kt:33`), so a caller that never explicitly supplies a populated list
silently gets plain, non-clickable text with no error or visual difference — nothing hints that a
member list was expected.

**Work-item descriptions supply that list; the wiki page description never does.**
`TaskDetailsScreen.kt:354` (and the equivalent in `IssueDetailsScreen.kt`, `EpicDetailsScreen.kt`,
`UserStoryDetailsScreen.kt`) calls `WorkItemDescriptionWidget(..., onMentionClick = goToProfile)`
with a `members` list sourced from `WorkItemMentionsDelegate.mentionsState.value.members`, itself
populated by `WorkItemMentionsDelegateImpl.loadMembers()`
(`feature/workitem/ui/.../delegates/mentions/WorkItemMentionsDelegateImpl.kt:18-26`) calling
`UsersRepository.getTeamMembers()`. Each of the four `*DetailsViewModel`s mixes in
`WorkItemMentionsDelegate by WorkItemMentionsDelegateImpl(usersRepository)` (confirmed for
`TaskDetailsViewModel.kt:161`) and calls `loadMembers()` during load.

`WikiPageScreen`'s call to the same widget
(`feature/wiki/ui/.../page/details/WikiPageScreen.kt:160-171`) passes only `description`,
`onDescriptionClick`, `descriptionState`, `canModify`, `isOffline` — no `members`, no
`onMentionClick`. `WikiPageViewModel`
(`feature/wiki/ui/.../page/details/WikiPageViewModel.kt:34-51`) mixes in
`WorkItemAttachmentsDelegate` and `WorkItemDescriptionDelegate` but not `WorkItemMentionsDelegate`
— grepping `MentionsDelegate|TeamMembers|mentionsState|members` across
`feature/wiki/ui/.../page/details/` returns zero hits. There is no code path in the wiki feature
that ever fetches team members or would have anything to pass even if the call site were updated.

**Comments are not in scope — wiki pages don't have a comments feature at all.**
`WorkItemCommentsDelegate`/`CommentsSectionWidget` are only mixed into the four work-item
ViewModels/Screens; `WikiPageViewModel` doesn't implement `WorkItemCommentsDelegate`. So "unlike
... comments" in the report is accurate only insofar as work-item comments do support mentions —
there's no wiki-comments code path to compare against.

**Wiki page *creation* is unaffected/out of scope.** `WikiCreatePageScreen.kt` renders its content
as a plain editable `HintTextField` (`content_hint`, line 113-118), not through
`MarkdownTextWidget`/`WorkItemDescriptionWidget` — there's no markdown rendering there to begin
with, so no mention-link behavior to compare.

**The fix shape mirrors the four work-item ViewModels exactly**, since `UsersRepository
.getTeamMembers()` (`feature/users/domain/.../UsersRepository.kt:7`) resolves the current
project's team internally — it takes no project-id parameter — and `goToProfile` is already
wired end-to-end in the wiki nav graph (`WikiPageScreen.kt:56` param, passed as
`onUserItemClick = goToProfile` at `WikiPageScreen.kt:127`, itself already used for the page's
author `UserItem` click at `WikiPageScreen.kt:187-188`). Nothing about wiki's project/session
context is different from how work items already do this.

Not GUI-verified on-device this session — the code path is unambiguous (an empty default list
that the wiki call site never overrides, confirmed by grep across the whole feature module), so
this wasn't judged to need a live check before writing up, but the eventual fix should still get
one per CLAUDE.md's Verification standard.

## Root cause

`WikiPageScreen` never wires a populated `members` list (or `onMentionClick`) into the
`WorkItemDescriptionWidget` call it shares with the four work-item detail screens, and
`WikiPageViewModel` has no `WorkItemMentionsDelegate` (or any team-members fetch) to source one
from. `MarkdownTextWidget.rewriteMentions` silently no-ops on an empty list, so `@username` text in
a wiki page's content renders as plain text instead of a clickable mention link.
`uikit/src/commonMain/.../widgets/text/MarkdownTextWidget.kt:88-99`,
`feature/wiki/ui/.../page/details/WikiPageScreen.kt:160-171`,
`feature/wiki/ui/.../page/details/WikiPageViewModel.kt:34-51`.

## Impact

Cosmetic/UX-consistency gap, not a data or correctness bug — the `@username` text itself is
preserved and readable, it just isn't a tappable link to that user's profile the way it is
everywhere else `@mentions` appear (task/issue/epic/userstory descriptions and comments). Affects
every wiki page whose content contains an `@mention`. No workaround from the UI (nothing marks the
text as "should be a link").

## Open questions

- Should mentions also become clickable in `WikiCreatePageScreen`'s content field? That screen is
  a plain-text editor with no markdown preview at all today, so "fix mentions there" would mean
  adding a preview/rendering mode that doesn't currently exist — a materially bigger change than
  wiring the existing widget correctly in the read view. Treated as out of scope unless gregory
  wants it folded in.
- Not GUI-verified on-device (see Findings) — worth a real on-device check once implemented, not
  just a code read, per CLAUDE.md's Verification standard.

## Options

**Option A — Wire `WorkItemMentionsDelegate` into `WikiPageViewModel`, mirroring the four
work-item ViewModels (recommended).** Add `usersRepository: UsersRepository` to the constructor,
mix in `WorkItemMentionsDelegate by WorkItemMentionsDelegateImpl(usersRepository)`, call
`loadMembers()` in `loadData()`, and update `WikiPageScreen`'s `WorkItemDescriptionWidget` call
site to pass `members = mentionsState.members` and `onMentionClick = goToProfile`.
- Pros: identical shape to the existing, already-tested pattern in all four work-item ViewModels;
  `goToProfile` navigation is already wired end-to-end; small, mechanical diff.
- Cons: one more network call (`getTeamMembers()`) on every wiki page open, even for pages with no
  mentions in their content — the four work-item screens already pay this cost, so it's consistent
  with existing behavior, not a new pattern.
- Risk/blast radius: low. Touches `WikiPageViewModel.kt` and `WikiPageScreen.kt` only; no shared
  widget or delegate code changes.

**Option B — Won't fix / leave as-is.** Treat plain-text `@mentions` in wiki content as acceptable
since wiki pages are a different content type (free-form documentation) than task/issue
descriptions.
- Cons: inconsistent with every other place `@mentions` appear in the app; doesn't address what
  gregory noticed. Not recommended — Option A is a small, well-precedented change.

**Recommendation:** Option A. It reuses an existing, already-tested delegate with no new
abstraction, and resolves the inconsistency gregory noticed.

## Decision

Not yet made — gregory wants this implemented in a separate session, not this one.
