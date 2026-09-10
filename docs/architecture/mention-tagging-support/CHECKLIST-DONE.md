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
