# 2026-08-08 — Should ViewModels do I/O in `init`?

**Status:** Investigating — options laid out, no decision made yet
**Link:** [revisit.md #1](../revisit.md#1-viewmodels-doing-io-in-init), which points back to
[the koingraphtest issue](2026-08-02-koingraphtest-leaks-coroutine-exceptions.md)
**Updated:** 2026-08-08

## The question

Ten ViewModels start real network/DB work from an `init` block:

```
feature/settings/ui/user/SettingsUserScreenViewModel.kt
feature/wiki/ui/page/details/WikiPageViewModel.kt
feature/wiki/ui/bookmark/list/WikiBookmarksViewModel.kt
feature/wiki/ui/page/list/WikiPagesViewModel.kt
feature/scrum/ui/backlog/ScrumBacklogViewModel.kt
feature/scrum/ui/open/ScrumOpenSprintsViewModel.kt
feature/workitem/ui/screens/sprint/EditSprintViewModel.kt
feature/epics/ui/list/EpicsViewModel.kt
feature/sprint/ui/SprintViewModel.kt
feature/issues/ui/list/IssuesViewModel.kt
```

All ten follow the same shape — `init { loadData() }` (sometimes several loader calls, sometimes
also `session.xFilters.onEach { ... }.launchIn(viewModelScope)` to observe an in-memory filter
flow), where `loadData()` is `viewModelScope.launch { repository.getX() ... }`. Construction
therefore fires a real coroutine that reaches a repository. This is what made `KoinGraphTest`
(which constructs every Koin definition for real) throw from a background thread mid-suite — fixed
at the test layer by installing a non-executing `Main` dispatcher, which the fix doc explicitly
flagged as brittle: it holds only as long as every `init`-time launch goes through
`Dispatchers.Main`, undispatched.

## What's actually driving the design today

Read `SettingsUserScreenViewModel` (simple case) and `WikiPageViewModel` (rich case) in full, plus
`SettingsUserScreenViewModelTest` and `SettingsUserScreenScreen`. Three things constrain the
options more than the revisit entry suggested:

1. **State is not a single derived view — it's an event-sourced object.** `WikiPageViewModel`'s
   `_state: MutableStateFlow<WikiPageState>` is mutated from the initial load, two delegate
   callbacks (`onAttachmentAdd`/`onAttachmentRemove`), a description-edit flow subscription, two UI
   toggles, and a delete action — six independent writers, not one. This is the standard shape
   described in CLAUDE.md's "ViewModel + State Pattern." It rules out collapsing the whole `state`
   into a single `repositoryFlow.stateIn(...)` pipeline; only the *initial load* is a candidate for
   any lazy/deferred mechanism.

2. **Every existing test for these ten relies on synchronous, eager `init`.**
   `SettingsUserScreenViewModelTest` uses `MainDispatcherRule()` (which is
   `UnconfinedTestDispatcher`, confirmed via the test's own comment) specifically so the `init`
   coroutine finishes before `createViewModel()` returns, then asserts `sut.state.value` directly —
   no `runTest { }` collection, no `advanceUntilIdle()`. Any change to *when* the load runs changes
   this test shape for all ten ViewModels' test suites, not just production code.

3. **The screens already use the `LaunchedEffect(Unit)` idiom for something else.**
   `SettingsUserScreen.kt:44` does `LaunchedEffect(Unit) { topBarController.update(...) }` right next
   to where `state` is collected via `collectAsStateWithLifecycle()`. An explicit "screen started"
   trigger would slot into a pattern that's already there, not introduce a new one.

## Options

### A. Status quo — leave `init { loadData() }` as-is

Accept the `KoinGraphTest` fix as sufficient and write down why the design stays.

- **Pros:** zero work. Construction-equals-navigation-to-screen is a defensible proxy in a
  single-Activity Compose app — the ViewModel's lifetime already starts when the screen is
  navigated to, not before. Widely used pattern elsewhere too.
- **Cons:** the fragility the `KoinGraphTest` doc named stands: the safety net depends on every
  `init`-launch going through `Dispatchers.Main`, undispatched, forever. Nothing enforces that — the
  first `viewModelScope.launch(Dispatchers.IO)` in an `init` block (or a bean gaining its own
  scope — checked in revisit #2, currently empty) silently reintroduces the leak with no test
  pointing at the cause. Testing still requires the `UnconfinedTestDispatcher` trick to make
  construction synchronous.

### B. Explicit trigger — `LaunchedEffect(Unit) { viewModel.onScreenStart() }`

Move the loader call out of `init`, expose a public (idempotent) `onScreenStart()`, call it once
from the screen the same way `topBarController.update()` is already called.

- **Pros:** fits an idiom already in every one of these screens (see point 3 above). Fully
  decouples construction from I/O — `KoinGraphTest` would no longer need the `Dispatchers.Main`
  workaround for these ten specifically (other beans are unaffected either way). Test diff is small:
  each test adds one line (`sut.onScreenStart()`) after `createViewModel()`; assertions stay
  synchronous `.value` reads.
- **Cons:** needs an idempotency guard on the ViewModel side (a `hasLoaded` flag or equivalent) —
  without it, navigating back to a retained backstack entry re-fires `LaunchedEffect(Unit)` (fresh
  Composable, same ViewModel) and reloads on every return, which is a **behavior change** from
  today's load-once-per-ViewModel-lifetime. Two files touched per ViewModel (VM + Screen) instead of
  one. "Easy to forget" on a new screen unless CLAUDE.md's ViewModel pattern section is updated to
  include it.

### C. Lazy collection — `state = _state.onStart { loadData() }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(...), initial)`

This is the pattern most of "the countless articles" describe (Now in Android and similar):
`onStart` fires the load the first time `state` gets a real subscriber, `stateIn` only starts that
upstream collection on 0→1 subscriber transitions. `KoinGraphTest` constructs the ViewModel but
never collects `.state`, so nothing would fire during the graph check — same effect as B, no
`Dispatchers.Main` dependence, no explicit trigger method needed.

- **Why it doesn't actually fit here, concretely:**
  - `collectAsStateWithLifecycle()` subscribes when the screen reaches `STARTED` and *unsubscribes*
    when it drops below — with `WhileSubscribed(5000)`, a background/foreground cycle past the
    timeout, or navigating away and back, re-triggers `onStart` and reloads. Same behavior-change
    problem as B, but here it's a property of the primitive, not an omittable guard — avoiding it
    means hand-rolling a "have I loaded before" check inside `onStart` anyway, which gives up most
    of the pattern's simplicity.
  - It breaks every existing test's shape harder than B does: `sut.state.value` right after
    construction would read the flow's *initial* value, not the loaded one, because nothing has
    subscribed yet. Tests would need to actually collect (`state.test { }`/Turbine, or
    `launch { state.collect {} }` + `advanceUntilIdle()`) instead of reading `.value` — a rewrite of
    assertion style across all ten test suites, not a one-line addition.
  - It only covers the *load*. `WikiPageViewModel`'s other five mutators (delegates, toggles, the
    description-flow subscription) stay exactly as they are today — this option buys less than B
    while costing more in test churn.

This is very likely why the articles' solution didn't fit: they're written for the "state is one
derived read-model" case, and this codebase's state shape (point 1 above) plus its existing
synchronous test convention (point 2) are exactly the two things that pattern assumes away.

## Recommendation

**B.** It's the only option that removes construction-time I/O without changing observable
behavior (once the idempotency guard is in) or the shape of the existing tests, and it extends a
pattern the screens already use rather than introducing one. C looks like less code in isolation but
actually asks for a bigger behavior decision (reload-on-return, yes or no) and a test-suite rewrite
to get there — for a benefit (no idempotency guard needed) that's smaller than it looks once the
guard has to be hand-rolled inside `onStart` anyway.

**Not decided yet:** whether "reload on returning to the screen" is desirable at all, independent of
which option implements it — worth a separate product-level answer before touching any of this.
Also open: whether the `session.xFilters.onEach {}.launchIn(viewModelScope)` subscriptions
(`ScrumBacklogViewModel`, `EpicsViewModel`, `IssuesViewModel`) should move too — they don't hit
network/DB directly (in-memory `SharedFlow`/`StateFlow` from a session object), so they're lower
risk, but B's `onScreenStart()` would be the natural place to start them as well if moved.

## Next step, per CLAUDE.md's own instruction on this entry

"Pick the pattern first, apply it to one ViewModel, confirm the screen still behaves, then decide
whether the other nine are worth touching." Once gregory picks A/B/C (or confirms B), prototype on
`SettingsUserScreenViewModel` — smallest of the ten, one loader, already has full test coverage to
diff against.
