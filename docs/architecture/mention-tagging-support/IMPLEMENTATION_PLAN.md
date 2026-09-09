# @-Mention Tagging Support — Implementation Plan

**Origin:** [docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md)
— gregory's decision 2026-09-09: build full support (input-time autocomplete +
rendered mention links), not just the render-only minimal fix.

## What's already true (verified, not to be re-derived)

The investigation doc traced `taiga-back` source and confirmed live against the local
dev Taiga instance (`http://localhost:9000`) that **the notification/tagging
mechanism already works today**, purely from this app sending plain `@username`
text: the mentioned user gets added as a watcher and receives a real
`mentioned_in_comment`/`mentioned` web notification. Nothing in this plan touches
that — it already works and needs no client change. Everything below is client-side
UX: showing the user that a mention is a mention, both while typing it and
afterward when it's rendered.

## Server contract this plan relies on

- `taiga-back`'s `taiga/mdrender/extensions/mentions.py:48`:
  `MENTION_RE = r"\B(@)([\w.-]+)\b"` — the exact pattern the server matches. The
  client-side detection/rendering below should match this shape (word chars, dots,
  hyphens) so what the client highlights and what the server actually tags agree.
- A username that doesn't resolve to a real project member is left as plain
  `@typo'd-name` by the server (`mentions.py:66-69`, silent `except ... return`) — no
  error surfaces anywhere. This is exactly what the input-time autocomplete (picking
  a real member from a list, never free-typing a guess) is for: it structurally
  prevents ever sending a `@typo` the server will silently fail to tag.
- The server's own rendered link
  (`<a class="mention" href=".../profile/<username>" title="...">@username</a>`,
  confirmed live) is **not** consumed by this plan — this app's DTOs never decode
  `description_html`/`comment_html` and that isn't changing. The client independently
  recomputes the same linkification client-side (see Step 1) against its own known
  member list, which is simpler than adding two new DTO fields and a second code path
  that has to agree with the client-rendered markdown everywhere else in the app.

## Data source

`UsersRepository.getTeamMembersByProjectId(projectId, generateMemberStats = false):
ImmutableList<TeamMember>`
(`feature/users/domain/.../UsersRepository.kt:9-12`) is the existing call used to
populate the assignee/watcher picker
(`EditTeamMemberViewModel.kt:196` via `getTeamMembers()`, the no-project-id overload
used when the current project is already known from context). `TeamMember` already
carries `id`, `username`, `name` — exactly what both autocomplete filtering and
mention-link resolution need. **No new API/DTO work required anywhere in this
initiative** — every step below is UI/state-layer only, reusing this one existing
repository method.

## Rendering mechanism

`uikit` renders markdown via `com.mikepenz:multiplatform-markdown-renderer-m3`
(`MarkdownTextWidget.kt`, `ExpandableMarkdownText.kt`) — confirmed by decompiling the
library sources (`0.45.0`) that it exposes exactly the hook this needs, with no
custom Markdown extension required:

- `com.mikepenz.markdown.annotator.annotatorSettings(...)` takes a
  `linkInteractionListener: LinkInteractionListener?` — fires with the clicked
  `LinkAnnotation` whenever a rendered markdown link (`[text](url)`, standard
  CommonMark, already supported) is tapped. Default behavior opens the URL via
  `LocalUriHandler`; this can be overridden per-call.
- **Plan:** before handing raw text to `Markdown(...)`, rewrite any `@username`
  substring that matches a real member in the passed-in member list into standard
  markdown link syntax: `[@username](mention:<id>)`. Pass a custom
  `linkInteractionListener` that recognizes the `mention:` scheme, extracts the id,
  and calls an `onMentionClick: (Long) -> Unit` callback instead of opening a URL —
  wired at the call site to
  `com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen(userId)`
  (`ProfileNavDestination(userId: Long)`, already navigable from any screen —
  confirmed it's already used this way for issue/task/US/epic/wiki "creator" avatars
  in `composeApp/.../nav/*NavGraph.kt`, not gated to "self profile only").
- Both `MarkdownTextWidget` and `ExpandableMarkdownText` need a new optional
  `members: ImmutableList<TeamMember> = persistentListOf()` param (default empty =
  today's behavior, unchanged) and `onMentionClick: (Long) -> Unit = {}`.

Only two production call sites render markdown that could plausibly contain a
mention: `feature/workitem/ui/.../widgets/WorkItemDescriptionWidget.kt` (description)
and `.../widgets/CommentsSectionWidget.kt` (comments). `CustomFieldsWidget.kt` also
calls `ExpandableMarkdownText` but for arbitrary custom-field text, not
description/comments — out of scope, leave its default empty `members` alone.

## Input-side mechanism

Two existing text-input surfaces need the same "detect `@`, show popup, insert
selection" behavior, but they're built on different primitives:

- `uikit/.../widgets/CreateCommentBar.kt:62-72` — comments, wraps
  `HintTextField` (`uikit/.../widgets/editor/TextFieldWithHint.kt`), itself a
  thin wrapper around Compose `TextField`. Owns its own `commentTextValue` state
  locally via `rememberSaveable { mutableStateOf("") }` — currently a plain `String`,
  not a `TextFieldValue`, so it has no cursor/selection tracking today.
- `feature/workitem/ui/.../screens/editdescription/WorkItemEditDescriptionScreen.kt:102-110`
  — description editing, a raw `BasicTextField` bound to
  `state.currentDescription`/`state.onDescriptionChange` via the ViewModel's state
  pattern (`EditDescriptionState.kt`/`EditDescriptionViewModel.kt`).

Both need to move from tracking a plain `String` to tracking `TextFieldValue`
(carries cursor/selection position — required to know where `@` was typed and where
to splice the selected username back in). This is a real, if mechanical, change to
each surface's state shape.

**Shared pure logic (put in `uikit`, no Compose dependency beyond `TextFieldValue`,
independently unit-testable):**

```kotlin
// Given the current TextFieldValue, find an "active" @query ending at the cursor,
// or null if the cursor isn't inside one.
fun findActiveMentionQuery(value: TextFieldValue): MentionQuery? // (query: String, range: IntRange)

// Splice `username` in place of the active query's range, inserting a trailing
// space and moving the cursor after it.
fun insertMention(value: TextFieldValue, query: MentionQuery, username: String): TextFieldValue
```

Match the query pattern against the server's own `[\w.-]+` shape (see Server
contract above) so a query the popup is filtering on is always a legal Taiga
username shape.

**New `uikit` component:** a suggestion popup/dropdown, anchored to the text field,
showing `TeamMember` rows (avatar, username, name) filtered by the active query's
prefix, dismissed on selection or on the query no longer matching any member.
Consult the **uikit-guide** subagent first (per CLAUDE.md) for whatever
dropdown/popup primitive already exists in `uikit` (e.g. the pattern behind
`DropdownSelector`) before building a new one from scratch — this may turn out to be
a thin wrapper rather than new positioning/anchoring logic.

**Call-site wiring:** both `CreateCommentBar` and `WorkItemEditDescriptionScreen`
need a new `members: ImmutableList<TeamMember>` param, sourced from
`getTeamMembersByProjectId` at whichever level already knows the current project id.
`CreateCommentBar` is called from 4 details screens
(`UserStoryDetailsScreen`/`TaskDetailsScreen`/`EpicDetailsScreen`/`IssueDetailsScreen`);
`WorkItemEditDescriptionScreen` is one shared screen (`WorkItemEditsNavGraph.kt`,
also reused by `WikiPageScreen` via the shared `WorkItemDescriptionState`/delegate
pattern per CLAUDE.md's Navigation Pattern section) — check during Step 4/5 whether
each of these screens already fetches the project's team members for something else
(e.g. the assignee widget) before adding a second fetch; reuse if so.

## Offline / permissions

Per CLAUDE.md's Offline State Pattern: the mention popup is a convenience, not a
network write — it should still appear while offline (the member list was already
fetched and cached in memory for the session; no new network call happens per
keystroke). `CreateCommentBar` already takes `isOffline` and disables the whole input
when offline — no separate offline handling needed for the popup itself, it simply
never appears because the field is disabled. Confirm this holds for the description
editor too during Step 6.

## Non-goals

- Not changing anything about the server-side notification/tagging mechanism — it
  already works (see "What's already true" above).
- Not decoding `description_html`/`comment_html` from the API — the client
  recomputes linkification itself (see "Server contract" above for why).
- Not adding a "mention" concept to any domain model beyond what's needed to render
  a link and drive the popup — no new persisted state, no new Taiga API calls beyond
  the existing team-members endpoint.
