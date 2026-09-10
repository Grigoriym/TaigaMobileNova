# @-Mention Tagging Support — Checklist

**Progress:** 5/8 done. **Current step:** 6 (replace the popup with an inline
horizontally-scrollable row) — no unfinished dependency, approved by gregory
2026-09-10.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice — including
its "Team-member data source" section (added 2026-09-10) and the row-replacement
decision recorded at the end of "Input-side mechanism" (added 2026-09-10). Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).
Steps 1-5 are done — see [CHECKLIST-DONE.md](CHECKLIST-DONE.md). Steps 6-7 replace
the `DropdownMenu`-based popup built in steps 3-4 outright, per
[docs/issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md](../../issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md)'s
investigation and decision — not a second implementation kept alongside the first.

## Step 6: Replace `MentionSuggestionsPopup` with an inline horizontally-scrollable row

Full context and rationale:
[docs/issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md](../../issues/2026-09-10-mention-popup-keyboard-dismiss-flicker.md)
and `IMPLEMENTATION_PLAN.md`'s "Input-side mechanism" decision note. Replace
`uikit/.../widgets/editor/MentionSuggestionsPopup.kt` in place — built on `LazyRow`,
not `DropdownMenu`/`Popup`, so it renders inline in normal layout flow instead of a
separate focusable Android window (that's what causes the keyboard dismiss/flicker
being fixed). Keep the `members`/`onSelect` param shape; `onDismissRequest` most
likely goes away entirely since there's no `Popup`-driven dismiss to fight — confirm
rather than assume. Reuse the existing 40dp circular avatar treatment from the
current `MentionSuggestionRow`; exact chip content (avatar only vs. avatar+username,
full name likely dropped for space) is a UI call to make here, not pre-decided.

**Verify:** new `uikit` `jvmTest` (mirroring `MentionSuggestionsPopupTest.kt`'s
`runComposeUiTest` pattern, in the same file or a replacement for it) covering: the
row renders one chip per filtered member, tapping a chip invokes `onSelect`, and an
empty `members` list renders nothing.

## Step 7: Rewire both call sites onto the row

Swap the row in at both existing call sites — `CreateCommentBar`
(`uikit/.../widgets/CreateCommentBar.kt`) and `EditDescriptionContent`
(`feature/workitem/ui/.../screens/editdescription/WorkItemEditDescriptionScreen.kt`)
— replacing `MentionSuggestionsPopup(...)`. Per the decision note, try dropping
`isMentionPopupDismissed` and its `onValueChange`-driven reset at both sites first,
going back to `expanded`/visibility as a pure function of
`activeMentionQuery != null && mentionSuggestions.isNotEmpty()` (this is what
Step 4 originally tried and had to abandon specifically because of `DropdownMenu`'s
own dismiss semantics — confirm those don't apply to the row before removing the
flag, rather than assuming). Once nothing references the old component
(`grep -rn MentionSuggestionsPopup` empty), delete `MentionSuggestionsPopup.kt` and
`MentionSuggestionsPopupTest.kt` — this is a full replacement, not a second
implementation kept alongside the first.

**Verify:** extend `CreateCommentBarTest.kt` and `EditDescriptionContentTest.kt`
(both already have a mention-typing test from Steps 4/5) to assert against the row
instead of the dropdown — same behavior: type a partial `@` query, see the filtered
suggestion(s), tap one, confirm the insertion. GUI-verify on the emulator using a
**real on-screen-keyboard tap sequence, not `adb shell input text`** — Step 5's GUI
check used synthetic text injection and screenshotted only after each string landed,
which is why it never caught the keyboard-dismiss/flicker in the first place (see the
investigation doc's Open Questions). Confirm the keyboard stays open and the row
doesn't flicker when `@` is typed, on both the comment bar and the description
editor.

## Step 8: Full-suite verification and polish

Run `./gradlew jvmTest` (full suite, not just the touched modules — per CLAUDE.md,
a coroutine leak in one module can fail an unrelated test), `ktlintCheck`,
`koverXmlReport` + `:koverVerify` (this initiative adds real production code paths —
confirm the floor still holds without needing to raise it; if it doesn't, add tests
rather than lower the floor). Re-run the Step 1/4/5/7 GUI checks once more after
normal in-between app usage (per CLAUDE.md's Verification rule about behavior that
depends on prior interaction — don't rely solely on the first-load checks already
done in earlier steps). Confirm ktlint's `standard:function-signature`/
`standard:class-signature` traps (CLAUDE.md Testing section) didn't bite any of the
new multi-param composables added across steps 1-7.

**Verify:** all of the above commands green; a manual emulator pass exercising both
comment and description mentions after navigating through at least one unrelated
screen first, not immediately after a fresh app launch.
