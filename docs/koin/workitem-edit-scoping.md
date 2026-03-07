# WorkItem Edit Scoping

## Problem

When navigating between work item detail screens (Issue, UserStory, etc.), each detail screen
navigates to shared sub-screens like `EditDescriptionScreen`, `EditTagsScreen`, `EditTeamMemberScreen`,
and `EditSprintScreen`. These sub-screens need to communicate their results back to the parent
detail screen.

A global singleton for this communication causes conflicts: if a user navigates from
`IssueDetailsScreen` into `UserStoryDetailsScreen`, both share the same singleton instance,
so updates from one bleed into the other.

See `docs/workitem_edit_scoping.puml` for a visual diagram.

---

## Current Solution

`WorkItemEditStateRepository` is registered as `@Single` (singleton). Isolation is achieved via an
internal `Map<String, WorkItemEditSession>` keyed by `"${taskType}_${workItemId}"`.

**Lifecycle management is manual:**
- The parent ViewModel subscribes to the session's flows in `init`
- The parent ViewModel calls `workItemEditStateRepository.clearSession(id, type)` in `onCleared()`

This works and is correctly isolated, but it is a simulated scope — not a true DI scope.

**Files involved:**
- `feature/workitem/ui/…/WorkItemEditStateRepository.kt` — singleton + session map
- `feature/workitem/ui/…/WorkItemEditSession.kt` — per-item channels and state
- `feature/issues/ui/…/IssueDetailsViewModel.kt` — subscribes in `init`, clears in `onCleared()`
- `feature/workitem/ui/…/EditDescriptionViewModel.kt` — injects repository, calls `updateDescription`

---

## Proposed Solution: Koin Scopes

Koin's `Scope` API is available in KMP `commonMain` on all targets (Android, iOS, Desktop).

The idea: the parent ViewModel **creates** a scope when it initialises and **closes** it in
`onCleared()`. Sub-screen ViewModels **look up** the already-open scope by ID to get the same
session instance.

### 1. Define a scope qualifier (shared location, e.g. `workitem/domain` or `workitem/ui`)

```kotlin
import org.koin.core.qualifier.named

val WorkItemEditScope = named("WorkItemEditScope")
```

### 2. Register `WorkItemEditSession` as scoped

In the Koin module for `workitem/ui`:

```kotlin
@Module
class WorkItemUiModule {
    @Scoped(binds = [], scope = WorkItemEditScope)
    fun workItemEditSession(): WorkItemEditSession = WorkItemEditSession()
}
```

Or with the `@Single` + `scope` parameter (Koin IR compiler plugin supports this):

```kotlin
@Single(scope = WorkItemEditScope)
class WorkItemEditSession
```

### 3. Parent ViewModel opens and owns the scope

```kotlin
@KoinViewModel
class IssueDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    // ... other deps
) : ViewModel(), KoinComponent {

    private val route = savedStateHandle.toRoute<IssueDetailsNavDestination>()
    private val issueId: Long = route.issueId

    private val scopeId = "Issue_$issueId"
    private val koinScope = getKoin().createScope(scopeId, WorkItemEditScope)

    private val editSession: WorkItemEditSession = koinScope.get()

    init {
        editSession.descriptionFlow
            .onEach(::onNewDescriptionUpdate)
            .launchIn(viewModelScope)
        // … other subscriptions
    }

    override fun onCleared() {
        super.onCleared()
        koinScope.close()  // automatically destroys WorkItemEditSession
    }
}
```

### 4. Sub-screen ViewModels look up the existing scope 

The `workItemId` and `TaskIdentifier` are already in the nav args
(see `WorkItemEditDescriptionNavDestination`), so the sub-ViewModel can reconstruct the scope ID
without any new parameter:

```kotlin
@KoinViewModel
class EditDescriptionViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel(), KoinComponent {

    private val route = savedStateHandle.toRoute<WorkItemEditDescriptionNavDestination>(
        typeMap = typeMapOf(listOf(typeOf<TaskIdentifier>()))
    )

    // Reconstruct the same key the parent used
    private val scopeId = "${route.taskIdentifier.toScopePrefix()}_${route.workItemId}"
    private val editSession: WorkItemEditSession = getKoin().getScope(scopeId).get()

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            if (shouldReturnCurrentValue && descriptionChanged()) {
                editSession.emitDescription(_state.value.currentDescription)
            }
            _onBackAction.send(Unit)
        }
    }
}

// Helper to produce consistent scope ID prefixes
fun TaskIdentifier.toScopePrefix(): String = when (this) {
    is TaskIdentifier.WorkItem -> commonTaskType.name
    TaskIdentifier.Wiki -> "WIKI"
}
```

---

## Comparison

| | Current (singleton + map) | Koin scopes |
|---|---|---|
| Isolation | Yes, by map key | Yes, by scope ID |
| Lifecycle owner | ViewModel (manual) | ViewModel (manual open, Koin closes) |
| Forget to clean up | Session leaks | Scope leaks (same risk) |
| Sub-ViewModel access | Inject repository, pass workItemId | `getKoin().getScope(id).get()` |
| Koin API coupling in sub-VM | Low (plain injection) | Higher (`getKoin()` call) |
| Semantic correctness | Simulated scope | True DI scope |
| Testability | Easy (inject mock repository) | Slightly harder (need to set up Koin scope in tests) |

---

## Recommendation

The current solution is **correct and safe**. The Koin scope approach is more semantically accurate
and removes the internal session map, but the practical gain is small because `onCleared()` already
handles cleanup reliably.

**Migrate to Koin scopes if:**
- You want to remove `WorkItemEditStateRepository` as a singleton entirely
- You want Koin to be the single source of truth for object lifetimes
- You are adding more scoped dependencies in the future (scope becomes more valuable with multiple members)

**Keep the current approach if:**
- You prefer simpler, injection-based sub-ViewModel setup (no `getKoin()` lookup)
- You want easier unit testing of sub-ViewModels without scope setup

If migrating, the scope ID convention (`"${taskType}_${workItemId}"`) is already established and
consistent with the current session key format — no nav arg changes needed.