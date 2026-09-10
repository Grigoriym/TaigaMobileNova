# @-Mention Tagging Support — Checklist

**Progress:** 4/6 done. **Current step:** 5 (wire autocomplete into the description
editor for work items and wiki) — depends on 2 and 3, both done.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice — including
its "Team-member data source" section (added 2026-09-10). Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).
Steps 1-4 are done — see [CHECKLIST-DONE.md](CHECKLIST-DONE.md).

Step 5 depends on 2 and 3 (both done). Step 6 depends on everything before it.

## Step 5: Wire autocomplete into the description editor (work items and wiki)

Same input-side mechanism as Step 4, applied to
`WorkItemEditDescriptionScreen.kt`'s `BasicTextField`/`EditDescriptionState.kt`/
`EditDescriptionViewModel.kt`, plus the same rendering-wiring finish (`members` +
`onMentionClick` into `WorkItemDescriptionWidget` at the description-display call
site, if Step 4 didn't already cover every entity type). This screen is shared by
`WikiPageScreen` via the `WorkItemDescriptionState`/delegate pattern (CLAUDE.md's
Navigation Pattern section) — confirm wiki pages get the popup too as a side effect,
not a separate implementation, and that Wiki's own ViewModel can reuse Step 4's
delegate (or needs its own instance — Wiki isn't one of the 4
`CreateCommentBar` call sites, so this may be the first time its ViewModel needs
`members` at all; check what it already has in scope before assuming reuse is free).

**Verify:** extend `EditDescriptionViewModelTest.kt` the same way as Step 4's
comment-bar test. GUI-verify on the emulator for both a work-item description and a
wiki page description (per CLAUDE.md's note that description input is shared code,
but the two screens are reached through different navigation paths — worth a real
click-through on each once, not just one and an assumption about the other).

## Step 6: Full-suite verification and polish

Run `./gradlew jvmTest` (full suite, not just the touched modules — per CLAUDE.md,
a coroutine leak in one module can fail an unrelated test), `ktlintCheck`,
`koverXmlReport` + `:koverVerify` (this initiative adds real production code paths —
confirm the floor still holds without needing to raise it; if it doesn't, add tests
rather than lower the floor). Re-run the Step 1/4/5 GUI checks once more after
normal in-between app usage (per CLAUDE.md's Verification rule about behavior that
depends on prior interaction — don't rely solely on the first-load checks already
done in earlier steps). Confirm ktlint's `standard:function-signature`/
`standard:class-signature` traps (CLAUDE.md Testing section) didn't bite any of the
new multi-param composables added across steps 1-5.

**Verify:** all of the above commands green; a manual emulator pass exercising both
comment and description mentions after navigating through at least one unrelated
screen first, not immediately after a fresh app launch.
