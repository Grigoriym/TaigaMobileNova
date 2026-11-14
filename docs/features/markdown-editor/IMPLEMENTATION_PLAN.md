# Markdown Editor — Implementation Plan

**Origin:** gregory, 2026-11-14 — the app can render Markdown but not help edit it; every input
site is a plain text field. Status: **investigation**. No approach is chosen yet (see
[CHECKLIST.md](CHECKLIST.md) step 3).

## What exists today (verified 2026-11-14)

**Rendering:** `uikit/.../widgets/text/MarkdownTextWidget.kt` wraps
`com.mikepenz:multiplatform-markdown-renderer` (`markdownRenderer = "0.46.0"`, `-m3` + `-coil3`
artifacts). On top of the library it rewrites `@username` into `mention:<id>` links
(`rewriteMentions`) and routes clicks on them through `onMentionClick`.
`ExpandableMarkdownText.kt` sits on top of it. The library only renders. It has no editor.

**Input sites.** All four are plain text fields with no formatting help:

| # | Site | Widget | Text type | Mention row |
|---|------|--------|-----------|-------------|
| 1 | `WorkItemEditDescriptionScreen` (`feature/workitem/ui/.../screens/editdescription/`): the full-screen description editor. Task, Issue, User Story, Epic **and the Wiki page edit** all navigate here via `navigateToWorkItemEditDescription` (`composeApp/.../nav/{Task,Issue,UserStory,Epic,Wiki}NavGraph.kt`) | `BasicTextField` | `TextFieldValue` in VM state | yes |
| 2 | `WikiCreatePageScreen` content field (`feature/wiki/ui/.../page/create/`) | `HintTextField` | `TextFieldValue` | yes |
| 3 | `CreateTaskScreen` description field (`composeApp/.../createtask/`) | `HintTextField` | — | no |
| 4 | `CreateCommentBar` (`uikit/.../widgets/`): the compact bar under comments | `HintTextField` | `TextFieldValue` (`rememberSaveable`) | yes |

No flow for editing an existing comment was found. Comments can only be created.

Site 1 matters most. One screen covers description editing for all five content types, so an
editor built there reaches most of the app at once.

**Mention coupling.** `uikit/.../widgets/editor/MentionQuery.kt` (`findActiveMentionQuery`,
`insertMention`) works on a `TextFieldValue` (text + selection). An editor that keeps
`TextFieldValue` as its model keeps mentions working unchanged. One that replaces the text model
(see option C) has to rebuild mention detection and insertion on its own state type.

## Options

### A. Markdown-source editor: formatting toolbar + Edit/Preview toggle

The text stays raw Markdown. A toolbar above the keyboard applies transforms to the
`TextFieldValue` selection: wrap it in `**`/`_`/`` ` ``, prefix the line with `# `/`- `/`1. `/`> `/`- [ ] `,
insert `[text](url)`, and so on. A Preview tab renders the current text with the existing
`MarkdownTextWidget`.

- **+** Lossless by construction. The server gets exactly what the user typed, so other users'
  content written on the web, in syntax this app doesn't understand, survives an edit untouched.
- **+** No new dependency. All common code, so Android, iOS and desktop work the same.
- **+** The toolbar transforms are pure `(TextFieldValue) -> TextFieldValue` functions, so they
  can be fully tested in `commonTest`. Same shape as `insertMention`.
- **+** Mentions keep working with no changes.
- **−** Not WYSIWYG: the user still sees `**bold**` while typing.

### B. Option A + live syntax highlighting

Same as A, plus styling of Markdown tokens while typing (bold text shown bold, markers dimmed)
through a `VisualTransformation`. The styling is display-only, so the text stays lossless.

- **+** Close to WYSIWYG feel at a fraction of the risk.
- **−** Highlighting on every keystroke with a hand-rolled or JetBrains-markdown-parser pass. Long
  wiki pages need a perf check (see `docs/perf/profiling.md`).
- **−** The cleaner API for this (`TextFieldState` + `OutputTransformation`) would mean moving
  sites 1/2/4 off `TextFieldValue`, which touches the mention helpers. Can be added after A, not
  instead of it.

### C. WYSIWYG with `compose-rich-editor`

`com.mohamedrejeb.richeditor:richeditor-compose` is the main KMP rich-text editor. Latest is
**1.2.1** (Maven Central `maven-metadata.xml`, lastUpdated 2026-09-30). It publishes per-target
artifacts for `-android`, `-desktop` (**not** `-jvm`), `-iosarm64` and `-iossimulatorarm64`, so
all three of this app's platforms are covered. It edits a `RichTextState` and converts to and
from Markdown.

- **+** True WYSIWYG. The best UX if it works.
- **−** **Round-trip loss is the deciding risk.** Content is Markdown on the server, often written
  in Taiga's web editor. Anything the library's Markdown import/export can't represent (tables,
  raw HTML, nested lists, Taiga-specific syntax such as `#123` refs or `[[wiki links]]`) can get
  silently rewritten or dropped the moment a mobile user saves an unrelated one-word fix. That is
  data loss in someone else's text. Not verified yet, see checklist step 2.
- **−** Mentions have to be rebuilt on `RichTextState` instead of `TextFieldValue`.
- **−** New third-party dependency on the core edit path.

**Leaning (not a decision):** A first, B later if A feels too raw. C only if step 2's spike shows
lossless round-trips on real Taiga content.

## Open questions (answered by checklist steps)

1. Which Markdown dialect does Taiga store and render: GFM tables, task lists, `#ref` links,
   `[[wiki]]` links, emoji `:x:`? This sets both the toolbar's action list and option C's
   round-trip risk surface. Source: `taiga-back/taiga/mdrender/` (its `mentions.py` is already
   cited in [mention-tagging-support](../../architecture/mention-tagging-support/IMPLEMENTATION_PLAN.md)),
   checked live against `http://localhost:9000`.
2. Does `compose-rich-editor` 1.2.1 round-trip real Taiga descriptions losslessly?
3. Scope: site 1 only first, or all four? `CreateCommentBar` is a compact single bar where a full
   toolbar may not fit, and might only get a subset or none.
