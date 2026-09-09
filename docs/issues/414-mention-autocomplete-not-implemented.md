# 414 — User marking with @ not triggering tagging

**Status:** Approved
**Link:** https://github.com/Grigoriym/TaigaMobileNova/issues/414   **Updated:** 2026-09-09 (backend verified against `taiga-back`/`taiga-front` source)

## Report

> While commenting and writing on the description page if I want to address a user or
> tag him with @, the action with @ is neither triggering to choose a user nor marking
> him within the text.
>
> Version 2.2.1
> F-Droid

- **Symptom:** typing `@` in a comment box or a description editor does nothing — no
  autocomplete popup appears, and no special styling/link is applied to the typed
  text afterward.
- **Environment:** v2.2.1, F-Droid build. No repro steps beyond "type @ while
  commenting or editing a description," no screenshots, no server version given.
- **Reporter's diagnosis:** implicitly treats this as a broken trigger ("not
  triggering"), i.e. assumes the feature exists and is misfiring. This is the
  hypothesis tested below.
- No comments on the issue. The reporter did not say which entity type (task, user
  story, epic, issue) or platform beyond "F-Droid" (Android is implied but not
  stated).
- GitHub label on the issue: `enhancement` (set by the repo, not the reporter).

## Findings

Traced every comment-input and description-edit surface, all @-mention-related
strings in production source, and the markdown rendering path.

- **Comment input** is `uikit/.../widgets/CreateCommentBar.kt:62-72`, wrapping
  `HintTextField` (`uikit/.../widgets/editor/TextFieldWithHint.kt:42`) — a plain
  hint-decorated `TextField` with only an `onValueChange` callback. No key/character
  listener, no popup, no `@`-detection of any kind.
- **Description editor** is `feature/workitem/ui/.../screens/editdescription/WorkItemEditDescriptionScreen.kt:102-110`
  — a raw `BasicTextField` bound directly to `state.currentDescription` /
  `state.onDescriptionChange`. Same absence of mention logic.
- **No production code references mention/autocomplete at all.** Grepping the whole
  repo for `mention`, `Mention`, `tagUser`, `autocomplete` across every `*Main` source
  set returns nothing except one string: `tools/seed/.../Seeder.kt:294`, a piece of
  *fake demo content* generated for the offline seed tool
  (`"Implement @mention autocomplete in comment fields that notifies mentioned
  users."`) — this is sample user-story text seeded into a demo project, not an
  implementation.
- **Markdown rendering has no `@username` rule either.** Both
  `uikit/.../widgets/text/MarkdownTextWidget.kt:13-27` and `ExpandableMarkdownText.kt`
  render through the third-party `com.mikepenz.markdown.m3.Markdown` composable, which
  only implements standard CommonMark. Nothing rewrites `@username` into a link or
  styled span before or after that call.
- **No autocomplete-capable text field exists in `uikit`** — `TextFieldWithHint.kt`
  is the only reusable text-field wrapper, and it has no suggestion-dropdown
  capability.
- **Closest existing analog:** the assignee picker —
  `feature/workitem/ui/.../widgets/AssignedToWidget.kt:36-176` opens
  `feature/workitem/ui/.../screens/teammembers/WorkItemEditTeamMembersScreen.kt:58-138`,
  a full-screen static `LazyColumn` of project team members with toggle-select, backed
  by `EditTeamMemberViewModel.kt`. It has no search/filter box and is not
  inline-text-triggered, but it already sources the "which users are on this
  project" data this app would need for a mention picker.
- **No Taiga "mentions" API surface is wired up anywhere** — no DTO, repository
  method, or endpoint reference in `core/api`/`feature/*/data` beyond the existing
  project-members/team endpoints already used for assignee selection.

All of the above are direct code-search results (file:line), not inference.

**Resolved against `taiga-back` source** (`/home/gregory/proj/taiga/taiga-back`, local
checkout): the server *does* fully handle `@username` server-side, independent of
anything the client does beyond sending the plain text:

- `taiga/mdrender/extensions/mentions.py:40-84` — a Markdown inline-pattern extension
  (`MENTION_RE = r"\B(@)([\w.-]+)\b"`) that runs whenever the server renders
  description/comment markdown to HTML. It looks up the matched username against the
  project's members, and for a match rewrites it into `<a class="mention"
  href="...">@username</a>` — this is the server-rendered HTML the client never
  requests (see below).
- `taiga/projects/notifications/services.py:100-133` (`get_mentions`,
  `get_object_mentions`, `add_watchers_to_project_from_mentions`) — the same
  extraction also runs independently of HTML rendering, purely to build a list of
  mentioned `User` objects from raw text.
- `taiga/projects/notifications/mixins.py:104-195`
  (`create_web_notifications_for_mentioned_users`,
  `create_web_notifications_for_mentions_in_comments`) — on every save of a
  description/content field or a new comment, diffs old vs. new mentions and fires
  `signal_mentions` / `signal_comment_mentions` for anything newly mentioned.
- `taiga/projects/notifications/signals.py:98-122` (`on_mentions`,
  `on_comment_mentions`) — the signal receivers push a real
  `WebNotificationType.mentioned` / `mentioned_in_comment` web notification to the
  mentioned user, and (`services.py:109-111`) add them as a watcher on the object.

**This means "tagging" in the sense the reporter means — the mentioned user actually
getting notified — already works today from this app**, with zero client-side special
handling: typing plain `@someusername` as ordinary text and saving/submitting the
comment or description is enough to trigger the server-side notification, because the
app already sends raw markdown text on the existing update/comment endpoints.

**What's genuinely missing is purely visual, on both ends of the round-trip:**

1. **Input-side:** no autocomplete/suggestion popup while typing `@` (confirmed
   above) — a user has to already know and correctly spell a teammate's username with
   no help or confirmation.
2. **Render-side:** the server *does* produce a ready-to-use hyperlinked
   `description_html` (`taiga/projects/userstories/serializers.py:149-158`, and the
   task/issue/epic equivalents) and `comment_html`
   (`taiga/projects/history/serializers.py:29`) field with the mention already
   resolved into a link — but this app's DTOs never decode it. Confirmed empty-result
   grep for `_html`/`description_html`/`comment_html` across all `*.kt` sources, and
   directly in the DTOs: `feature/workitem/dto/.../WorkItemResponseDTO.kt:36` only
   declares `val description: String?` (raw markdown), and
   `feature/workitem/dto/.../CommentDTO.kt` only declares the raw `comment` field —
   neither has an `_html` counterpart. The app re-renders raw markdown client-side via
   `MarkdownTextWidget`'s `com.mikepenz.markdown.m3.Markdown`, which has no mention
   rule, so the link the server already computed is discarded and never shown.

## Root cause

There is no client-side root cause to trace in the sense of a broken trigger — there
is no existing @-mention input feature to be broken. `CreateCommentBar`/
`HintTextField` and the description `BasicTextField` are plain text fields with no
character-triggered logic or member-suggestion popup. But the underlying
functionality the reporter actually wants — a mentioned user getting notified/tagged
— **already works server-side today**, purely because the app sends raw markdown
text; the server independently parses `@username` and fires a web notification (see
Findings). What's actually missing is two purely-visual gaps: no input-side
autocomplete, and no client-side rendering of the mention link the server already
computes and returns (discarded because the DTOs don't decode `description_html`/
`comment_html`). GitHub's `enhancement` label undersells this slightly — the visible
part of the report ("marking him within the text" doesn't happen) is a real client
bug/gap in how little of the server's own response is used, not purely a feature
request.

## Impact

A user who @-mentions a teammate gets no visual feedback while typing and no
indication afterward that a link/tag was created — the text just looks like plain
`@username`. **This is actively misleading every reporter into believing tagging
silently fails outright, when it doesn't: confirmed live (Open Questions #1) that the
mentioned user is notified and added as a watcher purely from the plain-text
`@username` this app already sends.** This is a parity gap against Taiga's web
client (which shows the autocomplete popup and renders the link) and a real,
confirmed client bug in how little of the server's response this app uses — not a
functional/notification gap.

## Open questions

1. ~~Not yet confirmed against a live instance...~~ **Resolved — confirmed live**
   against the local Taiga instance (`http://localhost:9000`, via `taiga-mcp`):
   `PATCH /api/v1/userstories/21` as `admin` with
   `{"comment": "Hey @user1 please check this out"}` produced, in the same response:
   - `watchers` went from `[5]` to `[5, 6]` (`user1`'s id is 6) — the mention added
     them as a watcher, exactly per `services.py:109-111`.
   - `GET /api/v1/history/userstory/21` shows `comment_html: "<p>Hey <a
     class=\"mention\" href=\"http://localhost:9000/profile/user1\"
     title=\"user1\">@user1</a> please check this out</p>"` — the server-rendered
     mention link this app's `CommentDTO` never decodes.
   - Logging in as `user1` and calling `GET /api/v1/web-notifications` shows a brand
     new notification, `event_type: 6` (`WebNotificationType.mentioned_in_comment`,
     confirmed against `choices.py:25-31`), timestamped the same second as the
     comment.

   **This proves the tagging/notification mechanism works end-to-end today, exactly
   as typing plain `@username` text and submitting a comment through this app's
   existing API calls** — no client-side mention handling of any kind is required for
   the functional half of the reporter's request. Confirms the Root Cause/Impact
   sections above are correct, not merely source-level inference. (Test comment left
   on the local dev instance's user story #21 — disposable test data on a local dev
   instance, not the production Taiga this app talks to.)
2. Reporter didn't say which entity type or confirm Android vs. no other platform;
   likely irrelevant since the input widgets (`CreateCommentBar`, description editor)
   are shared across all work-item types and are KMP-common, not Android-specific.

## Options

**A. Implement inline @-mention autocomplete (full parity with Taiga web).**
Add a popup/dropdown to `CreateCommentBar` and the description editor that opens on
`@`, filters the current project's team members (reusing the data source behind
`WorkItemEditTeamMembersScreen`) as the user types, and on selection inserts
`@username` into the text; render `@username` spans as styled/clickable in
`MarkdownTextWidget` (see option B for that half in isolation).
- Pros: closes the parity gap completely — matches Taiga web's actual input UX,
  reuses an existing member data source, protects users from silent typos in a
  username (a typo currently fails the server-side match silently — `mentions.py:69`
  falls back to plain `@typo'd-name` with no error).
- Cons: non-trivial UI work — needs a new suggestion-popup component in `uikit`
  (doesn't exist today), cursor-position-aware text editing, and coordination between
  `BasicTextField`/`HintTextField`'s raw text and structured token insertion.
- Risk/blast radius: touches shared `uikit` text-field code and two feature modules'
  editors; moderate size for a single PR, likely wants the multi-session
  `docs/<initiative>/CHECKLIST.md` split per CLAUDE.md.

**B. Render `@username` as a styled/clickable link in `MarkdownTextWidget`, no
input-side autocomplete.**
Leaves comment/description input as plain text (already functionally sufficient per
the live confirmation above); only `MarkdownTextWidget`/`ExpandableMarkdownText` gain
a rule recognizing `@username` against the current project's member list and render
it as a link/styled span — the client-side equivalent of what
`mentions.py:54-84` already computes server-side and this app already receives but
discards.
- Pros: small, contained surface (rendering only, no text-field/cursor work); directly
  fixes the visible part of the report ("not... marking him within the text") for
  every place a comment/description is later *displayed*, and — since tagging already
  works functionally (Open Questions #1) — closes most of the reporter's actual
  complaint on its own.
- Cons: doesn't add the input-time discovery/autocomplete Taiga web has — a user still
  has to know and correctly spell a teammate's username while typing, with no
  in-the-moment confirmation.

**C. Won't fix / backlog.**
Leave as-is, label stays `enhancement`.
- Pros: zero engineering cost.
- Cons: leaves users believing @-mentions don't work at all (per Impact) even though
  the notification already fires — a false negative that actively discourages a
  working feature.

**Recommendation: B first, then A as a follow-up if gregory wants full input-time
parity.** The live test proves the *notification* mechanism is already fine — the
entire user-facing gap is that nothing in the app ever shows a mention as a mention.
B closes that with a single, contained rendering change and no risk to the
comment/description input surfaces. A is worth doing afterward for typing-time
discoverability and typo protection, but it's a genuine feature addition (new uikit
component, cross-module editor changes) — size it as its own
`docs/<name>/CHECKLIST.md` initiative per CLAUDE.md rather than bundling it with B.

## Decision

**2026-09-09, gregory: go full support — Option A** (input-time autocomplete +
rendered mention links), not just the render-only minimal fix. Tracked as its own
multi-session initiative:
[docs/architecture/mention-tagging-support/](../architecture/mention-tagging-support/CHECKLIST.md).
