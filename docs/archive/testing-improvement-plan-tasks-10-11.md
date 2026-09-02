# Testing improvement plan — tasks 10–11 (archived)

Full write-ups for the completed tasks of [../testing/improvement-plan.md](../testing/improvement-plan.md),
moved here 2026-08-07 once both were done — kept for their reasoning (the Compose UI test spike
findings, the per-widget Result notes, the wiring gotchas), not needed for day-to-day work on the
live tasks (12+). The status table in the live doc still lists 10 and 11 with their dates; this file
is where the detail behind each row lives now.

No anchors here are linked to from outside this file as of the move — `compose-ui-test-spike.md`
holds the durable how-to content (build-script wiring, `assertExists`/`assertDoesNotExist`,
`jvmTest` vs `commonTest`) and was not moved; it stays in `docs/testing/` since it is still the
first read for the next Compose UI test, feature-level or otherwise.

---

## Task 10 — Compose UI test spike (one uikit widget)

**Why:** nothing in the repo tests a single Composable. Screens, uikit widgets and navigation wiring
have no automated verification at all. This task is a **spike**: prove the wiring works on one
widget, then decide whether to expand.

**Scope — deliberately tiny:**

- Add `runComposeUiTest` support to `:uikit` (`compose.uiTest` dependency in `commonTest`; on JVM it
  needs the desktop test artifact).
- Write **one** test for **one** simple, stateful uikit widget.
- Write down what the wiring took, in `docs/testing/`.

**Watch for:** `:uikit` is currently excluded from Kover aggregation, and per Task 1, non-aggregated
modules do not run in CI via the Kover step. Confirm the new test actually runs in CI — if Task 1's
`jvmTest` step is in place it will, but verify rather than assume. Re-including `:uikit` in Kover
aggregation is a reasonable follow-up but is **not** part of this task.

**Done when:** one Compose test passes via `./gradlew :uikit:jvmTest`, runs in CI, and the setup is
documented well enough that the next widget test needs no research.

**Finalize focus:** high. The output of a spike is knowledge, not the test. If the wiring turns out
to be painful or flaky, **say so** and recommend against expanding — a negative result is a
successful spike.

**Result (2026-08-06):** done, and it's a positive spike — recommend expanding incrementally. Wired
`compose.dependencies.uiTest` + the desktop `uiTestJUnit4`/`currentOs` artifacts into
`uikit/build.gradle.kts` (JVM/desktop only — no `iosTest`/`androidUnitTest` source sets exist here,
and CI only runs `jvmTest`), and wrote `CreateCommentBarTest`
(`uikit/src/jvmTest/kotlin/.../widgets/CreateCommentBarTest.kt`): two tests against
`CreateCommentBar`, chosen because it owns real `rememberSaveable` state that changes through
interaction rather than for being the simplest widget available. Needed two `Modifier.testTag`
additions in `CreateCommentBar.kt` itself, since its send button's icon has
`contentDescription = null`. Full write-up — the build-script accessor gotcha (`compose.dependencies`
resolves at top level but not inside a nested `dependencies {}` block), the
`assertExists`/`assertDoesNotExist` member-vs-import trap, why `jvmTest` over `commonTest` — is in
[compose-ui-test-spike.md](../testing/compose-ui-test-spike.md), written so the next widget test needs none of
this re-derived. Confirmed the new test runs under the root `./gradlew jvmTest` aggregate task (what
CI invokes before `koverXmlReport`), so no CI change was needed. `jvmTest`, `ktlintCheck` and
`detekt` are all green; `:uikit` stays outside Kover aggregation so `:koverVerify` is unaffected.

---

## Task 11 — Compose UI test sweep, one uikit widget per session

**Why:** task 10 proved the `runComposeUiTest` wiring on one widget; this task spends it. Same shape
as the 9a/9c sweeps — repeatable, one widget (or occasionally a tight pair) per session, following
the priority order below unless scoping the session finds something better.

**Scope:** `uikit` `commonMain` Composables only, JVM/desktop `jvmTest`, same pattern as task 10 —
prefer widgets that own real interactive state over ones that only forward an `onClick`. Add
`Modifier.testTag(...)` (+ a public `const val ..._TEST_TAG`) wherever a widget's interactive element
has no unique text/content-description semantics, same as `CreateCommentBar`'s send button.

**Candidates, in priority order (scoped 2026-08-06):**

1. **`DropdownSelector`** (`uikit/src/commonMain/.../widgets/DropdownSelector.kt`) — generic `<T>`,
   owns real `isExpanded` state (`remember { mutableStateOf(false) }`). Test: click to open, click an
   item, assert `onItemSelect` fired with the right value and the menu closed. Needs a concrete `T`
   in the test (e.g. `String`) and `itemContent`/`selectedItemContent` lambdas that render
   distinguishable text.
2. **`ConfirmActionDialog`** (`uikit/src/commonMain/.../widgets/dialog/ConfirmActionDialog.kt`) — no
   owned state, but real confirm/cancel button wiring worth verifying directly rather than trusting
   it by inspection. Check its actual parameter names before writing the test.
3. **`ExpandableMarkdownText`** (`uikit/src/commonMain/.../widgets/text/ExpandableMarkdownText.kt`) —
   real `isExpanded` toggle, but gated on `naturalHeight > maxHeight` computed from a real
   `onSizeChanged` layout pass. **Unknown risk, flag it explicitly in this session's Result note
   either way**: the desktop `runComposeUiTest` backend may not lay out to a real pixel size by
   default, in which case `naturalHeight` could stay `0.dp` and the "show more" button never
   appears. If so, this either needs an explicit test window size (check
   `runComposeUiTest`'s `effectContext`/size parameters — task 10 didn't need any) or gets written up
   as a real gap and deferred, not silently skipped.
4. **`SectionTitle`** — has `onAddClick`, but no owned state beyond the arrow-rotation animation.
   Lowest priority of the four; only the click-callback wiring is worth asserting.

**Deferred separately — not part of this sweep, don't pick them up under task 11:**

- `MultiColumnDragDrop` — gesture-based drag & drop. Testing a drag sequence
  (`performTouchInput`/`performMouseInput` with move/up) is a different shape of test than a click/type
  interaction and deserves its own scoping, not a same-priority-list entry.
- `DatePickerDialogWidget` — thin wrapper over Material3's own `DatePickerDialog`; a test here would
  mostly be re-testing M3's component, not this repo's code. Low value.

**Done when (per widget):** the widget's test passes via `./gradlew :uikit:jvmTest`, and the status
table + this task's own section gets a dated note recording what happened — including if a candidate
turned out not to work (see the `ExpandableMarkdownText` risk above), same as 9a records
closed-as-blocked modules rather than silently dropping them.

**Finalize focus:** medium. Mostly execution of an already-validated pattern; only worth a deeper
harvest if a candidate surfaces a *new* wiring gotcha (like `ExpandableMarkdownText`'s layout risk
might).

**Ungated** — per gregory's 2026-08-06 decision (see the scope note in the live doc), take the next
candidate without asking.

**Result (2026-08-07):** `DropdownSelector` done. Wrote `DropdownSelectorTest.kt`
(`uikit/src/jvmTest/kotlin/.../widgets/DropdownSelectorTest.kt`), two tests: opening the menu via
click and selecting an item invokes `onItemSelect` with the right value and closes the menu; and,
since the click-to-open `Modifier.clickable` is only attached when `canModify && !isOffline`,
`canModify = false` leaves the row inert and the menu never opens. Needed one `testTag` addition —
`DROPDOWN_SELECTOR_ROW_TEST_TAG` on the header `Row` — because `selectedItemContent` is caller-supplied
and has no fixed text/description to select on generically; items themselves didn't need tags since
the test's own `itemContent = { Text(it) }` gives each a distinguishable string. No new gotchas beyond
what task 10 already documented — `./gradlew :uikit:jvmTest`, `ktlintCheck`, `detekt`, and the full
`./gradlew jvmTest` are all green.

**Result (2026-08-07):** `ConfirmActionDialog` done. Wrote `ConfirmActionDialogTest.kt`
(`uikit/src/jvmTest/kotlin/.../widgets/dialog/ConfirmActionDialogTest.kt`), three tests: confirming
invokes `onConfirm` and not `onDismiss`, dismissing invokes `onDismiss` and not `onConfirm`, and
`isVisible = false` renders nothing (asserted via `onNodeWithText(title).assertDoesNotExist()`). No
`testTag` needed — the confirm/dismiss buttons and title/description are plain `Text`, and the test
passes `NativeText.Simple(...)` for the button text instead of the default `RString.yes`/`RString.no`
resources to get a known string to assert on without a `StringResource`-resolving test environment.
Noticed in passing (not acted on): `runComposeUiTest` itself is deprecated in favor of a `v2`
overload — see the deprecation note in the live doc, which stayed there since it affects every widget
test, not just this one. `./gradlew :uikit:jvmTest`, `ktlintCheck`, `detekt`
and the full `./gradlew jvmTest` are all green.

**Result (2026-08-07):** `ExpandableMarkdownText` done — the flagged layout risk was real but
recoverable, not a blocker. Wrote `ExpandableMarkdownTextTest.kt`
(`uikit/src/jvmTest/kotlin/.../widgets/text/ExpandableMarkdownTextTest.kt`), two tests: short text
never shows the expand button, and long text (30 short paragraphs, `maxLinesCollapsed = 6`) shows
"Show more", clicking it swaps to "Show less" and reveals the rest. No `testTag` needed — the button
text (`stringResource(RString.show_more)`/`show_less`) resolves to real strings in this JVM test
environment (unlike `ConfirmActionDialogTest`'s workaround, `stringResource` just worked here, so that
workaround isn't a universal requirement — worth trying plain `stringResource` first on future
widgets before reaching for `NativeText.Simple`).

The real finding: `naturalHeight` (set via `onSizeChanged` on the actual desktop `runComposeUiTest`
backend, confirming it *does* lay out to a real pixel size, resolving the risk flagged in this task's
own scope note) updates on a **later frame** than the one `setContent` settles on, so a single
`waitForIdle()` after `setContent` was not reliably enough to observe the button appearing — it failed
consistently when this test ran second in the class (after `shortTextNeverShowsExpandButton`) and
passed when run alone, i.e. genuinely order/timing-dependent, not a one-off flake. Diagnosed by
temporarily inserting `onRoot().printToLog(...)` before the assertion, which incidentally made the
test pass — a clue that *something* extra was needed, not that logging itself was the fix. Replaced
with `waitUntil { onAllNodesWithText("Show more").fetchSemanticsNodes().isNotEmpty() }`, which polls
across frames until the condition holds; that made it pass deterministically across repeated reruns
and regardless of declaration/execution order. **Lesson for future widgets whose visible state depends
on a layout callback (`onSizeChanged`, `onGloballyPositioned`, etc. — not just a plain
`remember { mutableStateOf(...) }` toggle): use `waitUntil { ... }` on the expected semantics rather
than `waitForIdle()`.** `./gradlew :uikit:jvmTest`, `ktlintCheck`, `detekt` and the full
`./gradlew jvmTest` are all green.

**Result (2026-08-07):** `SectionTitle` done — the last candidate on the list, so task 11 is now
closed. Wrote `SectionTitleTest.kt` (`uikit/src/jvmTest/kotlin/.../widgets/text/SectionTitleTest.kt`),
covering both composables in the file: `SectionTitle`'s add button (click invokes `onAddClick`;
`onAddClick = null` hides the button entirely) and `SectionTitleExpandable`'s click-through-text
wiring (clicking the title text invokes `onExpandClick`, since the whole `Surface` is clickable and
pointer dispatch hits it regardless of which child is under the tap point — no need to target the
`Surface` itself). One `testTag` addition — `SECTION_TITLE_ADD_BUTTON_TEST_TAG` on the add button's
`Box` — same reason as `DropdownSelector`'s row tag: the icon has `contentDescription = null` and no
other unique semantics. The arrow-rotation animation on `SectionTitleExpandable` was deliberately left
unasserted, per the task's own priority note ("only the click-callback wiring is worth asserting").
`./gradlew :uikit:jvmTest`, `ktlintCheck`, `detekt` and the full `./gradlew jvmTest` are all green —
`FiltersStorageImplTest` failed once inside the full suite and passed both alone and on a clean-tree
re-run, confirmed pre-existing cross-module flakiness unrelated to this change (not a new one, and not
investigated further here).

**Task 11 is now fully done — all four candidates on the priority list are closed.**
