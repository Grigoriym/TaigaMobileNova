# @-Mention Tagging Support — Checklist

**Progress:** 0/6 done. **Current step:** 1 (mention-link rendering).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice. Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).

Steps 1-3 are independent building blocks (rendering, detection logic, popup
component) with no ordering dependency between them, but are listed in the order
that gives the fastest user-visible win first. Steps 4-5 depend on 2 and 3. Step 6
depends on everything before it.

## Step 1: Render `@username` as a tappable mention link

Add mention-link rewriting to `MarkdownTextWidget`/`ExpandableMarkdownText`
(`uikit/.../widgets/text/`): a new optional `members: ImmutableList<TeamMember> =
persistentListOf()` and `onMentionClick: (Long) -> Unit = {}` param. Before handing
text to `Markdown(...)`, rewrite any `@username` substring matching a real entry in
`members` into `[@username](mention:<id>)`; supply a custom
`linkInteractionListener` (via `annotatorSettings`) that intercepts the `mention:`
scheme and calls `onMentionClick` instead of opening a URL. Wire the two real call
sites — `feature/workitem/ui/.../widgets/WorkItemDescriptionWidget.kt` and
`.../widgets/CommentsSectionWidget.kt` — to pass the current project's
`getTeamMembersByProjectId(...)` result and `onMentionClick = {
navigator.navigateToProfileScreen(it) }`. Leave `CustomFieldsWidget.kt`'s call alone
(default empty `members`, out of scope per the plan).

**Verify:** a new `uikit` `commonTest` for the rewrite function (given text + a
member list, assert the exact rewritten markdown string, including the "no match for
this username" pass-through case) — write it failing first, then make it pass. Then
GUI-verify on the emulator (per CLAUDE.md's Verification rule): open a work item with
an existing `@realusername` in its description or a comment, confirm it renders
styled/tappable and navigates to that user's `ProfileScreen`.

## Step 2: Mention-query detection + insertion utility

Add `findActiveMentionQuery(value: TextFieldValue): MentionQuery?` and
`insertMention(value: TextFieldValue, query: MentionQuery, username: String):
TextFieldValue` to `uikit` (pure functions, no Compose UI dependency beyond
`TextFieldValue`). Query pattern matches the server's `\B(@)([\w.-]+)\b`
(`taiga-back`'s `mentions.py:48`) so what the popup filters on is always a shape the
server will actually tag.

**Verify:** `commonTest` cases covering: cursor mid-word after `@` (query found),
cursor after a completed mention with trailing space (no active query), `@` preceded
by a word character (not a mention trigger — `\B` boundary), empty query (bare `@`),
and `insertMention` producing the correct spliced text + cursor position for each.
Write failing first.

## Step 3: Mention suggestion popup component

**Before starting:** consult the **uikit-guide** subagent for any existing
dropdown/anchored-popup primitive in `uikit` (e.g. whatever backs
`DropdownSelector`) — reuse or thinly wrap it rather than building new
positioning/anchoring logic if one already fits.

Add a new `uikit` composable (suggested name: `MentionSuggestionsPopup`) taking a
filtered `ImmutableList<TeamMember>` and `onSelect: (TeamMember) -> Unit`, rendering
an anchored list of member rows (avatar, username, name) below/above the text field.

**Verify:** a Compose UI test (or screenshot test, matching whatever pattern the
**testing** subagent recommends for a new uikit widget) confirming the popup renders
the given members and `onSelect` fires with the tapped one. No live network — pass a
fixed fake list.

## Step 4: Wire autocomplete into `CreateCommentBar`

Convert `CreateCommentBar`'s local comment state from `String` to `TextFieldValue`.
On every edit, run Step 2's `findActiveMentionQuery`; when non-null, filter the
`members` param (new, sourced the same way as Step 1) by prefix and show Step 3's
popup; on selection, apply `insertMention` and hide the popup. Update the 4 call
sites (`UserStoryDetailsScreen`, `TaskDetailsScreen`, `EpicDetailsScreen`,
`IssueDetailsScreen`) to pass `members` — check each screen for an existing
team-members fetch (e.g. behind the assignee widget) to reuse before adding a new
one. Confirm the popup is simply absent while `isOffline` (field already disables
under that condition — no separate offline branch expected, confirm during this
step rather than assume).

**Verify:** update/extend `CreateCommentBarTest.kt` (`uikit/src/jvmTest/`) — typing
`@` shows filtered suggestions, selecting one inserts `@username `, and the final
submitted comment string is exactly what one existing test already asserts today
(no regression to the plain-text path when no `@` is typed). Then GUI-verify on the
emulator: type `@`, see the popup, pick a member, send the comment, confirm it
renders as a link per Step 1.

## Step 5: Wire autocomplete into the description editor

Same mechanism as Step 4, applied to
`WorkItemEditDescriptionScreen.kt`'s `BasicTextField`/`EditDescriptionState.kt`/
`EditDescriptionViewModel.kt`. This screen is shared by `WikiPageScreen` via the
`WorkItemDescriptionState`/delegate pattern (CLAUDE.md's Navigation Pattern section)
— confirm wiki pages get the popup too as a side effect, not a separate
implementation, and that a wiki page's "members" source (project team, same as any
other entity) is available at this call site.

**Verify:** extend `EditDescriptionViewModelTest.kt` the same way as Step 4's
comment-bar test. GUI-verify on the emulator for both a work-item description and a
wiki page description (per CLAUDE.md's note that description input is shared code,
but the two screens are reached through different navigation paths — worth a real
click-through on each once, not just one and an assumption about the other).

## Step 6: Full-suite verification and polish

Run `./gradlew jvmTest` (full suite, not just the touched modules — per CLAUDE.md,
a coroutine leak in one module can fail an unrelated test), `ktlintCheck`,
`koverXmlReport` + `:koverVerify` (this initiative adds real production code paths —
confirm the floor still holds without needing to raise it; if it doesn't, add tests
rather than lower the floor). Re-run the Step 1/4/5 GUI checks once more after
normal in-between app usage (per CLAUDE.md's Verification rule about behavior that
depends on prior interaction — don't rely solely on the first-load checks already
done in earlier steps). Confirm ktlint's `standard:function-signature`/
`standard:class-signature` traps (CLAUDE.md Testing section) didn't bite any of the
new multi-param composables added across steps 1-5.

**Verify:** all of the above commands green; a manual emulator pass exercising both
comment and description mentions after navigating through at least one unrelated
screen first, not immediately after a fresh app launch.
