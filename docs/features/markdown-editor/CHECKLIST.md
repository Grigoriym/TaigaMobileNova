# Markdown Editor — Checklist

**Progress:** 9/10 done. **Current step:** 10.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for what exists today, the input-site
inventory, and options A/B/C. Option A was chosen; see the plan's "Decision" section for the toolbar action
list and design.

## 10. Tap a task-list checkbox in a saved description to tick it

Today a `- [ ]` / `- [x]` item renders as a checkbox in the read-only description, but tapping it does nothing: the
library's `MarkdownCheckBox` (`com.mikepenz.markdown.m3.elements`, 0.46.0) draws an M3 `Checkbox` with
`onCheckedChange = null`. Make a tap flip that one item's `[ ]` ↔ `[x]` in the Markdown source and save the description.

- **Where:** `WorkItemDescriptionWidget` (`feature/workitem/ui/.../widgets/`) is the read-only description on Task,
  Issue, User Story, Epic and Wiki page details, so one change there covers all five. It renders through
  `ExpandableMarkdownText` → `MarkdownTextWidget`, which already overrides the `checkbox` component
  (`MarkdownTextWidget.kt`, `checkbox = { model -> MarkdownCheckBox(...) }`); swap that for an interactive one when a
  toggle callback is given, and keep it display-only otherwise (Preview, comments).
- **Finding the item:** the component's `model.node` is the checkbox `ASTNode`, whose `startOffset`/`endOffset` index
  into `model.content`, the source string. A pure `(String, offset) -> String` that flips the box at that offset goes in
  `uikit/.../widgets/editor/MarkdownFormatting.kt` next to the other transforms, with `commonTest` coverage. Check first
  that the content the widget passes the renderer is the raw description: `rewriteMentions` changes the text before
  rendering, so offsets may need mapping back, or the toggle must run on the rewritten string's counterpart.
- **Saving:** the same path an edited description already takes — each details ViewModel's `onNewDescriptionUpdate`
  → `WorkItemDescriptionDelegate.updateDescription` (`patchData` with the item's `version`); the wiki page has its own
  `patchWikiPage`. One save per tap. On failure (e.g. a version conflict) show the existing error and keep the old text.
- **Gating:** only interactive when `canModify` and not offline, the flags `WorkItemDescriptionWidget` already takes.
  Otherwise the checkbox stays display-only.

**Verify:** full `jvmTest`, `ktlintCheck`, `detekt` for the touched modules, `compileKotlinIosArm64`, emulator: tick and
untick an item in a Task description and a Wiki page, reopen, and confirm via the Taiga API that `[x]`/`[ ]` were stored.
