# @-Mention Tagging Support — Checklist

**Progress:** 0/6 done. **Current step:** 1 (mention-link rendering — mechanism only,
re-scoped 2026-09-10 before any code was written, see note below).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice — including
its new "Team-member data source" section (added 2026-09-10). Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).

Steps 1-3 are independent building blocks (rendering, detection logic, popup
component) with no ordering dependency between them. Step 1 no longer produces a
user-visible change by itself (see its scope note) — that lands with Step 4. Steps
4-5 depend on 2 and 3. Step 6 depends on everything before it.

## Step 1: Render `@username` as a tappable mention link (uikit mechanism only)

**Scope note, added 2026-09-10 before implementation started:** this step originally
also wired real project-member data into `WorkItemDescriptionWidget`/
`CommentsSectionWidget`. Investigation (done live, no code written) found that no
details ViewModel — Task/UserStory/Epic/Issue/Wiki — currently holds a project's
team-member list; that data is only ever fetched today inside the standalone
assignee/watcher picker (`EditTeamMemberViewModel`). Sourcing it for real means a new
shared delegate mirroring the existing `feature/workitem/ui/.../delegates/*` pattern
(comments/description/assignee all work this way), mixed into all ~5-6 details
ViewModels — which is exactly what Steps 4-5 need anyway for the autocomplete popup.
Rather than build that fetch twice, **this step is uikit-only**: the rendering
mechanism, verified with fake/fixture data, not a live screen. Steps 4 and 5 now own
building the delegate and wiring `members` into both the display widgets (finishing
this step's leftover) and the input widgets, in one pass. Full reasoning:
IMPLEMENTATION_PLAN.md's "Team-member data source" section.

Add mention-link rewriting to `MarkdownTextWidget`/`ExpandableMarkdownText`
(`uikit/.../widgets/text/`): a new optional `members: ImmutableList<TeamMember> =
persistentListOf()` and `onMentionClick: (Long) -> Unit = {}` param. Before handing
text to `Markdown(...)`, rewrite any `@username` substring matching a real entry in
`members` into `[@username](mention:<id>)`. The mikepenz library's top-level
`Markdown(...)` composable has no direct `linkInteractionListener`/click-override
param — the hook is one level down: `MarkdownParagraph(...)` and `MarkdownText(...)`
(`com.mikepenz.markdown.compose.elements`) both take an `annotatorSettings:
AnnotatorSettings = annotatorSettings()` param directly, and `AnnotatorSettings`
carries `linkInteractionListener` (confirmed by decompiling
`multiplatform-markdown-renderer[-m3]:0.45.0` sources — this project's pinned
version). So: override `Markdown(...)`'s `components` param
(`markdownComponents(text = ..., paragraph = ...)`) with lambdas that call
`MarkdownText`/`MarkdownParagraph` passing a custom `annotatorSettings` — build it by
taking the default `annotatorSettings()` and wrapping its `linkInteractionListener`:
for a `LinkAnnotation.Url` whose `url` starts with the `mention:` scheme, call
`onMentionClick(id)`; otherwise delegate to the default listener (which opens the URL
via `LocalUriHandler`, e.g. for a real link that happens to be in the text) — don't
just replace it outright, or genuine non-mention links break. Only `text` and
`paragraph` need overriding; nested contexts (list items, blockquotes) read the same
`LocalMarkdownComponents.current` and inherit the override automatically — confirmed
by reading `MarkdownList.kt`, don't re-verify this in the next session.

Demonstrate the mechanism with `@PreviewTaigaDarkLight` previews passing a literal
fixture `ImmutableList<TeamMember>` (2-3 fake members, one referenced by `@` in the
sample text, one not — to show both the linked and pass-through cases side by side).
**Do not touch `WorkItemDescriptionWidget.kt`, `CommentsSectionWidget.kt`, or any
ViewModel in this step** — that's Step 4/5's job now. Leave `CustomFieldsWidget.kt`
alone too either way (default empty `members`, out of scope per the plan).

**Verify:** a new `uikit` `commonTest` for the rewrite function (given text + a
member list, assert the exact rewritten markdown string, including the "no match for
this username" pass-through case, and the "`@` preceded by a word char is not a
mention" case per the server's `\B` boundary — see Step 2's identical concern) —
write it failing first, then make it pass. No emulator check for this step — there's
no real screen showing live data yet; the first real GUI-verify of rendered mentions
happens in Step 4.

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

## Step 4: Build the team-members delegate, wire autocomplete into `CreateCommentBar`, and finish Step 1's rendering wiring

This step now carries the data-plumbing work deferred from Step 1 (see its scope
note) — it's the biggest single step in this initiative. Consider whether it still
fits one session once Step 2/3 are done and this one actually starts; if not, split
the delegate build (part a) from the `CreateCommentBar` wiring (part b) into their
own checklist entries rather than pushing through — that's a normal mid-initiative
re-plan per CLAUDE.md, not a deviation to avoid.

1. **Build the shared delegate** (name TBD at implementation time, e.g.
   `WorkItemMentionsDelegate`/`WorkItemMentionsDelegateImpl`), mirroring the existing
   `feature/workitem/ui/.../delegates/{comments,description,assignee}` pattern:
   exposes the current project's team members as
   `StateFlow<ImmutableList<TeamMember>>`, backed by `usersRepository.getTeamMembers()`
   (the no-project-id-arg overload the assignee picker already uses — confirm it
   really does resolve "current project" from ambient session state before reusing
   it, don't assume from the name alone). Mix it into whichever of
   Task/UserStory/Epic/Issue's details ViewModels don't already have equivalent data
   in scope.
2. **Wire `CreateCommentBar` autocomplete.** Convert its local comment state from
   `String` to `TextFieldValue`. On every edit, run Step 2's
   `findActiveMentionQuery`; when non-null, filter the new `members` param by prefix
   and show Step 3's popup; on selection, apply `insertMention` and hide the popup.
   Update the 4 call sites (`UserStoryDetailsScreen`, `TaskDetailsScreen`,
   `EpicDetailsScreen`, `IssueDetailsScreen`) to pass `members` from the new
   delegate. Confirm the popup is simply absent while `isOffline` (field already
   disables under that condition — no separate offline branch expected, confirm
   during this step rather than assume).
3. **Finish Step 1's rendering wiring**, now that `members` exists for real: pass it
   (plus `onMentionClick = goToProfile` — already threaded to every one of these
   call sites today, confirmed 2026-09-10, no new callback needed) into
   `WorkItemDescriptionWidget`/`CommentsSectionWidget` at each of the same 4 details
   screens. This is what actually makes Step 1's mechanism visible in the running
   app for the first time.

**Verify:** update/extend `CreateCommentBarTest.kt` (`uikit/src/jvmTest/`) — typing
`@` shows filtered suggestions, selecting one inserts `@username `, and the final
submitted comment string is exactly what one existing test already asserts today
(no regression to the plain-text path when no `@` is typed). Then GUI-verify on the
emulator: type `@`, see the popup, pick a member, send the comment, confirm it
renders as a link (first real end-to-end check of Step 1's mechanism too).

## Step 5: Wire autocomplete into the description editor (work items and wiki)

Same input-side mechanism as Step 4, applied to
`WorkItemEditDescriptionScreen.kt`'s `BasicTextField`/`EditDescriptionState.kt`/
`EditDescriptionViewModel.kt`, plus the same rendering-wiring finish (`members` +
`onMentionClick` into `WorkItemDescriptionWidget` at the description-display call
site, if Step 4 didn't already cover every entity type). This screen is shared by
`WikiPageScreen` via the `WorkItemDescriptionState`/delegate pattern (CLAUDE.md's
Navigation Pattern section) — confirm wiki pages get the popup too as a side effect,
not a separate implementation, and that Wiki's own ViewModel can reuse Step 4's
delegate (or needs its own instance — Wiki isn't one of the 4
`CreateCommentBar` call sites, so this may be the first time its ViewModel needs
`members` at all; check what it already has in scope before assuming reuse is free).

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
