# Markdown Editor — Checklist

**Progress:** 10/11 done. **Current step:** 11.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for what exists today, the input-site
inventory, and options A/B/C. Option A was chosen; see the plan's "Decision" section for the toolbar action
list and design.

## 11. Continue a list when Enter is pressed

Pressing Enter at the end of a list item today just starts a plain line. Make it behave like Google Docs or Jira: the new
line starts with the next item's marker, and Enter on an empty item leaves the list.

- **Transform:** a pure `continueMarkdownList(previous: TextFieldValue, next: TextFieldValue): TextFieldValue` in
  `uikit/.../widgets/editor/MarkdownFormatting.kt`, with `commonTest` coverage. It acts only when `next` is `previous`
  with one line break typed at the cursor; anything else (typing, a multi-line paste, deletions) passes through.
  The finished line's indentation is kept and its marker carries over: `- `/`* `/`+ ` stays the same, `3. ` → `4. `,
  `- [x] ` → `- [ ] `. Renumbering later items isn't needed: Taiga's `sane_lists` numbers items sequentially anyway.
- **Empty item:** Enter on an item with nothing after its marker (and nothing after the cursor) removes the marker,
  leaving a blank line so the next paragraph isn't a lazy continuation of the list. A nested empty item outdents one
  level (4 spaces) instead.
- **Code blocks:** a list-looking line inside a fenced block is not continued.
- **Keyboards:** Gboard with autocorrect can commit a corrected word and the newline in one change, so detection must
  not assume the text before the cursor is unchanged — only that one `\n` was added before the cursor and the text after
  it is untouched.
- **Wiring:** the text field's `onValueChange` at the four Markdown sites only (`WorkItemEditDescriptionScreen`,
  `WikiCreatePageScreen`, `CreateTaskScreen`, `CreateCommentBar`), not inside `TextFieldWithHint`/`HintTextField`,
  which plain-text fields (e.g. `BlockDialog`) also use. Quotes (`> `) are not continued.

**Verify:** full `jvmTest`, `ktlintCheck`, `detekt` for the touched modules, `compileKotlinIosArm64`, emulator with
Gboard: type a bullet, numbered and task list in the edit-description screen and a comment, press Enter after each item,
press Enter on an empty item, and check autocorrect on the last word of an item doesn't break continuation.
