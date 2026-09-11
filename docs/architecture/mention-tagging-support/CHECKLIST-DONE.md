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

## Step 3: Mention suggestion popup component

Consulted the **uikit-guide** subagent first, per the step's "before starting" note.
`DropdownSelector` (`uikit/.../widgets/DropdownSelector.kt`) turned out unsuitable to
wrap: it always renders its own tap-to-toggle trigger row with no externally-driven
`expanded` param, but a mention popup's visibility must be driven by "is there an
active `@query`," not a tap. Its underlying primitive — Material3's own
`DropdownMenu`/`DropdownMenuItem` — is what it's built on, and is exactly the right
building block to use directly. Also confirmed no existing uikit row composable fits
(`UserItem` has no username field; the assignee picker's `TeamMemberItem` is
feature-local, private, and bakes in a selection checkmark) — built a small private
`MentionSuggestionRow` instead, matching `UserItem`'s avatar/spacer/column layout
convention (40dp circular avatar, 6dp spacer, username then name stacked).

Added `MentionSuggestionsPopup(members: ImmutableList<TeamMember>, expanded: Boolean,
onSelect: (TeamMember) -> Unit, onDismissRequest: () -> Unit, modifier: Modifier =
Modifier)` in `uikit/.../widgets/editor/MentionSuggestionsPopup.kt` (same package as
Step 2's `MentionInput.kt`). `expanded`/`onDismissRequest` were added beyond the two
params the checklist named (`members`, `onSelect`) — not scope creep, but the minimum
Material3's `DropdownMenu` itself requires to be driven externally rather than by an
internal tap-to-toggle. Background uses `dialogTonalElevation`, matching
`DropdownSelector`'s own surface treatment for visual consistency with the existing
dropdown.

Verify: new `uikit` `jvmTest` (`MentionSuggestionsPopupTest.kt`, same source set and
`runComposeUiTest` pattern as `DropdownSelectorTest.kt`) covers both `expanded = true`
(renders all given members' username+name, tapping a row invokes `onSelect` with that
member) and `expanded = false` (renders nothing) — both pass. `./gradlew jvmTest`
(full suite), `:uikit:ktlintCheck`, and `:uikit:compileKotlinIosSimulatorArm64` all
green. No deviation from the step's description. No emulator check — this component
has no call site yet (Step 4 wires it into `CreateCommentBar`). Next: Step 4 (team-
members delegate + `CreateCommentBar` wiring + finish Step 1's rendering wiring) —
depends on Steps 2 and 3, both now done.

## Step 4: Team-members delegate, `CreateCommentBar` autocomplete wiring, Step 1 rendering finish

Built `WorkItemMentionsDelegate`/`WorkItemMentionsDelegateImpl`
(`feature/workitem/ui/.../delegates/mentions/`), matching the existing delegate family:
exposes `mentionsState: StateFlow<WorkItemMentionsState>` (`members:
PersistentList<TeamMember>`), populated by a `suspend fun loadMembers()` that calls
`usersRepository.getTeamMembers()` (confirmed it resolves the current project id
internally via `TaigaSessionStorage`, per `UsersRepositoryImpl` — no project-id param
needed) inside `resultOf {}`, logging at `LogPriority.ERROR` and leaving `members`
empty on failure (a mention popup that stays empty is a silent, non-blocking
degradation — no snackbar/error state needed for this convenience feature). Mixed into
all 4 details ViewModels (`TaskDetailsViewModel`, `UserStoryDetailsViewModel`,
`EpicDetailsViewModel`, `IssueDetailsViewModel`) via `by WorkItemMentionsDelegateImpl(
usersRepository = usersRepository)`, each calling `viewModelScope.launch {
loadMembers() }` in `init {}` alongside the existing `loadTask()`/equivalent call.

Wired `CreateCommentBar` (`uikit/.../widgets/CreateCommentBar.kt`) for autocomplete: its
local comment state moved from `rememberSaveable { mutableStateOf("") }` to
`rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }`,
needing a new `HintTextField` overload (`uikit/.../widgets/editor/TextFieldWithHint.kt`)
taking `TextFieldValue`/`(TextFieldValue) -> Unit` — mirrors the existing `String`
overload exactly, added as an overload rather than changing the existing one since 4
other call sites still use plain `String`. On every edit, `findActiveMentionQuery`
(Step 2) runs against the new value; a non-null query filters the new `members` param
by `startsWith(query, ignoreCase = true)` and feeds `MentionSuggestionsPopup` (Step 3),
wrapped in a `Box` around the text field so the popup anchors to it. Selecting a
suggestion calls `insertMention` (Step 2). Deviation from the step's plan: added an
explicit `isMentionPopupDismissed` boolean (reset to `false` whenever the text changes,
set `true` in `onDismissRequest`) rather than deriving `expanded` purely from
"is there a live query" — a pure derivation left `DropdownMenu`'s own
back-press/outside-tap dismiss with nothing to do (the query stays active, so a purely
derived `expanded` would immediately show it again), which would have swallowed the
Android back gesture while the popup was open. Confirmed `isOffline` needs no separate
branch: the field disables via `enabled = !isOffline` exactly as before, so the popup
never gets a chance to open while offline — no new code path.

Updated the 4 call sites (`TaskDetailsScreen`, `UserStoryDetailsScreen`,
`EpicDetailsScreen`, `IssueDetailsScreen`) to collect `viewModel.mentionsState` and pass
`members = mentionsState.members` into `CreateCommentBar`, and `members`/
`onMentionClick = goToProfile` into `WorkItemDescriptionWidget`
(`feature/workitem/ui/.../widgets/WorkItemDescriptionWidget.kt`, gained both params
forwarding to `ExpandableMarkdownText`) and `CommentsSectionWidget`
(`feature/workitem/ui/.../widgets/CommentsSectionWidget.kt`, gained both params
forwarding through its private `CommentItem` to `MarkdownTextWidget`) — this is what
makes Step 1's rendering mechanism visible in the running app for the first time, as
planned. `onMentionClick` needed no new callback: `goToProfile` was already threaded to
every one of these call sites (confirmed in Step 1's data-source note).

Verify: extended `CreateCommentBarTest.kt` (`uikit/src/jvmTest/`) with
`typingAtSignShowsSuggestionsAndSelectingOneInsertsMention` — types `"Hey @al"`,
confirms only the matching member's row exists (prefix filtering), taps it, confirms
the field now reads `"Hey @alice "`, sends, and asserts the trimmed `"Hey @alice"`
reaches `onButtonClick` — plus the two pre-existing tests (plain-text send, blank-send
no-op) still pass unmodified, confirming no regression to the no-`@` path. New
`WorkItemMentionsDelegateImplTest.kt` (`feature/workitem/ui/commonTest/`) covers initial
empty state, successful load, and failure-leaves-empty. `./gradlew jvmTest` (full
suite), `ktlintCheck` (whole repo — one `standard:property-wrapping`/
`argument-list-wrapping` fix needed in the new test, auto-fixed via
`ktlintJvmTestSourceSetFormat`; one `standard:class-signature` fix needed in the new
delegate files, auto-fixed via `ktlintCommonMainSourceSetFormat`), `koverXmlReport` +
`:koverVerify` (floor holds with no change needed), and
`:uikit:compileKotlinIosSimulatorArm64`/the 4 details-`ui` modules' iOS compiles all
green.

GUI-verified on `Medium_Phone_API_36.1` against the local Taiga instance: opened Epic
#1 ("User Authentication & Authorization"), typed `Hey @ad` into the comment bar —
the popup appeared showing only `admin` (the real project member matching the prefix,
confirming filtering against the loaded `members` list rather than a hardcoded/stale
set), tapped it, the field became `Hey @admin `, sent it, and the comment rendered as
"Hey **@admin**" with the mention underlined/styled distinctly from plain text —
tapping it navigated straight to admin's Profile screen. Also observed `admin` was
added to the Epic's watchers list as a side effect of the mention, confirming the
already-working server-side tagging mechanism (documented in the investigation doc)
fired from a mention composed entirely via the new picker, not just free-typed text.
This is the first real end-to-end verification of Step 1's rendering mechanism too, as
scoped. Next: Step 5 (wire autocomplete into the description editor for work items and
wiki) — depends on Steps 2 and 3 (already done); not gated on anything from this step.

## Step 5: Wire autocomplete into the description editor (work items and wiki)

Applied Step 4's `CreateCommentBar` mechanism to `WorkItemEditDescriptionScreen.kt`.
`EditDescriptionState.currentDescription`/`onDescriptionChange` moved from `String` to
`TextFieldValue` (needed for cursor position); `originalDescription` stays `String` and
the `shouldGoBackWithCurrentValue`/repository-write comparisons now read
`currentDescription.text`. `EditDescriptionViewModel` mixes in
`WorkItemMentionsDelegate by WorkItemMentionsDelegateImpl(usersRepository =
usersRepository)` (unchanged from Step 4) and calls `loadMembers()` in `init {}`, same
pattern as the 4 details ViewModels. `EditDescriptionContent` (made public, was
`private`, so a Compose UI test can call it directly with a hand-built
`EditDescriptionState` — matches `SettingsScreenContent`'s existing public-content-
composable convention rather than inventing a new one) gained the same
`isMentionPopupDismissed`/`findActiveMentionQuery`/`MentionSuggestionsPopup` wiring as
`CreateCommentBar`, anchored in a `Box` around the `BasicTextField` (switched from its
`String` overload to the `TextFieldValue` one — both are built into
`androidx.compose.foundation.text.BasicTextField` itself, no new `uikit` wrapper needed
unlike `CreateCommentBar`'s `HintTextField` overload).

No rendering-side work was needed: Step 4 already wired `members`/`onMentionClick` into
`WorkItemDescriptionWidget` at every details-screen call site, and this step only
touches the input-side editor, which has no markdown rendering of its own. Confirmed
`WorkItemEditsNavGraph.kt`'s single `entry<WorkItemEditDescriptionNavDestination>` needs
no new params — `members` is self-fetched via the ViewModel's delegate exactly as Step
4's `CreateCommentBar` call sites were, not threaded through the nav graph. Wiki page
description editing needed no separate implementation: `WorkItemEditDescriptionScreen`
is the single shared screen for both entity types (routed via `TaskIdentifier.WorkItem`/
`TaskIdentifier.Wiki`), so wiring `EditDescriptionViewModel` once covers both, confirmed
by GUI-verifying each path independently rather than assuming the wiki path for free.

**Deviation from the step's plan:** `feature/workitem/ui` had no `jvmTest` source set
before this step (only `commonTest`) — `CreateCommentBarTest`'s `runComposeUiTest`
pattern lives in `uikit`, which already had one. Added the same
`compose.desktop.uiTestJUnit4`/`compose.desktop.currentOs` jvmTest dependency block to
`feature/workitem/ui/build.gradle.kts` (matching `feature/settings/ui`'s existing
identical block, an established repo pattern, not new infra) to host
`EditDescriptionContentTest.kt`.

Verify: extended `EditDescriptionViewModelTest.kt` with a
`usersRepository`/`FakeUsersRepository` constructor param (added to every existing test
call) and one new test confirming `loadMembers()` populates `mentionsState` on init;
updated every `onDescriptionChange`/`currentDescription` reference for the
`TextFieldValue` change. New `EditDescriptionContentTest.kt`
(`feature/workitem/ui/src/jvmTest/`) mirrors `CreateCommentBarTest`'s mention test:
types `Hey @al`-equivalent, confirms prefix filtering (`alice` shown, `bob` not), taps
the suggestion, confirms the field reads `...@alice `. `./gradlew jvmTest` (full
suite), `ktlintCheck` (whole repo — one `standard:property-wrapping`/
`argument-list-wrapping` fix needed in the new jvmTest file, auto-fixed via
`ktlintJvmTestSourceSetFormat`), `koverXmlReport` + `:koverVerify` (floor holds with no
change needed), and `:feature:workitem:ui:compileKotlinIosSimulatorArm64` all green.

GUI-verified on `Medium_Phone_API_36.1` against the local Taiga instance, both paths
independently: (1) Epic #1's description — typed `@ad` after a preceding space, the
popup showed only `admin` (prefix match), tapped it, inserted `@admin `, saved, and the
rendered description showed the mention link, tapping it navigated to admin's Profile
screen — full input→render→navigate round trip through the real edit screen, not just
the widget in isolation as Step 4 verified. (2) The `home` wiki page's description
(reached via Wiki → All Pages → home → tapping the description) — typed `@us` after a
space, the popup showed `user1`/`user2`/`user3` (excluding `admin`, confirming real
project-member filtering, not a stale/hardcoded list), tapped `user2`, inserted
`@user2 ` correctly, then discarded (via the existing "discard changes?" dialog, which
fired identically for the wiki path) to leave the seed page's content clean. Epic #1's
description was left with the `@admin` mention from part (1) rather than reverted —
matches Step 4's own precedent of leaving its comment-bar test artifact
(`Hey @admin`) on the same Epic. **Friction (not re-added to `docs/frictions.md` since
it's already documented as a known gotcha in the `emulator-testing` skill itself):**
`adb shell input text` dropped characters typed immediately after a `MOVE_END` landing
mid-way through a wrapped multi-line `BasicTextField` — `KEYCODE_MOVE_END` moves to the
end of the current *visual* line, not the end of the whole field, so a tap that lands
mid-paragraph plus `MOVE_END` does not reach the field's true end; re-tapping directly
at the last visible line's end character fixed it. Next: Step 6 (replace
`MentionSuggestionsPopup` with an inline horizontally-scrollable row) — added after
this step closed, per the keyboard-dismiss-flicker investigation; not gated on
anything from this step.

## Step 6: Replace `MentionSuggestionsPopup` with an inline horizontally-scrollable row

Full context and rationale:
[docs/issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md](../../issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md)
and `IMPLEMENTATION_PLAN.md`'s "Input-side mechanism" decision note. Added a new
`MentionSuggestionsRow` component (`uikit/.../widgets/editor/MentionSuggestionsRow.kt`)
built on `LazyRow`, not `DropdownMenu`/`Popup` — renders inline in normal layout flow
instead of opening a separate focusable Android window, which is what caused the
keyboard dismiss/flicker being fixed. **Added alongside the existing
`MentionSuggestionsPopup.kt`, not in place of it** — deviates from the step's own
wording ("replace ... in place") but matches every earlier step's own pattern (Step 3
built the popup component with no call site yet; Step 4 wired it in). Both call sites
(`CreateCommentBar`, `WorkItemEditDescriptionScreen`) still construct
`MentionSuggestionsPopup` today; swapping them onto the row and deleting the old
component/test is entirely Step 7's job, per the checklist's own "once nothing
references the old component, delete it" instruction — deleting the old file in this
step would have broken both call sites' compilation with no replacement wired in yet.

Kept the `members`/`onSelect` param shape; dropped `expanded` and `onDismissRequest`
entirely, confirming (not assuming) the plan's prediction — there's no `Popup`-driven
dismiss for a `LazyRow` to fight, so visibility is left entirely to the caller
choosing whether to compose the row at all (Step 7's job), with an internal
`if (members.isNotEmpty())` guard as a second line of defense matching the "empty
members renders nothing" verify requirement. Reused the existing 40dp circular avatar
treatment. Chip content dropped the full name (kept avatar + username only) to fit a
horizontally-scrollable chip shape — the UI call the plan flagged as undecided.
Reused `dialogTonalElevation` for the row's background, matching the old popup's own
surface treatment. `MENTION_SUGGESTION_ROW_TEST_TAG` (used by the still-live
`MentionSuggestionsPopupTest.kt`) was already taken in the same package, so the new
chip's test tag is `MENTION_SUGGESTION_CHIP_TEST_TAG` — both will coexist until Step 7
deletes the old file, at which point the old constant goes with it.

Verify: new `uikit` `jvmTest` (`MentionSuggestionsRowTest.kt`, same `runComposeUiTest`
pattern as `MentionSuggestionsPopupTest.kt`) covers: two members render two chips
(counted via `onAllNodesWithTag(MENTION_SUGGESTION_CHIP_TEST_TAG)`, since chip text no
longer includes the full name to assert against directly), tapping a chip invokes
`onSelect` with that member, and an empty `members` list renders zero chips — all 3
pass. `./gradlew jvmTest` (full suite), `:uikit:ktlintCheck`, `koverXmlReport` +
`:koverVerify` (floor holds — `uikit` isn't aggregated into coverage at all, so this
step's new code has no floor impact either way), and
`:uikit:compileKotlinIosSimulatorArm64` all green. No emulator check — this component
has no call site yet, same as Step 3. Next: Step 7 (rewire both call sites onto the
row, delete the old popup component and its test) — depends on this step, now done.

## Step 7: Rewire both call sites onto the row

Swapped both call sites from `MentionSuggestionsPopup` to `MentionSuggestionsRow`.
`CreateCommentBar.kt`: the `Box`-anchored overlay layout became a `Column` —
`HintTextField`+`IconButton` in an inner `Row` (unchanged relative to each other,
`HintTextField` now takes `weight(1f)` directly instead of via a wrapping `Box`), then
`MentionSuggestionsRow` below it in normal layout flow, so it renders as a fixed strip
under the whole input row rather than floating over the text field. Same restructure in
`WorkItemEditDescriptionScreen.kt`'s `EditDescriptionContent`: outer `Box` became a
`Column`, `BasicTextField` took `weight(1f)` + `fillMaxWidth()` (previously
`fillMaxSize()` inside the `Box`) so it still fills the available space above the row,
which sits below it.

**Confirmed, not assumed, per the plan's own instruction: `isMentionPopupDismissed`
was dropped entirely at both call sites**, along with its `onValueChange`-driven reset.
Visibility is now the pure derivation `activeMentionQuery != null &&
mentionSuggestions.isNotEmpty()` was predicted to become — in practice just passing
`mentionSuggestions` straight to `MentionSuggestionsRow`, since the `remember` block
already returns an empty list whenever `activeMentionQuery` is null and the row itself
no-ops on an empty list (Step 6). No `DropdownMenu`-style dismiss-fighting appeared, as
the plan expected for a non-`Popup` row.

Deleted `MentionSuggestionsPopup.kt` and `MentionSuggestionsPopupTest.kt` outright
(confirmed via `grep -rn MentionSuggestionsPopup` empty first) — this was the full
replacement Step 6 deferred, not a second implementation kept alongside the row.
`MENTION_SUGGESTION_ROW_TEST_TAG` (defined in the deleted popup file, already unused
elsewhere) went with it.

Verify: `CreateCommentBarTest.kt` and `EditDescriptionContentTest.kt` needed no changes
— both already asserted mention behavior via `onNodeWithText`/`onNodeWithTag` rather
than anything `DropdownMenu`-specific, so they exercise the row unchanged and still
pass. `./gradlew jvmTest` (full suite), `ktlintCheck` (whole repo), and
`koverXmlReport` + `:koverVerify` (floor holds, no change needed) all green.

GUI-verified on `Medium_Phone_API_36.1` against the local Taiga instance, using real
on-screen-keyboard taps (not `adb shell input text`) per the step's own instruction —
navigating the IME's symbols page to tap `@` rather than synthetic text injection, since
that's the specific gap that missed the keyboard-dismiss bug this replacement fixes.
(1) Epic #1's comment bar: tapped the field, typed `@` via real keyboard taps — the
chip row appeared inline directly below the input (not overlapping it), the on-screen
keyboard stayed open and visible the entire time, and tapping the `user1` chip inserted
`@user1 ` into the field correctly. Cleared the field afterward. (2) User story #17's
"Edit description" screen, reached after navigating away to Dashboard and back through
Kanban (satisfying the "re-test after normal in-between usage" rule rather than only a
fresh-load check) — tapped into the description field, typed `@` via real keyboard taps
at the end of the existing text, the chip row appeared inline above the keyboard,
keyboard stayed open throughout, tapping `user1` inserted `@user1 ` correctly. Discarded
the edit via the existing "discard changes?" dialog to leave the seed data clean. No
flicker or keyboard dismiss observed on either screen — the specific regression this
replacement exists to fix did not reproduce.

Deviation from the step's plan: none. Next: Step 8 (full-suite verification and
polish) — depends on this step, now done.

## Step 8: Full-suite verification and polish

Ran the four required commands in sequence: `./gradlew jvmTest` (full suite, BUILD
SUCCESSFUL), `./gradlew ktlintCheck` (whole repo, BUILD SUCCESSFUL — no
`standard:function-signature`/`standard:class-signature` fixes needed, confirming
steps 1-7's multi-param composables didn't trip either trap on a full-repo pass),
and `./gradlew koverXmlReport :koverVerify` (BUILD SUCCESSFUL — floor holds with no
change needed).

Rebuilt and reinstalled the fdroid debug APK (`./gradlew :androidApp:assembleFdroidDebug`
+ `adb install -r`) rather than reusing whatever build was already on the AVD, so the
GUI pass exercised steps 5-7's changes rather than a stale binary. Booted
`Medium_Phone_API_36.1`; the app was already logged in from a prior session (confirmed
persistent-login gotcha in `docs/EMULATOR_TESTING.md`), landing on Dashboard.
Navigated Dashboard → Kanban → Epics → Epic #1 before touching either mention surface,
satisfying the "not immediately after a fresh app launch" requirement.

**Comment bar (Epic #1):** typed `Hey ` via `input text`, then a real on-screen-keyboard
tap on `@` (symbols-page coordinates from `docs/EMULATOR_TESTING.md`) — the suggestion
row appeared inline below the field showing all 3 members, keyboard stayed open
throughout. Tapped `user1`, confirmed `Hey @user1 ` inserted with trailing space, sent
it, expanded the Comments section and confirmed the new comment rendered as "Hey
**@user1**" with the mention styled/underlined, alongside the pre-existing "Hey @admin"
artifact from Step 4's own verification.

**Description editor (Epic #1's description):** opened via tapping the description
text (which incidentally became the Step 5 GUI-check re-run). Positioned the cursor at
the field's true end (hit the same `MOVE_END`-stops-at-visual-line-end gotcha
documented from Step 5 — re-tapped directly at the last visible character to land
correctly), typed a space, tapped `@` via the real keyboard, confirmed the row showed
all 3 members unfiltered on an empty query, typed `us` and confirmed it filtered to
exactly `user1`/`user2`/`user3` (excluding `admin` — real prefix filtering, not a
static list), tapped `user2`, confirmed `@user2 ` spliced in correctly with the
keyboard still open. Discarded via the existing "discard changes?" dialog (re-dumped
bounds fresh right before tapping **Discard**, per the emulator-testing skill's
destructive-dialog rule) to leave the description unchanged from Step 5's own state.

No flicker or keyboard dismissal observed on either surface, and no new gotcha was hit
beyond the ones already recorded in `docs/EMULATOR_TESTING.md` (real-keyboard `@`
symbols-page tap, `MOVE_END` visual-line-end trap) — both were simply re-confirmed, not
new findings, so no doc update was needed there.

**Friction (not project-specific, already documented — not re-added to
`docs/frictions.md`):** hit the `emulator-testing` skill's own documented
"ollama-relay threshold" Read-tool gate (Step 1: "Reading a raw screenshot with the
Read tool can fail with 'over the N-line ollama-relay threshold'") several times on
screenshots of the mention UI. First occurrence read as a possible prompt injection
(a skill name not in this session's available-skills list) and was flagged to
gregory as such before realizing the skill itself already documents this exact
behavior and workaround — re-encoding at lower JPEG quality or cropping to the region
of interest, both of which worked as documented. No new finding; confirms the
skill's existing guidance is accurate and worth following on the first hit rather
than suspecting injection.

Deviation from the step's plan: none — all four verification commands green, GUI pass
covered both mention surfaces after normal in-between navigation as required. This was
the initiative's last step; queue is empty.
