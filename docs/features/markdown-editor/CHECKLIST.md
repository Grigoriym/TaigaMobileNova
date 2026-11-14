# Markdown Editor — Checklist

**Progress:** 0/3 done. **Current step:** 1.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for what exists today, the input-site
inventory, and options A/B/C. Implementation steps get decomposed after step 3's decision, as
their own commit.

## 1. Pin down Taiga's Markdown dialect

Read `taiga-back`'s `taiga/mdrender/` (the extensions list and the markdown library config) and
list every syntax Taiga renders beyond CommonMark: tables, task lists, `#ref` links, `[[wiki]]`
links, emoji, mentions, anything else. For each, confirm it live by writing it into a description
on the local instance (`http://localhost:9000`, taiga-mcp) and checking `description_html`. Also
check whether Taiga's own web editor stores Markdown or HTML in `description`.
Record the list in IMPLEMENTATION_PLAN.md's "Open questions" → answer 1.

**Verify:** the list has a server-source citation and a live `description_html` result for each
entry.

## 2. Spike `compose-rich-editor` round-trip fidelity

Throwaway `jvmTest` (not committed; keep it on `git stash`, not the scratchpad) depending on
`richeditor-compose-desktop:1.2.1`. Feed each step-1 syntax sample, plus 3–5 real seeded
descriptions/wiki pages, through `setMarkdown()` → `toMarkdown()` and diff the result against the
input. Record which constructs survive, which get rewritten, and which get dropped.

**Verify:** a table of per-construct results in IMPLEMENTATION_PLAN.md under option C, including
a clear yes/no on "an unchanged save is lossless".

## 3. Choose the approach and scope

⛔ **Gated — do not start without asking.**

gregory picks A / A+B / C and which input sites (1–4) are in scope, based on steps 1–2. Record the
decision in IMPLEMENTATION_PLAN.md, then decompose the implementation into checklist steps (its
own commit).
