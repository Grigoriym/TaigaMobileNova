# Excessive list requests on Issues/Epics/Backlog, and a saved-filter wipe on restore

**Status:** Done
**Link:** `docs/revisit.md` entry 53   **Updated:** 2026-11-22

## Report

Raised by gregory on 2026-09-07 as revisit entry 53, right after Kanban's duplicate `filters_data`
call was fixed (`0c34a570`). The concern was that the app makes more Taiga API calls than it needs
to, and that Kanban probably isn't the only place. The entry listed two leads and explicitly
labelled them unconfirmed:

1. **Filter-driven double list-fetch.** `loadFiltersData()` writes back to the same `StateFlow`
   the paging `combine` watches. It was thought to re-fetch only when the user has saved filters.
2. **No search debounce anywhere.** Every keystroke starts a request.

The entry also asked that each lead be confirmed with real request counts before any production
code changed. That's what this investigation did.

## Method

- Fdroid debug build (`./gradlew :androidApp:assembleFdroidDebug`) on the `Pixel_10a` AVD
  (API 37), logged in to the local Taiga at `10.0.2.2:9000`, project 1.
- The app's Ktor client already logs every call at `LogLevel.ALL` under the `Ktor` tag
  (`core/api/.../KmpNetworkModule.kt:114-121`). Counts below are `REQUEST:` lines from
  `adb logcat`, cleared immediately before each action. A cancelled request shows up as
  `REQUEST <url> failed with exception` (the `flatMapLatest` cancellation).
- To make a saved filter's counts go stale, a temporary issue was created in status *New* through
  the REST API and deleted at the end (`DELETE /api/v1/issues/3` → 204).

## Findings

All on-device evidence is from the Issues screen. Epics and Backlog weren't driven on device. Their
ViewModels have the same code shape line for line (cited below), so the same behaviour is
**inferred** for them, not observed.

### F1. With no saved filter, a cold open fetches the list once (the entry's inference holds)

Issues opened from the drawer with no filter selected:

```
issues/filters_data?project=1
issues?page=1&page_size=20&q=&project=1
```

One list request. `FiltersData.updateData()` (`feature/filters/domain/.../FiltersData.kt:56-92`)
maps over the current, empty selection, produces an equal `FiltersData`, and the `StateFlow`
drops it as a duplicate.

### F2. A saved filter whose counts changed on the server makes every screen open fetch the list twice (Lead 1 confirmed, with a narrower trigger than the entry assumed)

With the *New* status filter saved, opening Issues in a fresh process:

- **Counts unchanged since the filter was saved:** one list request. `updateData` refreshes
  `name`/`color`/`count` to the same values, so again nothing is re-emitted. The entry's "once a
  user has previously selected any filter" was too broad.
- **Counts changed** (one more *New* issue created on the server): two list requests, the first
  cancelled partway:

```
22:42:52.315 REQUEST: issues/filters_data?project=1
22:42:52.412 REQUEST: issues?page=1&page_size=20&q=&project=1&status=1
22:42:52.480 REQUEST issues?...&status=1 failed with exception    ← cancelled by flatMapLatest
22:42:52.490 REQUEST: issues?page=1&page_size=20&q=&project=1&status=1
```

The two requests are identical. Only the selected item's `count` differs between the two
`FiltersData` values, and the paging source never sends `count`: it reads only ids, and names for
tags (`feature/issues/data/.../IssuesPagingSource.kt:38-43`, `commaString()`).

How often this happens: on any team where work moves, the counts on a saved filter (open
issues per status, per assignee) change between visits, so this is the normal case, not an
edge case.

Code: `IssuesViewModel.kt:53-57` (`combine(session.issuesFilters, searchQuery).flatMapLatest`) and
`:101-105` (write-back). The same code is in `EpicsViewModel.kt:55-62`/`:101-105` and
`ScrumBacklogViewModel.kt:53-60`/`:101-116`.

### F3. After process death, the list is first fetched with no filters at all (new)

Backgrounded on Issues with the *New* filter saved, `am kill`, relaunched (the app restores
straight onto Issues). In 6 of 6 restores the first list request had **no filter**:

```
22:43:50.403 REQUEST: issues/filters_data?project=1
22:43:50.404 REQUEST: issues?page=1&page_size=20&q=&project=1            ← no status=1
22:43:50.874 REQUEST issues?...&project=1 failed with exception          ← cancelled
22:43:50.895 REQUEST: issues?page=1&page_size=20&q=&project=1&status=1
```

The cause is `FiltersStorageImpl` (`core/storage/.../FiltersStorageImpl.kt:35-67`). Each filter
flow is `dataStore.data.map { … }.stateIn(scope, Eagerly, FiltersData())`. Until DataStore's first
read finishes, the `StateFlow` holds a placeholder `FiltersData()`, which looks exactly like "no
filters". In a fresh process that read took about 470–500 ms (the gap between the two list
requests above, consistent across runs). The paging `combine` starts on the placeholder and
fires an unfiltered request. Opening the screen from the drawer after a cold start doesn't hit
this, because by the time the user taps, the read has finished. `FiltersStorage` is injected
early through `AuthStateManager` (`core/storage/.../auth/AuthStateManager.kt:16`). (That this is
the reason the drawer path is clean is inference.)

### F4. The same placeholder can wipe the user's saved filter: reproduced 1 of 5 times (new, the most serious)

`loadFiltersData()` builds what it writes back from `_state.value.activeFilters`
(`IssuesViewModel.kt:103-105`), and `activeFilters` is copied from `session.issuesFilters`
(`:62-68`). If the `filters_data` response arrives **before** DataStore's first read,
`activeFilters` still holds the placeholder `FiltersData()`. `updateData()` of an empty selection
is empty, and `changeIssuesFilters(empty)` **persists the empty selection over the user's
real one**.

In F3's runs the two events were only ~60–100 ms apart, with DataStore winning. Five more
restores in a row:

| Run | DataStore value lands | `filters_data` response | Result |
|---|---|---|---|
| 1 | 18.515 | 18.583 | filter kept |
| 2 | 33.829 | 33.887 | filter kept |
| 3 | 49.227 | 49.283 | filter kept |
| 4 | 04.483 | 04.544 | filter kept |
| 5 | after 19.832 | **19.832** | **filter erased**: `status=1` request at 20.083, then another unfiltered request at 20.290 |

After run 5 the filter badge was gone from the screen, and the persisted file contained an empty
selection:

```
$ run-as … cat files/datastore/taiga_session_filters_storage.preferences_pb | xxd
… 0e66 696c 7465 7273 5f69 7373 7565 7312 042a 027b 7d   .filters_issues..*.{}
```

So the user silently loses their saved filter, and that run made **three** list requests instead
of one. The margin depends on network latency against disk speed: a fast LAN server (like this
one) or a slow device makes it more likely. Epics and Backlog have the same code (inferred, not
reproduced).

### F5. Search sends one request per keystroke on 4 screens (Lead 2 confirmed)

`issue` typed into the Issues search field with `adb shell input text`, one character per call
(5 characters in ~260 ms):

```
issues?…&q=i&…
issues?…&q=is&…
issues?…&q=iss&…
issues?…&q=issu&…
issues?…&q=issue&…
```

That's 5 list requests, 3 of them logged as cancelled. A human types more slowly than `adb`, so
more of those requests would complete instead of being cancelled. `grep -rn debounce` finds nothing
in the codebase. The screens that pass a search query into paging are exactly these four (checked
by grepping every ViewModel for `search` and every `flatMapLatest`):
`IssuesViewModel.kt:53`, `EpicsViewModel.kt:55`, `ScrumBacklogViewModel.kt:53`,
`ProjectSelectorViewModel.kt:47`. The last one has no filters, only search.

### Checked and ruled out

- **Dashboard cold start** makes 13 requests, all with different endpoint/parameter combinations.
  None are duplicates.
- **Selecting a filter** in the sheet makes exactly one list request, with the new parameters.
- **`kanbanFilters`** has no consumer in `feature/` (grep), so Kanban isn't affected by F2–F4.
  `FiltersStorage.kanbanFilters` looks like dead API. That's noted here, not acted on.

## Root cause

Three separate defects, two of which share a cause:

1. **`FiltersStorageImpl`'s `StateFlow`s start from a placeholder that can't be told apart from
   real data** (`FiltersStorageImpl.kt:35-67`, `stateIn(…, FiltersData())`). Downstream code
   treats it as "the user has no filters". That causes F3 (an unfiltered first fetch) and,
   because of the next point, F4.
2. **The ViewModels merge the refreshed server data into their own UI-state copy of the
   selection and write the result back** (`IssuesViewModel.kt:103-105` and the two siblings).
   That read-modify-write uses a value that may still be the placeholder. That's F4, the
   filter wipe.
3. **The list is keyed on the whole `FiltersData`, including display-only fields** (`name`,
   `color`, `count`), so the routine write-back in point 2 re-triggers paging even though the
   request is identical (F2). Separately, **the search query reaches `flatMapLatest` with no
   debounce** (F5).

## Impact

- **F4, the filter wipe:** anyone who saves a filter on Issues, Epics or Backlog and whose app
  gets killed in the background (routine on Android) can lose that filter on return, without any
  message. It reproduced 1 in 5 times here. It's the only finding with user-visible damage.
  The workaround is to re-select the filter.
- **F2/F3:** one or two extra list requests per screen open, under normal conditions (counts
  drift) or after a restore. On screen, F3 can briefly show unfiltered rows before the filtered
  list replaces them (not visually confirmed; the window is about 0.5 s).
- **F5:** N requests for an N-character search on 4 screens. The server cost is real on large
  projects because Taiga's `q=` search isn't cheap. In the app, `flatMapLatest` already discards
  stale results, so there's no correctness bug, only wasted load.

## Open questions

- **Is persisting `name`/`count` into the saved filter wanted at all?** `updateData` exists so
  the filter sheet shows current names and counts for the selected items. That could live only
  in UI state, never persisted. Doesn't block: either way the list should stop reacting to
  display-only fields.
- **Debounce length.** 300 ms is the usual default, and nothing here argues for a different
  value. The first emission (the empty query when the screen opens) and clearing the field
  should not be delayed. `debounce { if (it.isEmpty()) 0 else 300 }` handles both.

## Options

### A. Fix all four, as separate parts in one PR (recommended)

1. **F4:** move the read-modify-write into storage. A `suspend` call on `FiltersStorage`
   (e.g. `refreshIssuesFilters(serverData)`) does the `updateData` merge inside `dataStore.edit {}`,
   against the **persisted** value. The ViewModels stop reading `_state.value.activeFilters` to
   build the write.
2. **F3:** stop the list from starting on the placeholder. The ViewModels' paging `combine`
   uses a storage flow with no fake initial value (`dataStore.data.map{}.distinctUntilChanged()`),
   so the first list request waits for the real selection.
3. **F2:** key the list on the query-relevant part of the filters only: a
   `distinctUntilChanged` comparing selected ids (names for tags) before `flatMapLatest`.
4. **F5:** debounce the search query before `combine`, with an immediate first/empty emission,
   on all four screens.

- Pros: closes the data-loss bug at its cause, not just its symptom. Each part is small and
  independently testable with the existing fakes (`FakeFiltersStorage`, the ViewModel tests in
  each `feature/*/ui/commonTest`).
- Cons: touches `FiltersStorage`'s interface, which all three ViewModels, `FakeFiltersStorage` and
  `FiltersStorageImplTest` depend on. That's four screens' worth of ViewModels in one PR. Part 3
  adds a comparison helper on `FiltersData` that has to be kept in sync with what the paging
  sources actually send. If a new filter dimension is added and not added to the helper, changing
  only that dimension won't refetch.
- Risk / blast radius: low to moderate. Filters and search on Issues, Epics, Backlog and Project
  Selector. Kanban is untouched.

### B. Fix only F4 and F5

Parts 1 and 4 only.

- Pros: smallest diff that removes the user-visible bug and the biggest request multiplier.
- Cons: leaves one to two redundant list requests on most screen opens (F2/F3), the original
  complaint in revisit entry 53. The placeholder that caused F4 stays, so the next feature that
  reads `issuesFilters.value` at startup can hit the same trap.

### C. Do nothing

- Pros: no work.
- Cons: users keep losing saved filters intermittently, and the request overhead stays. Not
  defensible now that F4 is reproduced.

**Recommendation: A.** F4 is a real data-loss bug, and F3 shares its cause, so fixing the
placeholder once fixes both. F2 and F5 are each a few lines once the storage side is right.

## Decision

**Option A**, decided by gregory on 2026-11-22. The selected items' names and counts stay
persisted as they are today. Only the list stops reacting to them.

## Plan

One PR, one commit per part. Each part comes with a regression test that fails before its fix.

| Part | Fixes | Change | Verified by |
|---|---|---|---|
| 1 | F4 | `FiltersStorage.refresh{Issues,Epics,Scrum}Filters(serverData)` runs `updateData` inside `dataStore.edit {}` against the persisted value. The three ViewModels call it instead of building the write from `_state.value.activeFilters`. | ViewModel tests: the server's filter data arriving before the stored selection has loaded leaves the selection intact (fails before: it's overwritten with an empty one). `FiltersStorageImplTest`: refresh merges into what's stored. |
| 2 | F3 | `FiltersStorage`'s filter flows become plain `Flow`s with no placeholder initial value. | `FiltersStorageImplTest` with a DataStore whose first read is held back: nothing is emitted until the read completes (fails before: `FiltersData()` is emitted). |
| 3 | F2 | The paging `combine` reads the filters through `distinctUntilChanged(FiltersData::selectsSameItemsAs)`, which compares ids (names for tags) only. | `FiltersDataTest` for the comparison. ViewModel tests: a refresh that changes only counts doesn't start another list load (fails before: two loads). |
| 4 | F5 | The search query is debounced by 300 ms before it reaches paging. An empty query goes through immediately. Applies to Issues, Epics, Backlog and Project Selector. | ViewModel tests on virtual time: three fast keystrokes produce one list load with the last query (fails before: three). |

Final checks: full `./gradlew jvmTest`, `ktlintCheck`, `:koverVerify`, and an emulator re-run of
the F2, F3/F4 and F5 request counts from Findings.

## What landed

Three commits on `fix/filters-requests-and-wipe`. Parts 1 and 2 shipped as one commit because
they change the same `FiltersStorage` interface, and the fake can only model "stored filters not
read yet" once the placeholder is gone.

| Commit | Fixes | Regression tests (each confirmed failing before its fix) |
|---|---|---|
| `fix(filters): stop a saved filter being wiped on restore` | F3, F4 | `…ViewModelTest` × 3: "filters data arriving before the stored selection is read keeps the stored selection" (failed: overwritten with an empty selection). `FiltersStorageImplTest`: "a filters flow emits nothing until the stored value has been read" (failed with the old `stateIn(…, FiltersData())` restored temporarily), plus a `refresh*Filters` test per section. |
| `fix(filters): don't reload a list when only filter counts change` | F2 | `FiltersDataTest` (5 cases for `selectsSameItemsAs`). `…ViewModelTest` × 3: "refreshed counts on the selected filters do not reload the list" (failed: two loads, the count differing by one). |
| `fix(search): debounce list search instead of a request per keystroke` | F5 | `SearchQueryTest` (3 cases). `…ViewModelTest` × 4, Project Selector included: "quick search keystrokes load the list once, with the last query" (failed: `[, i, is, iss]`). |

Gates: full `./gradlew jvmTest` (1977 tests, 0 failures), `ktlintCheck`, `detekt` (0 findings),
`koverXmlReport` + `:koverVerify` (line 95.39 %, branch 82.69 %).

Emulator re-run (same AVD, server and *New* filter as in Findings):

| Scenario | Before | After |
|---|---|---|
| Restore onto Issues after process death, with the saved filter's count changed on the server | 2–3 list requests, the first unfiltered; filter erased in 1 of 5 | 1 filtered request in 6 of 6; filter kept, still present in the DataStore file |
| Type `issue` into Issues search | 5 list requests | 1 (`q=issue`) |
| Clear the search field | — | 1 (`q=`), immediately |
| Open Epics / Backlog / Project Selector | — | 1 list request each, rows render |

Left out on purpose: `FiltersStorage.kanbanFilters` has no consumer (see "Checked and ruled
out"). It only had its type changed to `Flow` so the interface stays consistent.
