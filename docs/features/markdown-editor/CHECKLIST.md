# Markdown Editor — Checklist

**Progress:** 7/9 done. **Current step:** 8.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for what exists today, the input-site
inventory, and options A/B/C. Option A was chosen; see the plan's "Decision" section for the toolbar action
list and design.

## 8. Site 3: create task description field

Same on `CreateTaskScreen`. Its `description` is a plain `String` today, so it needs a
`TextFieldValue` in state first (keep the same save behaviour).

**Verify:** full `jvmTest`, `ktlintCheck`, the module's `detekt` and `compileKotlinIosArm64`, emulator: create a task with a formatted description.

## 9. Site 4: `CreateCommentBar`

⛔ **Gated — do not start without asking.**

A full toolbar doesn't fit the one-line bar. gregory decides between a subset (e.g. bold,
italic, code, link), a compact expand button, or nothing.
