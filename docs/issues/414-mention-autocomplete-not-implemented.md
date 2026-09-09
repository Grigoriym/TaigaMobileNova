# 414 — User marking with @ not triggering tagging

**Status:** Awaiting decision
**Link:** https://github.com/Grigoriym/TaigaMobileNova/issues/414   **Updated:** 2026-09-09

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

**Not verified (inference / open question):** whether Taiga's *server* itself parses
`@username` out of comment/description text server-side (e.g. to send a mention
notification) independent of what the client renders. This repo has no access to the
`taiga-back`/`taiga-front` source, so this wasn't checked — see Open Questions.

## Root cause

There is no root cause to trace, because there is no existing @-mention feature to be
broken. `CreateCommentBar`/`HintTextField` and the description `BasicTextField` are
plain text fields with no character-triggered logic, no member-suggestion popup, and
no post-hoc `@username` markdown styling. The reported behavior ("@ does nothing") is
exactly what a text field with no mention handling would do — it isn't a regression
or a misfiring trigger, it's an absent feature. GitHub's own `enhancement` label on
the issue agrees with this reading.

## Impact

Every user who tries to @-mention a teammate in a comment or a description gets no
feedback and no tagging — the text is literally just typed characters. This is a
parity gap against Taiga's own web client, which does support inline @-mention
autocomplete. No workaround inside the app; a user has to know the teammate's
username and type it in prose, with no confirmation it will notify anyone.

## Open questions

1. **Does the Taiga backend do anything with a literal `@username` substring in a
   comment/description today** (e.g. trigger a notification) even without client-side
   autocomplete? If yes, a minimal fix could be "just render `@username` as a
   clickable/styled mention" without needing an input-side autocomplete popup. Needs
   checking against `taiga-back`'s comment/history-parsing code or a live instance
   test (type a raw `@username` from this app today and see if the mentioned user
   gets notified) — not done as part of this investigation.
2. Reporter didn't say which entity type or confirm Android vs. no other platform;
   likely irrelevant since the input widgets (`CreateCommentBar`, description editor)
   are shared across all work-item types and are KMP-common, not Android-specific.

## Options

**A. Implement inline @-mention autocomplete (full parity with Taiga web).**
Add a popup/dropdown to `CreateCommentBar` and the description editor that opens on
`@`, filters the current project's team members (reusing the data source behind
`WorkItemEditTeamMembersScreen`) as the user types, and on selection inserts
`@username` into the text; render `@username` spans as styled/clickable in
`MarkdownTextWidget`.
- Pros: closes the parity gap completely; reuses an existing member data source.
- Cons: non-trivial UI work — needs a new suggestion-popup component in `uikit`
  (doesn't exist today), cursor-position-aware text editing, and coordination between
  `BasicTextField`/`HintTextField`'s raw text and structured token insertion. Also
  depends on open question 1 — if the backend doesn't act on `@username` at all, this
  becomes cosmetic-only rather than "tagging."
- Risk/blast radius: touches shared `uikit` text-field code and two feature modules'
  editors; moderate size for a single PR, likely wants the multi-session
  `docs/<initiative>/CHECKLIST.md` split per CLAUDE.md.

**B. Minimal version: style/link `@username` in rendered markdown only, no input-side
autocomplete.**
Leaves comment/description input as plain text; only `MarkdownTextWidget` gains a rule
recognizing `@username` (matched against known project members) and renders it as a
clickable span (e.g. navigates to that user, or just visually highlights).
- Pros: much smaller surface — one file, no new uikit component, no text-editing
  cursor logic.
- Cons: doesn't address the reporter's actual ask ("the action with @ is ... not
  marking him within the text" while *typing* — they want the input-time experience,
  not just rendering). Partial fix at best.

**C. Won't fix / backlog.**
Leave as-is, label stays `enhancement`.
- Pros: zero engineering cost.
- Cons: known parity gap stays open indefinitely; issue will likely resurface from
  other reporters.

**Recommendation: A**, gated on resolving open question 1 first (a 5-minute check
against a live Taiga instance settles whether the backend already acts on
`@username`, which changes how much of A's scope is "must build" vs. "already
exists server-side"). This is a genuine feature addition, not a bug fix — sized for
the multi-session `docs/<name>/CHECKLIST.md` process in CLAUDE.md rather than a single
PR, given the new uikit component and the cross-module editor changes.

## Decision

Pending — not yet made by gregory.
