# Screen State Flicker on First Load

## The Problem

Screens that use a `when` block to switch between loading, error, empty, and content states
can flicker on the very first frame if the initial `isLoading` value is `false`.

### Concrete example — `TeamScreenContent`

```kotlin
when {
    state.teamMembers.isEmpty() && state.error.isNotEmpty() -> ErrorStateWidget(...)
    state.teamMembers.isEmpty() && !state.isLoading -> EmptyStateWidget(...)
    else -> LazyColumn { ... }
}
```

The initial `TeamState` is:

```kotlin
data class TeamState(
    val isLoading: Boolean = false,   // <-- false by default
    val error: NativeText = NativeText.Empty,
    val teamMembers: ImmutableList<TeamMember> = persistentListOf(),
    val onRefresh: () -> Unit
)
```

The ViewModel's `init` block launches a coroutine that sets `isLoading = true`, but coroutines
don't execute synchronously — there is at least one frame between the initial state emission and
when the coroutine body runs.

Frame-by-frame sequence on first launch:

| Frame | isLoading | teamMembers | error | Branch hit          |
|-------|-----------|-------------|-------|---------------------|
| 1     | false     | empty       | empty | **EmptyStateWidget** (wrong) |
| 2+    | true      | empty       | empty | `else` → spinner    |
| final | false     | [data]      | empty | `else` → list       |

The `EmptyStateWidget` flashes for one frame before the spinner appears.

---

## Use Cases to Watch For

### 1. Empty state shown before loading starts

**Trigger:** Any `when` branch that checks `list.isEmpty() && !isLoading`, combined with
`isLoading = false` as the initial state default.

**Symptom:** A brief flash of "no items found" text before the loading spinner appears.

### 2. Error state on first load (correct but fragile)

**Trigger:** `list.isEmpty() && error.isNotEmpty()` — this is the intended full-screen error
path when the first load fails.

**Risk:** If the initial `error` somehow has a non-empty value (e.g. a retained ViewModel
from a previous navigation back-stack entry), this branch fires immediately and shows the
error screen before any load attempt.

### 3. Error state during refresh when data is already loaded

**Trigger:** `list.isNotEmpty()`, so `list.isEmpty() && error.isNotEmpty()` is false.
The `else` branch keeps the list visible and a snackbar is shown via `LaunchedEffect`.

This is the **correct behavior** — the existing data should remain visible while the
refresh error is surfaced as a snackbar. The `when` conditions already handle this correctly
as long as the error check comes before the empty check.

---

## The Fix

Make `teamMembers` nullable to explicitly model three distinct states:

- `null` — not yet fetched (show spinner)
- empty list — fetched, no results (show empty state)
- non-empty list — fetched with data (show list)

```kotlin
data class TeamState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val teamMembers: ImmutableList<TeamMember>? = null,  // null = not yet loaded
    val onRefresh: () -> Unit
)
```

Update the `when` block to branch on nullability first:

```kotlin
when {
    state.teamMembers == null -> {
        // still loading — PullToRefreshBox spinner handles the visual
    }
    state.teamMembers.isEmpty() && state.error.isNotEmpty() -> ErrorStateWidget(...)
    state.teamMembers.isEmpty() -> EmptyStateWidget(...)
    else -> LazyColumn { ... }
}
```

Frame-by-frame sequence:

| Frame | teamMembers | isLoading | Branch hit               |
|-------|-------------|-----------|--------------------------|
| 1     | null        | false     | loading branch → spinner |
| 2+    | null        | true      | loading branch → spinner |
| final | [data]      | false     | `else` → list            |

No empty state is shown during the initial load.

---

## Why Not `isLoading = true` as Default

Setting `isLoading = true` in the state default feels like a fix but introduces edge cases:

- **Previews** — every preview starts in a spinner state unless overridden manually.
- **Retained ViewModels** — if the ViewModel survives config change or back-stack re-entry,
  `isLoading` might be `false` while `teamMembers` is empty, hitting the empty state
  unexpectedly on re-composition before the ViewModel re-fetches.
- **Semantic mismatch** — `isLoading = true` means "a request is in flight", not
  "we haven't started yet". Conflating the two makes the flag harder to reason about.

Nullable `teamMembers` makes the "not yet fetched" state unambiguous and self-documenting.

---

---

## Paging Variant (`LazyPagingItems`)

The same flicker happens with `LazyPagingItems`. On the very first frame, before the `Pager`
starts its initial load:

- `itemCount = 0` → `isEmpty() = true`
- `loadState.refresh = NotLoading` → `isNotLoading() = true`

So `stories.isEmpty() && stories.isNotLoading()` fires on frame 1 and shows `EmptyStateWidget`.

### Why nullable doesn't apply here

`LazyPagingItems` is not nullable — you can't use the same sentinel as the plain list case.

### The fix — use `endOfPaginationReached`

Paging sets `loadState.append.endOfPaginationReached = true` once the first load completes
(even if the result is empty). Before any load, it is `false`. This makes it a reliable
sentinel for "a real load has finished":

```kotlin
fun LazyPagingItems<*>.hasCompletedLoad(): Boolean =
    loadState.append.endOfPaginationReached || itemCount > 0
```

Add this extension to `PagingUtils.kt` and update the empty state condition:

```kotlin
when {
    stories.hasError() && stories.isEmpty() -> ErrorStateWidget(...)
    stories.isEmpty() && stories.isNotLoading() && stories.hasCompletedLoad() -> EmptyStateWidget(...)
    else -> LazyColumn { ... }
}
```

State machine:

| Frame | itemCount | isNotLoading | hasCompletedLoad | Branch hit          |
|-------|-----------|--------------|------------------|---------------------|
| 1     | 0         | true         | false            | `else` → spinner    |
| 2+    | 0         | false        | false            | `else` → spinner    |
| done (empty) | 0 | true        | true             | EmptyStateWidget    |
| done (data)  | N | true        | true             | `else` → list       |

No extra state in the ViewModel, no `remember` flags in the composable.

---

## Rule of Thumb

**Never show an empty state until a load has completed.**

Use a nullable list (or equivalent sentinel) to distinguish "not yet fetched" from
"fetched but empty". The `EmptyStateWidget` should only appear after a successful
response that returned zero items.