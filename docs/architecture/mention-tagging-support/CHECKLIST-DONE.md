# @-Mention Tagging Support — Completed Steps

Precedent for a step cited by number — not a place to look for open work. See
[CHECKLIST.md](CHECKLIST.md) for what's still open.

## Step 1: Render `@username` as a tappable mention link (uikit mechanism only)

Added mention-link rewriting to `MarkdownTextWidget`/`ExpandableMarkdownText`
(`uikit/.../widgets/text/`): a new optional `members: ImmutableList<TeamMember> =
persistentListOf()` and `onMentionClick: (Long) -> Unit = {}` param on both. Before
handing text to `Markdown(...)`, `rewriteMentions()` turns any `@username` substring
matching a real `members` entry into `[@username](mention:<id>)`; a non-matching
`@name` (no such member) and an `@` preceded by a word character (server's `\B`
boundary) are both left untouched. `Markdown(...)`'s `components` param is overridden
with `text`/`paragraph` lambdas that pass a custom `AnnotatorSettings` wrapping the
library's default `linkInteractionListener`: a `mention:<id>` URL calls
`onMentionClick(id)`, anything else falls through to the default (real links still
open via `LocalUriHandler`). `checkbox` is also re-specified in the same
`markdownComponents(...)` call, copying the m3 `Markdown(...)` default's own
`MarkdownCheckBox` (Material3 `Checkbox`) — omitting it would have silently fallen
back to the core library's plain checkbox for GFM task-list items the moment
`components` is overridden at all.

**Note:** the checklist's plan called for hoisting the built `AnnotatorSettings` once
at the top of `MarkdownTextWidget` and reusing it in both `text`/`paragraph` lambdas.
That crashed every existing caller (`ExpandableMarkdownTextTest`,
`CreateCommentBarTest`) with `IllegalStateException: No local MarkdownTypography`:
`annotatorSettings()`'s defaults read `LocalMarkdownTypography.current`, which
`Markdown(...)` only provides *inside* its own composition — not yet available at
`MarkdownTextWidget`'s call site before `Markdown(...)` is entered. Fixed by calling
the composable `mentionAnnotatorSettings(onMentionClick)` freshly inside each `text`/
`paragraph` lambda instead (matching how the library's own default components call
`annotatorSettings()` lazily in the same place) — this is the same pattern the
library's `CurrentComponentsBridge` defaults use, just missed in the original plan.
No other deviation from the step's description.

Verify: new `uikit` `commonTest`
(`MarkdownTextWidgetTest.kt`) covers the matching-username, no-match pass-through,
`\B`-boundary pass-through, and empty-members-list cases — written failing first
(crashed on `mentionAnnotatorSettings` before the fix above), all 4 pass now.
`./gradlew jvmTest` (full suite) and `./gradlew ktlintCheck` (whole repo) both green;
`:uikit:compileKotlinIosSimulatorArm64` confirms the iOS target still compiles. No
emulator check — this step has no live screen yet (Step 4 wires real data in). Next:
Step 2 (mention-query detection + insertion utility) — independent of this step, no
gate.

## Step 2: Mention-query detection + insertion utility

Added `MentionQuery(query: String, range: IntRange)`, `findActiveMentionQuery(value:
TextFieldValue): MentionQuery?` and `insertMention(value: TextFieldValue, query:
MentionQuery, username: String): TextFieldValue` — pure functions, no Compose UI
dependency beyond the `TextFieldValue`/`TextRange` types — in a new
`uikit/.../widgets/editor/MentionInput.kt` (same package as `TextFieldWithHint.kt`,
the other text-input primitive).

`findActiveMentionQuery` walks backward from the (collapsed-selection) cursor through
`[\w.-]` characters looking for a preceding `@`; it returns `null` if the selection
isn't collapsed, no `@` is found, or the `@` is itself preceded by a word character
(`\w`) — mirroring the server's `\B(@)([\w.-]+)\b` (`taiga-back`'s `mentions.py:48`,
already cited in Step 1) without needing regex backtracking over the whole string.
`range` covers `@` through the last query character inclusive, so `insertMention` can
replace it directly with `String.replaceRange`. `insertMention` splices in
`"@$username "` and moves the cursor to just after the trailing space.

Verify: new `uikit` `commonTest` (`MentionInputTest.kt`, same source set as Step 1's
`MarkdownTextWidgetTest.kt` — the functions take `TextFieldValue`/`TextRange`, both
multiplatform Compose UI types, not JVM-only) covers all 5 cases from the checklist
(mid-word query found, completed-mention-plus-space not active, `@` after a word
character not a trigger, bare-`@` empty query, non-collapsed selection not active)
plus 2 `insertMention` splice cases — all 7 pass. `./gradlew jvmTest` (full suite),
`:uikit:ktlintCheck`, and `:uikit:compileKotlinIosSimulatorArm64` all green. No
deviation from the step's description. Next: Step 3 (mention suggestion popup
component) — independent of this step, no gate.
