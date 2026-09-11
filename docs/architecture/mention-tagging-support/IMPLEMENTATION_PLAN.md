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

**One client-side consequence of that server-side add was missed by this plan's
original 8 steps**: the work-item screen's own `watchersState` (the watchers list
shown in the UI, `WorkItemWatchersDelegate` in `feature/workitem/ui`) is a client
cache loaded once in `loadTask()` and otherwise only updated by explicit
watch/unwatch actions — posting a comment or description that mentions someone
never refreshed it, so the new watcher was invisible until the screen was left and
re-entered. Fixed separately (not as a checklist step) by adding
`containsMention(text)` (`uikit/.../editor/MentionQuery.kt`) and
`WorkItemWatchersDelegate.refreshWatchers()`, called from `createComment()`/
`onNewDescriptionUpdate()` in all four work-item ViewModels (Task/Issue/UserStory/
Epic) only when the posted text contains a mention, to avoid two extra network
calls on every plain comment/description save.

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
ImmutableList<TeamMember>` and its no-arg-project sibling `getTeamMembers(...)`
(`feature/users/domain/.../UsersRepository.kt:7-12`) are the existing calls used to
populate the assignee/watcher picker (`EditTeamMemberViewModel.kt:196`, via
`getTeamMembers()` — the current-project-from-context overload). `TeamMember` already
carries `id`, `username`, `name` — exactly what both autocomplete filtering and
mention-link resolution need. **No new API/DTO work required anywhere in this
initiative.**

**Found 2026-09-10, before any code was written for Step 1:** despite the method
already existing, **no details ViewModel currently holds this data.**
`EditTeamMemberViewModel` fetches it lazily, only when the user navigates into the
separate full-screen assignee/watcher picker — none of `TaskDetailsViewModel`,
`UserStoryDetailsViewModel`/equivalent, `EpicDetailsViewModel`, `IssueDetailsViewModel`,
or the Wiki page ViewModel keep a project's team-member list in memory today. Getting
real `members` into `WorkItemDescriptionWidget`/`CommentsSectionWidget` (Step 1's
original scope) or into `CreateCommentBar`/the description editor (Steps 4-5) both
require the same new fetch — so building it once, shared, is the only sane sequencing.
This codebase already has an established shape for exactly this kind of cross-cutting,
per-entity-type concern: composed delegates under
`feature/workitem/ui/.../delegates/{comments,description,assignee,watchers,...}`,
each mixed into every details ViewModel via `by SomeDelegateImpl(...)`
(`TaskDetailsViewModel.kt:112` for the comments delegate is the concrete example
already read). **Decision (2026-09-10): build a new delegate in that same family**
(exact name TBD when Step 4 starts) exposing `StateFlow<ImmutableList<TeamMember>>`,
mixed into every details ViewModel that needs it, rather than duplicating an ad hoc
fetch per screen. This pushed the real data-wiring out of Step 1 and into Step 4 — see
CHECKLIST.md's Step 1 scope note and Step 4 for the concrete split. The navigation
side of this was already free: `goToProfile: (Long) -> Unit` is already threaded from
each `*NavGraph.kt` (wired to `navigator.navigateToProfileScreen`) down through every
one of these screens and into `CommentsSectionWidget`/`CommentItem` already — confirmed
reading `TaskDetailsScreen.kt:81,301,371-423` and `CommentsSectionWidget.kt:41,63,83,108`
— so `onMentionClick` in Step 4/5 is just `goToProfile`, not a new callback to invent.

## Rendering mechanism

`uikit` renders markdown via `com.mikepenz:multiplatform-markdown-renderer-m3`
(`MarkdownTextWidget.kt`, `ExpandableMarkdownText.kt`, both driven by the
`com.mikepenz.markdown.m3.Markdown(...)` composable). Confirmed by decompiling the
exact pinned version (`0.45.0`, `gradle/libs.versions.toml`'s `markdownRenderer`) that
it exposes what this needs, with no custom Markdown extension required — but the hook
is one level down from what the plan originally assumed:

- `Markdown(...)`'s own top-level params have **no** `linkInteractionListener`/click
  override — its `components: MarkdownComponents` param is the only per-node-type
  customization surface (`m3/Markdown.kt:62-102`, delegates to
  `com.mikepenz.markdown.compose.Markdown`).
- `MarkdownComponents` (`compose/components/MarkdownComponents.kt`) holds one
  `@Composable (MarkdownComponentModel) -> Unit` lambda per markdown node type —
  `text`, `paragraph`, `heading1..6`, `blockQuote`, lists, etc. — each defaulted to a
  `CurrentComponentsBridge` entry that calls the matching element composable
  (`MarkdownText`, `MarkdownParagraph`, ...).
- **`MarkdownText(content: String, node, style, ...)` and `MarkdownParagraph(content,
  node, ...)` (`compose/elements/`) both take `annotatorSettings: AnnotatorSettings =
  annotatorSettings()` directly** — this is the actual hook.
  `com.mikepenz.markdown.annotator.AnnotatorSettings` (`annotator/AnnotatorSettings.kt`)
  carries `linkInteractionListener: LinkInteractionListener?`; `annotatorSettings()`'s
  default listener opens the clicked `LinkAnnotation.Url.url` via `LocalUriHandler`.
  Traced the link all the way through: `AnnotatedStringKtx.kt:145,182,213,230` builds
  every rendered link as `LinkAnnotation.Url(url, annotatorSettings.linkTextSpanStyle,
  annotatorSettings.linkInteractionListener)` — so whatever listener is threaded
  through `annotatorSettings` at render time becomes that specific link's own
  listener; nothing ambient/global to fight with.
- **So the actual override point is `Markdown(...)`'s `components` param**, built via
  `markdownComponents(text = { model -> MarkdownText(..., annotatorSettings = X) },
  paragraph = { model -> MarkdownParagraph(..., annotatorSettings = X) })`. Only
  these two need overriding — nested contexts (list items, blockquotes, table cells)
  all read the same `LocalMarkdownComponents.current` CompositionLocal to render their
  own inline content (confirmed in `MarkdownList.kt:49`), so the override propagates
  automatically without touching every component individually.
- `X` (a small `@Composable` helper, not exported as a public API — built in
  `MarkdownTextWidget.kt` as `mentionAnnotatorSettings(onMentionClick)`): start from
  the default `annotatorSettings()` and wrap its `linkInteractionListener` — for a
  `LinkAnnotation.Url` whose `url` starts with a `mention:` scheme, call
  `onMentionClick(id)`; otherwise delegate to the *default* listener (which still
  opens a real `http(s)` URL via `LocalUriHandler` exactly as today). Overriding the
  listener outright instead of falling back would silently break clicking any genuine
  non-mention link already present in a description — don't do that.
- **`mentionAnnotatorSettings(...)` must be called from *inside* each `text`/
  `paragraph` component lambda, not hoisted once above `Markdown(...)`.** Its call to
  the library's `annotatorSettings()` reads `LocalMarkdownTypography.current`
  (`AnnotatorSettings.kt:48`), which `Markdown(...)` only provides within its own
  composition — not yet available at `MarkdownTextWidget`'s call site before
  `Markdown(...)` is entered. Hoisting it crashed every existing caller
  (`ExpandableMarkdownTextTest`, `CreateCommentBarTest`) with `IllegalStateException:
  No local MarkdownTypography` (confirmed 2026-09-10, Step 1). The library's own
  `CurrentComponentsBridge` defaults call `annotatorSettings()` the same lazy way, in
  the same place — this is the pattern to follow, not an edge case to work around.
- **Overriding `components` at all also silently swaps the checkbox renderer** unless
  `checkbox` is re-specified in the same `markdownComponents(...)` call. The m3
  `Markdown(...)`'s own default (`m3/Markdown.kt:75`) already overrides `checkbox` to
  `com.mikepenz.markdown.m3.elements.MarkdownCheckBox` (renders a Material3
  `Checkbox`); once *any* custom `components` object is passed in, that default is
  gone and GFM task-list items would silently fall back to the core library's plain
  checkbox. Copy that same `checkbox` lambda into the custom `markdownComponents(...)`
  call to avoid the regression (confirmed 2026-09-10, Step 1 — not something the
  original plan anticipated).
- **Plan:** before handing raw text to `Markdown(...)`, rewrite any `@username`
  substring that matches a real member in the passed-in member list into standard
  markdown link syntax: `[@username](mention:<id>)` (a plain string preprocessing
  pass over the raw markdown source, not an AST-aware rewrite — known limitation: a
  literal `@name` inside an inline code span would also get linkified, since the
  regex can't see node boundaries; acceptable for v1, revisit only if it's ever
  reported as a real problem). `onMentionClick: (Long) -> Unit` gets wired at the
  call site to `com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen(userId)`
  (`ProfileNavDestination(userId: Long)`, already navigable from any screen —
  confirmed it's already used this way for issue/task/US/epic/wiki "creator" avatars
  in `composeApp/.../nav/*NavGraph.kt`, not gated to "self profile only") — in
  practice this is just the `goToProfile` callback every details screen already has,
  see the Data source section above.
- Both `MarkdownTextWidget` and `ExpandableMarkdownText` need a new optional
  `members: ImmutableList<TeamMember> = persistentListOf()` param (default empty =
  today's rendered output, unchanged — `rewriteMentions` short-circuits on an empty
  list) and `onMentionClick: (Long) -> Unit = {}`. The custom `components` object is
  always built regardless of whether `members` is empty — it's cheap, and branching
  on it would just be two code paths to keep in sync for no behavioral gain.
- The mention regex the client should use to find candidates before matching against
  `members`: `\B@([\w.-]+)\b`, mirroring the server's own `\B(@)([\w.-]+)\b`
  (`mentions.py:48`) minus the capture group around `@` itself (not needed
  client-side). Same pattern reused for Step 2's cursor-based detection — keep both
  in sync if one ever changes.

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

**New `uikit` component:** `MentionSuggestionsPopup` (`uikit/.../widgets/editor/`),
showing `TeamMember` rows (avatar, username, name) filtered by the active query's
prefix, dismissed on selection or on the query no longer matching any member. Built
directly on Material3's `DropdownMenu`/`DropdownMenuItem`, not a wrapper around
`DropdownSelector` — that composable always owns its own tap-to-toggle trigger row
with no externally-driven `expanded` param, which doesn't fit a popup whose
visibility must track "is there an active query" rather than a tap. Its `expanded`/
`onDismissRequest` params are the minimum `DropdownMenu` itself requires to be driven
externally.

**Call-site wiring:** both `CreateCommentBar` and `WorkItemEditDescriptionScreen`
need a new `members: ImmutableList<TeamMember>` param, sourced from
`getTeamMembersByProjectId` at whichever level already knows the current project id.
`CreateCommentBar` is called from 4 details screens
(`UserStoryDetailsScreen`/`TaskDetailsScreen`/`EpicDetailsScreen`/`IssueDetailsScreen`);
`WorkItemEditDescriptionScreen` is one shared screen (`WorkItemEditsNavGraph.kt`,
also reused by `WikiPageScreen` via the shared `WorkItemDescriptionState`/delegate
pattern per CLAUDE.md's Navigation Pattern section) — check during Step 4/5 whether
each of these screens already fetches the project's team members for something else
(e.g. the assignee widget) before adding a second fetch; reuse if so. Step 4 answered
this for `CreateCommentBar`'s 4 call sites: none had team members in scope, hence the
new `WorkItemMentionsDelegate` (see "Team-member data source" above) — check the same
question for `WorkItemEditDescriptionScreen`/`WikiPageScreen`'s ViewModels in Step 5
rather than assuming the answer carries over.

**`expanded` cannot be a pure function of "is there an active query."** Step 4 tried
that for `CreateCommentBar` and it broke `DropdownMenu`'s own back-press/outside-tap
dismiss: `onDismissRequest` fired, but with nothing to set, the next recomposition
re-derived `expanded = true` from the still-live query and the popup reappeared
immediately — silently swallowing the Android back gesture while the popup was open.
Fixed with an explicit `isMentionPopupDismissed` boolean, reset to `false` whenever the
field's text changes and set `true` by `onDismissRequest`; `expanded =
!isMentionPopupDismissed && <query matches something>`. Step 5's description-editor
popup needs the same explicit flag, not a repeat of the pure-derivation attempt.

**Decision (2026-09-10): replace the `DropdownMenu`-based `MentionSuggestionsPopup`
with an inline, horizontally-scrollable row — not a second option kept alongside the
first.** Origin: gregory compared the dropdown against a row-based picker used by
another app and preferred the row (fixed position under the input, doesn't grow
unbounded); investigating a separate report that typing `@` dismisses/flickers the
keyboard (`docs/issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md`) then
found a structural reason to prefer it too, not just a style call: `DropdownMenu`'s
default `PopupProperties(focusable = true)` (confirmed via decompiled
`androidx.compose.material3`/`androidx.compose.ui` sources — see that doc's Findings)
makes the popup a genuinely separate, focusable Android window, and Android hands
that window input focus the moment it opens, detaching the IME from the text field
being typed into. A `LazyRow` rendered inline in normal layout flow never creates a
second window, so it avoids this by construction. Setting `focusable = false` on the
existing `DropdownMenu` was considered and rejected: that flag is the same one the
popup needs for its own back-press/outside-tap dismiss (the `isMentionPopupDismissed`
paragraph above), so turning it off trades the keyboard-dismiss bug for a
back-dismiss regression instead of fixing anything.

**What the replacement changes, for whichever step implements it:**

- `MentionSuggestionsPopup.kt` (`uikit/.../widgets/editor/`) gets replaced in place
  (not duplicated) with a row component built on `LazyRow`, not `DropdownMenu`/
  `Popup`. Its `members`/`onSelect` params stay the same shape; `onDismissRequest`
  most likely goes away entirely (see next point) — confirm rather than assume once
  actually wiring it.
- **`isMentionPopupDismissed` most likely becomes unnecessary at both call sites.**
  The whole reason it exists (`DropdownMenu`'s own back-press/outside-tap dismiss
  fighting a pure-derivation `expanded`, previous paragraph) doesn't apply to a
  non-`Popup` row — there is no framework-driven dismiss to fight. Visibility can
  likely go back to the originally-attempted pure derivation:
  `activeMentionQuery != null && mentionSuggestions.isNotEmpty()`, which also matches
  gregory's own stated hide condition ("the moment @ is removed, we will hide it").
  Verify this actually holds once built — don't carry the flag over on the assumption
  it's still needed.
- Chip content (avatar only vs. avatar+username, full name likely dropped for space)
  is not decided here — a UI call to make during implementation, not a blocker before
  starting.
- GUI verification for whichever step confirms this must use **real on-screen-keyboard
  taps**, not `adb shell input text` — Step 5's own GUI check used synthetic text
  injection and screenshotted after each string landed rather than frame-by-frame at
  the moment `@` was typed, which is why it never caught the keyboard-dismiss/flicker
  in the first place (see the investigation doc's Open Questions).

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
