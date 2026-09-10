# @-Mention Tagging Support — Checklist

**Progress:** 3/6 done. **Current step:** 4 (team-members delegate, `CreateCommentBar`
autocomplete wiring, finish Step 1's rendering wiring) — depends on 2 and 3, both done.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for architecture, the server
contract this relies on, and the reasoning behind each mechanism choice — including
its new "Team-member data source" section (added 2026-09-10). Origin:
[docs/issues/414-mention-autocomplete-not-implemented.md](../../issues/414-mention-autocomplete-not-implemented.md),
approved by gregory 2026-09-09 (full support, not just the render-only minimal fix).
Steps 1-3 are done — see [CHECKLIST-DONE.md](CHECKLIST-DONE.md).

Steps 1-3 are independent building blocks (rendering, detection logic, popup
component) with no ordering dependency between them. Step 1 did not produce a
user-visible change by itself (see its CHECKLIST-DONE.md note) — that lands with
Step 4. Steps 4-5 depend on 2 and 3. Step 6 depends on everything before it.

## Step 4: Build the team-members delegate, wire autocomplete into `CreateCommentBar`, and finish Step 1's rendering wiring

This step now carries the data-plumbing work deferred from Step 1 (see its scope
note) — it's the biggest single step in this initiative. Consider whether it still
fits one session once Step 2/3 are done and this one actually starts; if not, split
the delegate build (part a) from the `CreateCommentBar` wiring (part b) into their
own checklist entries rather than pushing through — that's a normal mid-initiative
re-plan per CLAUDE.md, not a deviation to avoid.

1. **Build the shared delegate** (name TBD at implementation time, e.g.
   `WorkItemMentionsDelegate`/`WorkItemMentionsDelegateImpl`), mirroring the existing
   `feature/workitem/ui/.../delegates/{comments,description,assignee}` pattern:
   exposes the current project's team members as
   `StateFlow<ImmutableList<TeamMember>>`, backed by `usersRepository.getTeamMembers()`
   (the no-project-id-arg overload the assignee picker already uses — confirm it
   really does resolve "current project" from ambient session state before reusing
   it, don't assume from the name alone). Mix it into whichever of
   Task/UserStory/Epic/Issue's details ViewModels don't already have equivalent data
   in scope.
2. **Wire `CreateCommentBar` autocomplete.** Convert its local comment state from
   `String` to `TextFieldValue`. On every edit, run Step 2's
   `findActiveMentionQuery`; when non-null, filter the new `members` param by prefix
   and show Step 3's popup; on selection, apply `insertMention` and hide the popup.
   Update the 4 call sites (`UserStoryDetailsScreen`, `TaskDetailsScreen`,
   `EpicDetailsScreen`, `IssueDetailsScreen`) to pass `members` from the new
   delegate. Confirm the popup is simply absent while `isOffline` (field already
   disables under that condition — no separate offline branch expected, confirm
   during this step rather than assume).
3. **Finish Step 1's rendering wiring**, now that `members` exists for real: pass it
   (plus `onMentionClick = goToProfile` — already threaded to every one of these
   call sites today, confirmed 2026-09-10, no new callback needed) into
   `WorkItemDescriptionWidget`/`CommentsSectionWidget` at each of the same 4 details
   screens. This is what actually makes Step 1's mechanism visible in the running
   app for the first time.

**Verify:** update/extend `CreateCommentBarTest.kt` (`uikit/src/jvmTest/`) — typing
`@` shows filtered suggestions, selecting one inserts `@username `, and the final
submitted comment string is exactly what one existing test already asserts today
(no regression to the plain-text path when no `@` is typed). Then GUI-verify on the
emulator: type `@`, see the popup, pick a member, send the comment, confirm it
renders as a link (first real end-to-end check of Step 1's mechanism too).

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
