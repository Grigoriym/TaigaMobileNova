# Markdown Editor — Checklist

**Progress:** 4/9 done. **Current step:** 5.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for what exists today, the input-site
inventory, and options A/B/C. Option A was chosen; see the plan's "Decision" section for the toolbar action
list and design.

## 5. `MarkdownToolbar` composable

A horizontally scrollable row of icon buttons in `uikit/.../widgets/editor/` that takes the
current `TextFieldValue` and emits the transformed one, plus an Edit/Preview toggle. Consult the
**uikit-guide** agent first for existing icon-button and preview conventions. Each button has a
content description string. Add a `@PreviewTaigaDarkLight` preview and a `jvmTest` Compose UI
test (tapping Bold on a selection wraps it).

**Verify:** `./gradlew :uikit:jvmTest :uikit:compileKotlinIosArm64 :uikit:detekt` and `ktlintCheck` green.

## 6. Wire the toolbar into site 1 (`WorkItemEditDescriptionScreen`)

Toolbar sits above the keyboard (IME-aware). The Edit/Preview flag lives in
`EditDescriptionState` per the ViewModel + State pattern. Preview renders
`MarkdownTextWidget(text, members = …)`, and switching back keeps text and selection. Mentions
keep working in Edit mode. Extend `EditDescriptionContentTest`.

**Verify:** full `./gradlew jvmTest`, `ktlintCheck`, then drive it on the emulator for a Task
description and a Wiki page edit: format text, preview, save, and reopen to see the saved
Markdown.

## 7. Site 2: wiki create page content field

Same toolbar and toggle on `WikiCreatePageScreen`'s content field (already a `TextFieldValue`).

**Verify:** full `jvmTest`, `ktlintCheck`, emulator: create a formatted wiki page.

## 8. Site 3: create task description field

Same on `CreateTaskScreen`. Its `description` is a plain `String` today, so it needs a
`TextFieldValue` in state first (keep the same save behaviour).

**Verify:** full `jvmTest`, `ktlintCheck`, emulator: create a task with a formatted description.

## 9. Site 4: `CreateCommentBar`

⛔ **Gated — do not start without asking.**

A full toolbar doesn't fit the one-line bar. gregory decides between a subset (e.g. bold,
italic, code, link), a compact expand button, or nothing.
