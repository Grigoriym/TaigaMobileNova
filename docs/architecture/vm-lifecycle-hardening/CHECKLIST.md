# ViewModel Lifecycle & Error-Handling Hardening — Checklist

**Progress:** 3/7 done. **Current step:** none active — steps 4-7 are all ungated and available;
step 4 (watch/unwatch race fix) is the only one with a concrete implementation already scoped
rather than being an investigation.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source articles, the codebase
evidence behind each finding, and the findings that were assessed and declined. See
[CHECKLIST-DONE.md](CHECKLIST-DONE.md) for ticked steps.

4. Gate the watch/unwatch button on `areWatchersLoading` (last-write-wins race)
   - Confirmed gap: `WorkItemWatchersDelegateImpl.handleAddMeToWatchers`/`handleRemoveMeFromWatchers`
     each run as their own independent `viewModelScope.launch`, doing several sequential network
     calls before writing `isWatchedByMe` into `_watchersState`. The watch/unwatch button in
     `WatchersWidget.kt` is never disabled while `areWatchersLoading` is true (only gated on
     `isOffline`), so a user can tap watch then unwatch before the first request returns — whichever
     response lands second wins, regardless of which action the user took last. Affects all four
     screens sharing this delegate/widget: Task, UserStory, Epic, Issue detail.
   - Fix: pass `isOffline = isOffline || watchersState.areWatchersLoading` to the watch/unwatch
     `TaigaTextButtonWidget` call in `WatchersWidget.kt` (feature/workitem/ui). Reuses the existing
     disabled-button visual; no new prop, no MVI rearchitecture.
   - Verify: `./gradlew jvmTest`; confirm on the emulator/desktop app per CLAUDE.md's Verification
     rule (this is a UI-visible change) that the button visibly disables while a watch/unwatch
     request is in flight.
   - Worth weighing before starting (not decided): going optimistic instead of/in addition to
     disabling the button — same pattern `KanbanViewModel.moveStory()` already uses for drag-and-drop
     (update state immediately, revert + show error on failure). See IMPLEMENTATION_PLAN.md finding
     8's "Secondary source" note (*What Are Optimistic Updates?*) for the tradeoff. Not scoped yet.

5. Investigate: redundant `init`-time re-fetches of already-known/rarely-changing data
   - Not gated — this is an investigation step, not a commitment to change anything project-wide.
   - Claim to check: "How to load ViewModel's data without using 'init'" (its Issue 3, "Customer that
     never returns") argues that unconditionally re-running an `init`-time load on every VM
     reconstruction — even for data that already loaded successfully and rarely changes — creates
     avoidable failure windows: a transient network blip on the re-fetch turns a screen that *was*
     fine into an error, costing a conversion for no reason tied to the actual data. Distinct from
     finding 3 above (that one is about test flakiness from concurrent loads) and from checklist step
     2 (that one is about losing *user-entered* input on process death) — this is about needlessly
     re-risking read-only/rarely-changing data on any VM recreation, not just process death.
   - Candidates already surfaced by finding 3's grep: `EpicsViewModel`, `IssuesViewModel`,
     `KanbanViewModel`, `ScrumBacklogViewModel`, `EditSprintViewModel` all unconditionally call
     `getPermissions()` from `init` — permissions rarely change mid-session, so every re-entry into
     these screens re-risks a network call that already succeeded once.
   - Approach agreed with gregory (2026-08-29): finding candidates is a static-grep pass (does the
     `init`-time load fetch data that's already known or unlikely to change, e.g. permissions, nav-arg
     data, already-cached repository reads?) — not something that needs live reproduction to
     enumerate. Only reproduce live (emulator, airplane-mode toggle around a re-navigation) on the one
     or two candidates actually picked, to confirm the UX regression is real, not as a blanket sweep.
   - Not started.

6. Investigate: does the watch/unwatch last-write-wins race (step 4) generalize elsewhere
   - Not gated — this is an investigation step, not a commitment to fix anything beyond step 4.
   - Claim to check: step 4's race (two independent `viewModelScope.launch` blocks writing to
     overlapping state with no ordering/cancellation guard, reachable by a user firing both before
     the first resolves) was found while investigating watchers specifically — the codebase was
     never swept for other ViewModels/delegates with the same shape.
   - Approach: static grep first — state classes/delegates with more than one independent `launch`
     site writing into the same `MutableStateFlow`, shortlisted by whether the UI actually exposes
     two overlapping triggers a user could reach (button pair, double-tappable toggle), the way
     `WatchersWidget` does. Only reproduce live on candidates that survive the shortlist, not as a
     blanket sweep.
   - Not started — see IMPLEMENTATION_PLAN.md's "Does the watch/unwatch race generalize?" section.

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
