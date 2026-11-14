# Markdown Editor — Implementation Plan

**Origin:** gregory, 2026-11-14 — the app can render Markdown but not help edit it; every input
site is a plain text field. Status: **implementation**. Option **A** was chosen 2026-11-14 (see "Decision" below and
[CHECKLIST.md](CHECKLIST.md)).

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

**Round-trip spike result (checklist step 2, 2026-11-14): an unchanged save is NOT lossless.**
Method: a throwaway `uikit` `jvmTest` ran each input through
`RichTextState().setMarkdown(x).toMarkdown()`, then a second time over the output. It needs no
Compose UI or composition, a bare `RichTextState` works headless on JVM. The inputs were the 28
step-1 syntax samples plus the 6 most Markdown-heavy seeded texts in project `main-2`: wiki
pages `getting-started`, `architecture`, `home`, `api-reference`, epic #2 and issue #69. Input
and output were both rendered through Taiga's `POST /api/v1/wiki/render`, which tells harmless
rewrites (different bytes, same HTML) apart from real loss. The spike is on `git stash` as
`markdown-editor step 2 spike: compose-rich-editor round-trip`.

**None of the 6 real texts came back byte-identical, and 4 of them render differently.** Three
of the four seeded wiki pages contain a fenced code block, and all three lose it.

| Construct | Result after one round-trip |
|---|---|
| `~~strike~~`, `#ref`, `[[wiki]]`, `[x](slug)`, `:emoji:`, `@mention`, bare e-mail, `[TOC]`, abbr, `{: attr}`, 4-space nested list, images, `snake_case`, single newlines | **Survive**, byte-identical (the editor leaves unknown inline syntax alone as plain text) |
| Definition list | Rewritten (`:   Def` → `: Def`), same rendered HTML |
| Trailing spaces on a line | Trimmed, rendered HTML differs only by whitespace (2 real descriptions) |
| **Fenced code block** | **Dropped**: the fences disappear and the code turns into a paragraph. A second save loses characters too (`val x = 1` → `val x  1`) and collapses aligned spaces |
| **Tables** | **Dropped**: the pipes are gone, cells become plain text joined by two spaces |
| **Task lists** | **Checkboxes dropped**, `- [x] done` → `- done` |
| **Footnotes** | **Definition dropped**, `[^1]: note` → `<br>`, the reference stays as literal `[^1]` |
| **Setext heading** (`Title\n=====`) | **Underline dropped**, the heading becomes a paragraph |
| Bare URL / `www.` autolink | Rewritten to `[url](url)`. Taiga's own autolinker then fires inside the link, so it renders as **a duplicated, empty `<a>` plus the real one**. `www.` loses its `http://` too |
| 2-space nested list | Re-indented to 4 spaces. It now renders nested on Taiga, where before it didn't: a visible change, arguably a fix |
| `<div markdown="1">` | Wrapper dropped, contents kept |
| Raw HTML | `<b>` → `**…**` (same render). `<script>x</script>` → bare `x`, where Taiga showed the escaped tag as literal text |

**This rules out option C as described.** Loading any text with a code block, table, task list or
footnote into the editor and saving it without changes would rewrite someone else's content.
Making C safe would need either our own Markdown ↔ `RichTextState` converter, or a guard that
refuses rich editing for any text that doesn't round-trip byte-identically. Both are bigger than
options A and B together.

**Leaning (not a decision):** A first, B later if A feels too raw. C only if step 2's spike shows
lossless round-trips on real Taiga content.

## Decision (2026-11-14, checklist step 3)

gregory chose **option A**: a Markdown-source editor with a formatting toolbar and an
Edit/Preview toggle. C is out because of step 2's round-trip loss. B stays possible as a later
add-on but isn't scheduled.

**Scope and order:** site 1 (`WorkItemEditDescriptionScreen`) first, since it covers description
edits for all five content types. Sites 2 (wiki create) and 3 (create task) follow as their own
steps, reusing the same toolbar. Site 4 (`CreateCommentBar`) is gated: a full toolbar doesn't fit
a one-line bar, so whether it gets a subset or nothing is gregory's call when that step comes up.

**Design that follows from steps 1–2:**
- **Transforms are pure `(TextFieldValue) -> TextFieldValue` functions** in `uikit`'s
  `widgets/editor/`, next to `insertMention`. The toolbar composable only calls them. This keeps
  them fully testable in `commonTest` and keeps the text lossless.
- **Toolbar actions:** bold `**`, italic `_`, strikethrough `~~`, inline code `` ` `` (wrap the
  selection, or insert an empty pair with the cursor between; applying the same action again
  unwraps it). Heading `# `, bullet `- `, numbered `1. `, quote `> `, task `- [ ] ` (line-prefix
  toggles over every line the selection touches). Indent/outdent in **4 spaces**, Taiga's
  nesting width (step 1). Link (`[text](url)`, with `url` selected afterwards). Fenced code block.
- **The transforms (step 4) are in `uikit/.../widgets/editor/MarkdownFormatting.kt`**: `toggleBold`/
  `toggleItalic`/`toggleStrikethrough`/`toggleInlineCode`, `toggleHeading`/`toggleQuote`/`toggleBulletList`/
  `toggleNumberedList`/`toggleTaskList`, `indentLines`/`outdentLines`, `insertLink`, `insertCodeBlock`.
  Behaviours the toolbar relies on:
  - **Inline actions toggle off** when the selection includes the markers or the markers surround it, and also
    when a bare cursor sits inside an empty pair.
  - **Line actions** act on every line the selection touches. They skip blank lines and keep leading indentation.
    A selection that ends right after a newline doesn't touch the next line. If every touched line already has
    the prefix, it's removed from all of them, otherwise it's added to all of them.
  - **List kinds swap.** Bullet, numbered and task list replace each other instead of stacking, so `- a` becomes
    `1. a` and not `1. - a`. Numbering is sequential (`1.`, `2.`, …).
  - **Link and code block only insert.** They don't toggle off.
  - **Every transform returns a fresh `TextFieldValue` with no IME composition range.**
- **The toolbar (step 5) is `uikit/.../widgets/editor/MarkdownToolbar.kt`**:
  `MarkdownToolbar(value, onValueChange, isPreview, onPreviewToggle)`. It is stateless. The caller owns
  both the `TextFieldValue` and the Edit/Preview flag. The toggle stays pinned at the start and the
  formatting buttons scroll horizontally. They are disabled while previewing. Icons come from
  `material-icons-extended`.
- **Preview uses the existing `MarkdownTextWidget`** with the screen's `members` for mention
  links. It shows the app's rendering, not Taiga's. Known differences are in `docs/revisit.md`
  entry 59, and a fix for that belongs with that entry, not with the toolbar.

## Open questions (answered by checklist steps)

1. Which Markdown dialect does Taiga store and render: GFM tables, task lists, `#ref` links,
   `[[wiki]]` links, emoji `:x:`? This sets both the toolbar's action list and option C's
   round-trip risk surface. Source: `taiga-back/taiga/mdrender/` (its `mentions.py` is already
   cited in [mention-tagging-support](../../architecture/mention-tagging-support/IMPLEMENTATION_PLAN.md)),
   checked live against `http://localhost:9000`.

   **Answer (checklist step 1, 2026-11-14).** Taiga renders with **Python-Markdown 3.10**, not a
   CommonMark/GFM engine, and runs the output through **bleach 4.1.0**
   (`taiga-back/taiga/mdrender/service.py`, `taiga-back` `eb0803da`; the running Docker image has
   the same versions and a byte-identical `service.py`). The extension list is
   `_make_extensions_list()`: `pymdownx.tasklist` (pymdown-extensions 10.17.1), eight custom
   extensions in `mdrender/extensions/`, then `extra`, `codehilite`, `sane_lists`, `toc`,
   `nl2br`. `extensions/spaced_link.py` exists but is never registered.

   Every row below was rendered live through `POST /api/v1/wiki/render` (the same `render()`
   call) on project `main-2`. The samples were then saved together as one user story
   `description`. It came back stored verbatim, and its `description_html` was byte-identical to
   `wiki/render`'s output for the same text. The story was deleted afterwards.

   | Syntax | Source | Live result |
   |---|---|---|
   | Tables | `extra` | `<table>` with `thead`/`tbody` |
   | Task lists `- [ ]` / `- [x]` | `pymdownx.tasklist` | `ul.task-list` with `<input type=checkbox [checked]>` |
   | Fenced code ```` ```lang ```` | `extra` + `codehilite` | `div.codehilite > pre > code`, Pygments `<span>`s |
   | `~~strike~~` | `strikethrough.py` | `<del>` |
   | `#123` refs | `references.py` | `a.reference.{epic,user-story,task,issue}` with text `&num;N`. Plain text if the ref doesn't exist. Not matched after `[`, letters, digits or `-` |
   | `[[Page]]`, `[[Page\|label]]` | `wikilinks.py` | `a.reference.wiki` to `/project/<slug>/wiki/<slugified>` |
   | Bare-slug links `[go](home)` | `wikilinks.py` (`RelativeLinksTreeprocessor`) | Rewritten to a wiki-page link. A `/path` href gets the instance base URL prepended |
   | `:emoji:` | `emojify.py` | `<img src=…/static/img/emojis/x.png>` only for names in a fixed ~880-name set. Unknown names stay literal |
   | `@username` | `mentions.py` | `a.mention` to `/profile/<user>`, only for project members. Otherwise plain text |
   | Bare URLs / `www.` | `autolink.py` | `<a>`, `www.` gets an `http://` prefix |
   | Bare e-mail | `automail.py` | `mailto:` link |
   | External links | `target_link.py` | `target="_blank"` on every non-instance href |
   | Attachment URLs | `refresh_attachment.py` | Expired attachment URLs in `img`/`a` get re-signed. URL rewrite only, no syntax |
   | **Single newline** | `nl2br` | **`<br>` hard break.** CommonMark would make this a soft break |
   | `[TOC]` | `toc` | Generated `div.toc` list. Headings get `id`s |
   | Footnotes `[^1]` | `extra` | `sup` ref + `div.footnote` |
   | Abbreviations `*[HTML]: …` | `extra` | `<abbr title>` |
   | Definition lists `Term\n:   Def` | `extra` | `<dl>` |
   | `{: #id .class }` attributes | `extra` (`attr_list`) | Applied to the element |
   | `<div markdown="1">` | `extra` (`md_in_html`) | Markdown parsed inside the HTML block |
   | Raw HTML | bleach whitelist | Kept only for whitelisted tags (`b`, `table`, `div`, `span`, `del`, `ins`, `sup`, `img`, `input`, `label`, …). Anything else, e.g. `<u>` and `<script>`, is **escaped and shown as literal text**, not stripped |
   | **Nested lists** | Python-Markdown core | **Need 4-space indentation.** `- a\n  - b` (2 spaces, valid GFM) renders as one flat list |
   | `- a\n1. b` | `semi_sane_lists` + `nl2br` | Not a mixed list: `1. b` folds into item `a` after a `<br>` |
   | `snake_case_name` | Python-Markdown core | No intra-word emphasis, stays literal |

   **Taiga's web editor stores Markdown, not HTML** (`taiga-front` `d2da06116`,
   `app/modules/components/wysiwyg/wysiwyg.directive.coffee`). `save` sends `$scope.markdown`.
   The editor has two modes, chosen per browser in `localStorage` `editor-mode`. **`html`, a
   CKEditor WYSIWYG from `taiga-html-editor`, is the default.** In that mode every change runs
   `updateMarkdownWithCurrentHtml()`, which converts the editor's HTML back to Markdown
   (`wysiwyg.service.coffee` `getMarkdown`). On load, `setHtmlEditor` also round-trips the stored
   text Markdown → HTML → Markdown (`refreshAttachmentURLFromMarkdown`). So a save from the web's
   default mode already replaces the stored text with that converter's output, whatever the user
   typed. The converter's internals, which live in `taiga-html-editor` and not in this checkout,
   were not inspected.

   **What this means for the options:**
   - **Option A toolbar:** the list-nesting action must indent with **4 spaces**. A task-list
     action (`- [ ] `) and a table insert are real features. Strikethrough is `~~`. The toolbar
     can't rely on a single newline being a soft break, because it is a hard break.
   - **Option A Preview / existing rendering:** `MarkdownTextWidget`'s CommonMark/GFM renderer
     differs from what Taiga shows on at least single newlines (soft vs. `<br>`), 2-space nested
     lists, `#ref`/`[[wiki]]`/`:emoji:` and `extra`'s footnote/abbr/deflist/attr syntax. A Preview
     tab shows the app's own rendering, not Taiga's. That matches what the read-only screens
     already show, so it adds no new inconsistency. It can't promise "this is what the web shows".
   - **Option C round-trip surface for step 2:** every row above. Taiga-specific inline syntax
     (`#ref`, `[[wiki]]`, `:emoji:`, `@mention`), whitelisted raw HTML, `extra`'s non-GFM
     constructs, 4-space nested lists, and single-newline line breaks are the likeliest to be
     rewritten. The web editor's own default mode also normalizes on save, so content touched on
     the web may already be in that converter's style. That doesn't make further rewrites safe.
     It only means "byte-identical to what's stored" is the bar step 2 has to measure against.
2. Does `compose-rich-editor` 1.2.1 round-trip real Taiga descriptions losslessly?
3. Scope: site 1 only first, or all four? `CreateCommentBar` is a compact single bar where a full
   toolbar may not fit, and might only get a subset or none.
