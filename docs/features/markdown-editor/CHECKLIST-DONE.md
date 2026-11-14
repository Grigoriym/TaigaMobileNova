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
