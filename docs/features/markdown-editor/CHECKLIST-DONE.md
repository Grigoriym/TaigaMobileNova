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
