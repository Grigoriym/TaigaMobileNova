# ViewModel Lifecycle & Error-Handling Hardening — Checklist

**Progress:** 6/7 done. **Current step:** none active — step 7 is ungated and available. Step 8 is
new (found while doing step 6) and ungated — it's the same one-line fix pattern step 4 already used,
just at three more call sites.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source articles, the codebase
evidence behind each finding, and the findings that were assessed and declined. See
[CHECKLIST-DONE.md](CHECKLIST-DONE.md) for ticked steps.

7. Investigate: UiState-leak and derived-property convention
   - Not gated — this is an investigation step, not a commitment to change anything project-wide.
   - Claim to check: "Sealed State vs. Data State" (finding 6 already validated its headline
     data-class-over-sealed conclusion) also argues (a) a State field is a "UI-decision leak" if it
     encodes a rendering decision rather than raw data, regardless of sealed vs. data class, and
     (b) UI-only derived booleans should be `get()` properties on the State class, not stored fields
     or logic duplicated inline in Composables/mappers.
   - Approach: static grep over `*State.kt` classes for rendering-decision-shaped field names
     (`isEmpty`, `showX`, `xVisible`) that are stored fields rather than `get()` properties, and for
     existing `get()` properties to see whether the convention is already partially followed. Only
     dig into live behavior/tests for whatever candidates the grep turns up.
   - Not started — see IMPLEMENTATION_PLAN.md's "UiState-leak and derived-property convention"
     section.

8. Extend step 4's button-gating fix to the delegates step 6 found doing the same thing
   - Not gated — same one-line fix pattern step 4 already used (`isOffline = isOffline ||
     state.areXxxLoading`) validated by gregory, just applied to three more sites. Not a design
     question.
   - Confirmed real in IMPLEMENTATION_PLAN.md finding 10 (step 6's investigation): every case below
     already has an `areXxxLoading`/`isXxxLoading` flag in state, already rendered as a spinner, but
     the button(s)/icon(s) that fire the write are gated only by `isOffline`:
     - `WorkItemWatchersDelegateImpl.handleRemoveWatcher` — the per-watcher remove icon in
       `WatchersWidget` (via `TeamUserWithActionWidget`, `TeamUserWidget.kt:99-107`) — step 4 fixed
       only the watch/unwatch toggle button, not this. Same `_watchersState` step 4 touched.
     - `WorkItemSingleAssigneeDelegateImpl` / `WorkItemMultipleAssigneesDelegateImpl` — the
       Assign-to-me/Unassign toggle (`AssignedToWidget.kt:160-171`) and the per-assignee remove icon
       (same `TeamUserWithActionWidget`).
     - `WorkItemTagsDelegateImpl.handleTagRemove` — each tag chip's remove click (`TagItemWidget`,
       used from `WorkItemTagsWidget.kt`).
   - Fix: same shape as step 4 at each site — `isOffline = isOffline || <state>.areXxxLoading` on the
     relevant button/icon's `enabled`/`isOffline` param. Four screens share `WatchersWidget`
     (Task/UserStory/Epic/Issue details); `AssignedToWidget` and `WorkItemTagsWidget` are likely
     shared the same way — check call sites before assuming the count.
   - Verify: `./gradlew jvmTest` and `ktlintCheck`; per CLAUDE.md's Verification rule this is
     UI-visible — attempt a live check on device/emulator, but this session's `xdotool` clicks against
     the desktop build were unreliable (`docs/frictions.md`, 2026-08-29) enough that code-read +
     `jvmTest` may be the practical fallback again.

Findings assessed and declined — see IMPLEMENTATION_PLAN.md for detail, no further action:
- Concurrent independent loads fired from `init` (the "Startup-Intent" article's flaky-test claim)
  — confirmed 8 ViewModels do this, but the current test convention (final `.state.value` snapshot
  assertions + `MainDispatcherRule`'s unconfined dispatcher) already sidesteps the flakiness the
  article describes.
- `AppInfoProvider.isDebug()` runtime facade (the "debug code in release builds" article) — matches
  the flagged anti-pattern, but the article's fix (Android build-type source sets) doesn't
  generalize across this project's iOS/JVM targets.
- State design (data class, not sealed) — already matches the article's recommended convention.
- The Startup-Intent pattern, the Startup Task multibinding pattern, "MVP vs MVVM vs MVI", and the
  custom FIFO/`UiStateMachine`/`MviViewModel` machinery from "Why your MVI can't handle two Intents
  at once" — not applicable, they presuppose an MVI pipeline or Hilt multibinding this project
  doesn't use. That last article's underlying race-condition claim is not MVI-specific though — it
  produced its own actionable finding, step 4 above.
