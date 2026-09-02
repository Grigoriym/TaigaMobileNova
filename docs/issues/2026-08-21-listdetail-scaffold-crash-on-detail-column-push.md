# 2026-08-21 — Crash pushing a new screen into the ListDetailSceneStrategy detail pane

**Status:** Open — blocks step 12c
**Link:** none — found while doing step 12c's manual verification, see
[tablet-form-factor-support/CHECKLIST.md](../architecture/tablet-form-factor-support/CHECKLIST.md)
**Updated:** 2026-08-21

## Report

Not a user report. Step 12c's manual verification pass on the `Medium_Tablet` AVD (1280dp-wide,
two-pane `ListDetailSceneStrategy` layout for Issues, wired in step 12b) crashed the app the moment
the promote-to-user-story action pushed `UserStoryDetailsNavDestination` into the detail pane
alongside the still-composed Issues list pane — exactly the "Kanban/Issues[list] → Issue[detail] →
UserStory[pushed in the detail column]" scenario step 12's `ResultBus` fix (step 12a) targeted.

```
androidx.compose.runtime.ComposeRuntimeError: Compose Runtime internal error. Unexpected or
incorrect use of the Compose internal runtime API (Missed recording an endGroup). Please report to
Google or use https://goo.gle/compose-feedback
	at androidx.compose.runtime.ComposerKt.composeImmediateRuntimeError(Composer.kt:1429)
	at androidx.compose.runtime.composer.gapbuffer.changelist.ComposerChangeListWriter.endCurrentGroup(ComposerChangeListWriter.kt:483)
	at androidx.compose.runtime.GapComposer.end(GapComposer.kt:1607)
	at androidx.compose.runtime.GapComposer.endRestartGroup(GapComposer.kt:2194)
	...
	at androidx.compose.material3.ScaffoldKt.ScaffoldLayout_FMILGgc$lambda$4$0(Scaffold.kt:163)
	...
	at androidx.compose.ui.layout.LayoutNodeSubcompositionsState.subcompose(SubcomposeLayout.kt:721)
	...
	at androidx.compose.material3.ScaffoldKt.ScaffoldLayout_FMILGgc$lambda$6$0(Scaffold.kt:274)
	at androidx.compose.ui.layout.LayoutNodeSubcompositionsState$createMeasurePolicy$1.measure-3p2s80s(SubcomposeLayout.kt:955)
	...
	at androidx.compose.ui.node.MeasureAndLayoutDelegate.forceMeasureTheSubtreeInternal(MeasureAndLayoutDelegate.kt:788)  [x6, nested]
	at androidx.compose.ui.node.MeasureAndLayoutDelegate.forceMeasureTheSubtree(MeasureAndLayoutDelegate.kt:745)
	at androidx.compose.ui.platform.AndroidComposeView.forceMeasureTheSubtree(AndroidComposeView.android.kt:1893)
	...
```

**Environment:** `Medium_Tablet` AVD (1280dp landscape, expanded width), fdroid debug build,
Compose Multiplatform / Material3 adaptive-navigation3 `1.3.0-beta02` (pinned in step 12b), local
Docker Taiga backend.

## Reproduction (100%, 2/2 attempts)

1. Two-pane Issues list-detail on `Medium_Tablet` (list pane left, detail pane right — confirmed
   rendering correctly beforehand, screenshot taken).
2. Open any Issue in the detail pane (list stays composed alongside it — this is the point of
   `ListDetailSceneStrategy`).
3. Overflow menu (⋮) → **Promote**. The API call succeeds (`POST
   /issues/{id}/promote_to_user_story` → 200, body `[<new ref>]`), the app fetches the new user
   story, and then crashes as it tries to push `UserStoryDetailsNavDestination` into the detail
   pane.

Reproduced identically on two separate freshly-seeded Issues (different ids/refs), same exact
stack trace both times — not a flaky/timing one-off.

## What it isn't

**Not reproducible on phone width.** Same build, same promote action, on `Medium_Phone_API_36.1`
(single-pane, full-screen push navigation instead of `ListDetailSceneStrategy`): promote succeeds
and lands cleanly on `User story #88` with no crash, no `FATAL EXCEPTION` in logcat. Back navigation
from there also correctly returns to the Issue detail showing "This issue has been promoted to
#88..." — confirming step 12a's `ResultBus` per-pairing key still delivers correctly in single-pane
mode (already covered by 12a's own regression check).

**Not app code.** The full crash stack (`grep -c "at com.grappim"` against the `FATAL EXCEPTION`
block) has zero frames in this codebase — every frame is `androidx.compose.runtime`,
`androidx.compose.material3.ScaffoldKt`, or `androidx.compose.ui.node`/`SubcomposeLayout`
internals. The only app-level trace nearby is a `StrictMode` `DiskReadViolation`/
`DiskWriteViolation` around `UserStoryDetailsViewModel.<init>` → `loadUserStory` — unrelated noise
(StrictMode disk-IO warnings, not what crashes), not folded into this writeup.

## Analysis

The crash fires inside `ScaffoldLayout`'s bottom-bar slot subcomposition
(`ScaffoldKt.ScaffoldLayout_FMILGgc$lambda$6$0`, `Scaffold.kt:274`,
`LayoutNodeSubcompositionsState.subcompose`), itself invoked from a **forced full-subtree remeasure**
(`MeasureAndLayoutDelegate.forceMeasureTheSubtreeInternal`, six frames deep — i.e. six nested
layout nodes forced to remeasure in the same pass) that originates from
`AndroidComposeView.forceMeasureTheSubtree`. The full stack traces all the way up through
`ViewRootImpl.performTraversals` — i.e. this is the **app's root composition** (`MainScreen`'s own
top-level `Scaffold`), not something scoped inside whichever screen we navigate to.

**Original theory (below) investigated 2026-08-21 session 2 and found false:** neither
`IssueDetailsScreen` nor `UserStoryDetailsScreen` wraps its content in a `Scaffold` at all — both are
plain `Column`/`Box(weight(1f))`/`CreateCommentBar` layouts (confirmed by reading both files in
full, and via `git log` that neither has changed since step 8). Kept below for the record since it
was the working theory this doc originally shipped with:

> ~~Both `IssueDetailsScreen` and `UserStoryDetailsScreen` wrap their content in their own Material3
> `Scaffold` with a `bottomBar` (the comment bar) — i.e. a `SubcomposeLayout`-backed `Scaffold`
> nested inside `ListDetailSceneStrategy`'s own `SubcomposeLayout`-backed detail-pane slot. When the
> detail pane's content identity changes (Issue → UserStory) while the list pane stays composed,
> something forces a synchronous full-subtree remeasure that recomposes the new screen's `Scaffold`
> bottom-bar slot mid-pass, and the Compose runtime's group-tracking bookkeeping ends up in an
> inconsistent state.~~

This shape — nested `SubcomposeLayout` plus a forced full-subtree remeasure — still matches a known
class of Compose runtime bug rather than a mistake in this codebase's navigation wiring, but *which*
nesting triggers it is now an open question again; see "Further investigation" below for what was
ruled out. Not yet confirmed against Compose's own issue tracker (searched 2026-08-21, no exact
match found — see below) or by bisecting the Compose BOM / `adaptive-navigation3` version pin.

## Further investigation (2026-08-21, session 2)

Four hypotheses tested and ruled out, in order:

1. **Switch to a different top-level nav section (Backlog) before pushing `UserStoryDetailsNavDestination`**,
   instead of pushing it onto the still-active Issues sub-stack — the idea being to keep it out of
   `ListDetailSceneStrategy`'s pane entirely. `IssueNavGraph.kt`'s `goToUserStory` was changed to
   `navigator.navigate(ScrumBacklogDestination)` then `navigator.navigateToUserStory(...)`. **Still
   crashed, identical stack trace, same 6-frame depth.** Reverted (not committed).
2. **Disable the `NavDisplay` crossfade transition** (`MainNavHost.kt`'s `transitionSpec`/
   `popTransitionSpec`, `fadeIn`/`fadeOut` → `EnterTransition.None`/`ExitTransition.None`), on the
   theory that `AnimatedContent`'s own forced-remeasure-for-transition-sizing was the trigger.
   **Still crashed, identical stack trace.** Reverted (not committed).
3. **Composable/subcomposition weight of the incoming screen.** Full read of
   `UserStoryDetailsScreen.kt` and the shared `feature/workitem/ui` widgets it composes (tags,
   badges, custom fields, attachments, comments, assignee/watcher rows): zero `LazyColumn`/
   `LazyRow`/`Pager`/`SubcomposeLayout`/`AnimatedContent`/`Crossfade` anywhere — only `FlowRow`
   (multi-content `Layout`, not subcompose-backed) and a plain `verticalScroll` `Column`.
   `IssueDetailsScreen` (already on screen when the crash fires) has the **identical** shape.
   `EditTagsNavDestination` — which does **not** crash when pushed from the same Issue detail pane —
   actually contains a real `LazyColumn` (`WorkItemEditTagsScreen.kt:133`), which *is*
   subcompose-backed. So the screen that crashes is the *lighter* one in this dimension; the theory
   points the wrong way.
4. **`ViewModel` `init`-block async I/O timing.** Both `UserStoryDetailsViewModel.loadUserStory()`
   and `WorkItemEditTagsViewModel.fetchTags()` are `viewModelScope.launch`-wrapped from `init` —
   same shape, no differentiator.

Also checked: `entry<UserStoryDetailsNavDestination>` (`UserStoryNavGraph.kt:22`) carries no
`ListDetailSceneStrategy` metadata of its own, ruling out a competing-detail-pane-tag theory.

**Net result: the crash is confirmed specific to `UserStoryDetailsNavDestination` as a navigation
target** (reproduces via both the Promote button and tapping an already-promoted issue's user-story
link — same `goToUserStory` callback), **not to "any push out of the two-pane Issues detail pane"**
(`EditTagsNavDestination` proves that), and not to any of the four candidate mechanisms above.
Nothing about *why* `UserStoryDetailsNavDestination` specifically triggers it has been found yet.

**Web research (2026-08-21):** no existing bug report found matching this exact crash on Google's
issue tracker or JetBrains' `compose-multiplatform` GitHub repo. `forceMeasureTheSubtree` is a
confirmed fragile area of the Compose runtime in general — JetBrains
[compose-multiplatform#1464](https://github.com/JetBrains/compose-multiplatform/issues/1464) hit a
different crash there (desktop window-DPI change, not navigation-related), closed with no public fix
detail. `ListDetailSceneStrategy` itself has a separately-reported animation gap
([nav3-recipes#212](https://github.com/android/nav3-recipes/issues/212): no transitions between
top-level `listPane()` destinations, no animation for in-pane content swaps) — not the same symptom
as this crash, but confirms the scene strategy's animation/transition handling is an active rough
edge upstream.

## Impact on step 12

This blocks step 12c's own verification for the tablet path: the Kanban/Issues[list] →
Issue[detail] → UserStory[pushed in the detail column] → back scenario that the whole step 12 body
of work (`ResultBus` fix, `ListDetailSceneStrategy` wiring) was built to support **cannot be reached
on tablet at all** — the app crashes before "back" is ever tested. Phone-width (single-pane) is
unaffected and its own regression path (12a) is independently confirmed. Two-pane list rendering
itself (list + empty detail pane, list + populated detail pane) works correctly on tablet — only the
detail-column push transition (a second screen going into the already-open detail pane) crashes.

## Options (status as of 2026-08-21, session 2)

1. **Isolate and file upstream** — build a minimal repro (bare `ListDetailSceneStrategy` +
   `NavDisplay` + two destinations) outside this app, confirm it crashes the same way, and file
   against Compose/Material3-adaptive. Slowest but the correct fix if this is a genuine Compose bug
   — no app-level workaround would be principled. **Not attempted yet** — remains the most promising
   path given four app-level mitigations already failed (see "Further investigation" above) and no
   existing upstream report was found. gregory is checking the navigation code directly before
   deciding whether to pursue this.
2. **Remove the nested `Scaffold` from one or both detail screens** — **ruled out, premise was
   false.** Neither detail screen has a `Scaffold` (confirmed above).
3. **Bump `adaptive-navigation3` / Compose Multiplatform** past `1.3.0-beta02` — **not available.**
   Checked Maven Central: `1.3.0-beta02` (current pin) is still the latest published release
   (`maven-metadata.xml` `lastUpdated` 2026-06-16). Only unversioned JetBrains Space dev/snapshot
   builds exist beyond it (`1.3.0-beta03+dev...`) — no confirmed fix, non-reproducible artifact,
   would need a new, non-standard repository added to `settings.gradle.kts`.
4. **Narrow step 12's MVP further** — ship two-pane Issues list-detail without the promote path
   reachable in two-pane mode (e.g. hide "Promote" when the detail pane is active), deferring a real
   fix. Still viable, fastest to close step 12c if 1 turns out to be a large investigation.

An alternative reframing was tried and did not pan out: navigating to the Backlog section instead of
pushing into the same pane (see "Further investigation" point 1) — this was a plausible product-level
fix (promoting conceptually moves an issue into the Backlog) but the crash is not actually caused by
the pane-nesting shape, so it didn't help. That two-call navigation change was reverted, not kept.
