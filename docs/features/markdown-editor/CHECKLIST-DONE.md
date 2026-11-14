# Markdown Editor — Checklist (done)

Ticked steps, moved here from [CHECKLIST.md](CHECKLIST.md). Precedent only, not open work.

## 1. Pin down Taiga's Markdown dialect

Read `taiga-back`'s `taiga/mdrender/` (the extensions list and the markdown library config) and
list every syntax Taiga renders beyond CommonMark: tables, task lists, `#ref` links, `[[wiki]]`
links, emoji, mentions, anything else. For each, confirm it live by writing it into a description
on the local instance (`http://localhost:9000`, taiga-mcp) and checking `description_html`. Also
check whether Taiga's own web editor stores Markdown or HTML in `description`.
Record the list in IMPLEMENTATION_PLAN.md's "Open questions" → answer 1.

**Verify:** the list has a server-source citation and a live `description_html` result for each
entry.

**Done 2026-11-14.** Note: the live check used `POST /api/v1/wiki/render` per sample (there is no `/api/v1/mdrender` route). One throwaway user story then confirmed `description_html` is byte-identical to `wiki/render`'s output for the same text. That was simpler than a separate description write per syntax. The answer is in IMPLEMENTATION_PLAN.md's Open questions → 1. Next: step 2, the `compose-rich-editor` round-trip spike. Its sample list is that answer's table.

## 2. Spike `compose-rich-editor` round-trip fidelity

Throwaway `jvmTest` (not committed; keep it on `git stash`, not the scratchpad) depending on
`richeditor-compose-desktop:1.2.1`. Feed each step-1 syntax sample, plus 3–5 real seeded
descriptions/wiki pages, through `setMarkdown()` → `toMarkdown()` and diff the result against the
input. Record which constructs survive, which get rewritten, and which get dropped.

**Verify:** a table of per-construct results in IMPLEMENTATION_PLAN.md under option C, including
a clear yes/no on "an unchanged save is lossless".

**Done 2026-11-14.** Note: the round-trip was also run a second time over the output, and input and output were compared by Taiga's rendered HTML (`wiki/render`), not only by text diff, to separate harmless rewrites from real loss. Verdict: an unchanged save is **not** lossless. Fenced code, tables, task lists, footnotes and setext headings are dropped. The table is in IMPLEMENTATION_PLAN.md under option C. Next: step 3, which is gated: gregory picks the approach and scope.

## 3. Choose the approach and scope

gregory picks A / A+B / C and which input sites (1–4) are in scope, based on steps 1–2. Record the
decision in IMPLEMENTATION_PLAN.md, then decompose the implementation into checklist steps (its
own commit).

**Done 2026-11-14.** Note: gregory chose option A. The scope wasn't stated explicitly, so it is decomposed as site 1 first, then sites 2–3, with site 4 gated (steps 4–9). Next: step 4, the pure formatting transforms.

## 4. Pure Markdown formatting transforms

Add `uikit/.../widgets/editor/MarkdownFormatting.kt`: one `(TextFieldValue) -> TextFieldValue`
function per toolbar action from the plan's "Decision" section (inline wrap/unwrap, line-prefix
toggle, 4-space indent/outdent, link insert, fenced code block). No Compose UI. `commonTest`
covers each action with a selection, with a bare cursor, toggling an action off, and across
multiple selected lines.

**Verify:** `./gradlew :uikit:jvmTest` and `ktlintCheck` green.

**Done 2026-11-14.** Note: `insertLink` with nothing selected inserts `[text](url)`; with a selection the selection becomes the link text. Either way `url` ends up selected. Taiga renders a fenced block correctly right after a paragraph line, with no blank line needed (checked live via `wiki/render`), so `insertCodeBlock` only adds the newlines that put the fences on their own lines. `:uikit:jvmTest` alone missed a JVM-only API (`StringBuilder.replace(Int, Int, String)`). Only the iOS compile caught it, and `detekt` caught a loop style rule. Step 5's Verify now runs both. Next: step 5, the `MarkdownToolbar` composable.

## 5. `MarkdownToolbar` composable

A horizontally scrollable row of icon buttons in `uikit/.../widgets/editor/` that takes the
current `TextFieldValue` and emits the transformed one, plus an Edit/Preview toggle. Consult the
**uikit-guide** agent first for existing icon-button and preview conventions. Each button has a
content description string. Add a `@PreviewTaigaDarkLight` preview and a `jvmTest` Compose UI
test (tapping Bold on a selection wraps it).

**Verify:** `./gradlew :uikit:jvmTest :uikit:compileKotlinIosArm64 :uikit:detekt` and `ktlintCheck` green.

**Done 2026-11-14.** Note: icons come from `material-icons-extended`, now an `implementation` dependency of
`uikit`. `composeApp` already depended on it, so the app's binary doesn't grow. API:
`MarkdownToolbar(value, onValueChange, isPreview, onPreviewToggle)`. The Edit/Preview toggle is pinned at the
start, outside the scrolling row, and the formatting buttons are disabled (not hidden) while previewing. Content
descriptions are new `markdown_*` strings; the toggle reuses the existing `edit` string. Next: step 6, wiring the
toolbar into `WorkItemEditDescriptionScreen`.

## 6. Wire the toolbar into site 1 (`WorkItemEditDescriptionScreen`)

Toolbar sits above the keyboard (IME-aware). The Edit/Preview flag lives in
`EditDescriptionState` per the ViewModel + State pattern. Preview renders
`MarkdownTextWidget(text, members = …)`, and switching back keeps text and selection. Mentions
keep working in Edit mode. Extend `EditDescriptionContentTest`.

**Verify:** full `./gradlew jvmTest`, `ktlintCheck`, then drive it on the emulator for a Task
description and a Wiki page edit: format text, preview, save, and reopen to see the saved
Markdown.

**Done 2026-11-14.** Note: `EditDescriptionState` gained `isPreview`/`onPreviewToggle`. Preview swaps the
`BasicTextField` for a scrolling `MarkdownTextWidget`, and the mention row only shows in Edit mode. The toolbar sits
at the bottom of the content `Column` with `imePadding()`, and on the emulator it rode directly above Gboard.
`MarkdownTextWidget` applies its `modifier` twice when `onClick` is null, so passing it `verticalScroll` crashed with a
nested-scroll error; the preview puts the scroll on a wrapping `Box` instead, and the widget bug is `docs/revisit.md`
entry 60. Emulator: a Task (#51) and a Wiki page (`gggg`) were formatted with bold, heading, bullet, task-list and
link, previewed, saved, and reopened; Taiga stored the exact Markdown in both cases. Both were restored to their
seeded content afterwards. Next: step 7, the toolbar on `WikiCreatePageScreen`.

## 7. Site 2: wiki create page content field

Same toolbar and toggle on `WikiCreatePageScreen`'s content field (already a `TextFieldValue`).

**Verify:** full `jvmTest`, `ktlintCheck`, the module's `detekt` and `compileKotlinIosArm64`, emulator: create a formatted wiki page.

**Done 2026-11-14.** Note: `WikiCreatePageState` gained `isPreview`/`onPreviewToggle`. The slug field stays visible in
Preview; only the content field and mention row swap for `MarkdownTextWidget`. The screen's existing `verticalScroll`
moved onto an inner `Column` with `weight(1f)` so the toolbar can sit below it, outside the scroll, and ride above the
keyboard via the outer `imePadding()`. `feature/wiki/ui` had no Compose UI test wiring, so its `build.gradle.kts` gained
the same `jvmTest` deps `feature/workitem/ui` uses, for the new `WikiCreatePageScreenContentTest`. That test has to
`waitUntil` the preview text appears, because the Markdown renderer parses asynchronously; step 6's test doesn't wait
(`docs/revisit.md` entry 61). Emulator: created wiki page `md-step7-check` with heading, bullet and bold from the toolbar,
previewed, saved; the app handed off to the new page and Taiga stored `# Title\n- item\n**strong**` exactly. The page
was deleted afterwards. Next: step 8, the toolbar on `CreateTaskScreen`.

## 8. Site 3: create task description field

Same on `CreateTaskScreen`. Its `description` is a plain `String` today, so it needs a
`TextFieldValue` in state first (keep the same save behaviour).

**Verify:** full `jvmTest`, `ktlintCheck`, the module's `detekt` and `compileKotlinIosArm64`, emulator: create a task with a formatted description.

**Done 2026-11-14.** Note: `CreateTaskState.description` is now a `TextFieldValue`, with `isPreview`/`onPreviewToggle`
added. `SavedStateHandle` still stores only the plain text under the same `description` key, so process-death
restore is unchanged; a restored description gets its cursor at the end. Preview passes no `members`, since this screen
never loaded any and has no mention row. Layout follows step 7: the scrolling inner `Column` takes `weight(1f)` and the
toolbar sits below it. `composeApp`'s `jvmTest` gained `compose.uiTest` + `compose.desktop.uiTestJUnit4` for the new
`CreateTaskScreenContentTest` (it already had `currentOs` via `jvmMain`). Emulator: created issue `md-step8-check` with
heading, bullet and bold from the toolbar, previewed, saved; the app handed off to the new issue and Taiga stored
`# Plan\n- item\nmake **strong**` exactly. The issue was deleted afterwards. Next: step 9 (`CreateCommentBar`), which is
gated on gregory choosing a toolbar subset, a compact expand button, or nothing.
