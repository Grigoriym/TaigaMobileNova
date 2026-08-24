# WorkItem Edit Scoping

## Problem

When navigating between work item detail screens (Issue, UserStory, etc.), each detail screen
navigates to shared sub-screens like `EditDescriptionScreen`, `EditTagsScreen`, `EditTeamMemberScreen`,
and `EditSprintScreen`. These sub-screens need to communicate their results back to the parent
detail screen.

A global singleton for this communication causes conflicts: if a user navigates from
`IssueDetailsScreen` into `UserStoryDetailsScreen`, both share the same singleton instance,
so updates from one bleed into the other.

See [`workitem-edit-scoping.puml`](workitem-edit-scoping.puml) for a visual diagram.

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

## Feasibility investigation (2026-08-14)

Follow-up investigation to confirm the proposal above is actually buildable with what's pinned in
this repo today, and that the migration is safe given the real navigation topology. No code was
changed — this only verifies assumptions.

**Koin versions and APIs — confirmed, no dependency bump needed.** The pinned versions
(`koin-bom`/`koin-annotations` `4.2.2`, `koin-plugin` `1.1.0`) were checked directly against the
downloaded jars/sources. `Scope`, `createScope`, `getScope`, `KoinScopeComponent`, `ScopeRegistry`
all live in `koin-core`'s `commonMain` — identical on Android, iOS, and JVM, no platform-specific
gap.

**The compiler-plugin route doesn't fit — has to be the manual approach.** `koin-annotations`
4.2.2 does ship `@Scope`/`@Scoped`/`@ScopeId`, but `@ScopeId(name = "...")` bakes a **compile-time
literal** string into the generated `getScope("...")` call. It cannot take a runtime-computed id
like `"Issue_42"`, so it can't express this per-work-item scope. The hand-written
`KoinComponent` + `getKoin().createScope(...)`/`getKoin().getScope(...)` approach shown above is
not a stand-in for a nicer annotation-based path — it's the only path.

**No existing scope pattern to copy.** `grep` for `@Scope`, `scoped(`, `getScope`, `createScope`
across the whole codebase returns nothing outside this proposal. Every screen resolves its
ViewModel via plain `koinViewModel<T>()` against Koin's default/root scope. This migration would
be the first Koin scope used anywhere in the project.

**Navigation topology confirmed safe for the "parent creates scope, child looks it up" design.**
Checked `composeApp/.../main/MainNavHost.kt` and each feature's `*NavGraph.kt`:
- Single flat `NavHost`, no nested `navigation{}` graphs. The five edit destinations are
  registered once in `WorkItemEditsNavGraph.kt` and reached from `IssueNavGraph`,
  `UserStoryNavGraph`, `TaskNavGraph`, `EpicNavGraph`, and (description only) `WikiNavGraph`.
- Every parent → child call is a plain `navController.navigate(SomeDestination(...))` — no
  `popUpTo`/`launchSingleTop` that could pop the parent's back stack entry. Every child → back
  call is a plain `popBackStack()`.
- No custom `LocalViewModelStoreOwner` anywhere (`grep` empty) — `koinViewModel()` uses the
  standard Navigation-Compose per-`NavBackStackEntry` `ViewModelStore`. The parent's
  `viewModelScope`/`onCleared()` only fires when the user navigates back past it, not while a
  child sits on top.
- No deep links exist (`grep` for `deepLink`/`navDeepLink` empty) — there's no path that creates a
  child edit screen without its parent already on the stack.
- Process death does **not** risk resuming mid-flow: `MainNavHost` always calls
  `navigateToDashboardAsTopDestination()` on (re)start, which does
  `popUpTo(graph.id) { inclusive = true; saveState = false }` — any restored back stack is wiped
  back to Dashboard/ProjectSelector/Login before the user ever sees it. There's no scenario where a
  child edit ViewModel is created before its parent's.
- The edit destinations are genuinely **multi-parent** (5 different feature graphs open the same
  `WorkItemEditDescriptionNavDestination` etc., each with a different `TaskIdentifier`), which is
  exactly why keying by `"${taskType}_${workItemId}"` — as both the current map and the proposed
  scope ID do — is required; there's no single owning parent type to scope by instead.

**Real cost the comparison table above understates: testing.** The 5 child edit ViewModels
currently take `WorkItemEditStateRepository` via plain constructor injection, so their tests just
construct a fake directly — matching this project's hand-written-fakes-only convention (no MockK,
see CLAUDE.md Testing / Settled Decisions). Under the scoped design those ViewModels become
`KoinComponent` and call `getKoin().getScope(id).get()` inside the constructor body, so their tests
would need a real Koin scope stood up instead of a plain fake — a genuine regression against an
explicit project convention, not just a style change. There's also a stricter failure mode:
`getScope(id)` throws `ScopeNotCreatedException` if the parent hasn't opened it yet, where today's
`getOrCreateSession` silently creates one on first access.

---

## Recommendation

The current solution is **correct and safe**, and the investigation above found nothing that makes
migrating urgent — no bug, no version blocker, and the navigation topology would support either
design equally well. The Koin scope approach is more semantically accurate and removes the internal
session map, but the practical gain is small because `onCleared()` already handles cleanup
reliably, and the testing-convention cost (previous section) is a real downside, not just a style
tradeoff.

**Migrate to Koin scopes if:**
- You want to remove `WorkItemEditStateRepository` as a singleton entirely
- You want Koin to be the single source of truth for object lifetimes
- You are adding more scoped dependencies in the future (scope becomes more valuable with multiple members)

**Keep the current approach if:**
- You prefer simpler, injection-based sub-ViewModel setup (no `getKoin()` lookup)
- You want easier unit testing of sub-ViewModels without scope setup (this project's established
  convention — see above)

If migrating, the scope ID convention (`"${taskType}_${workItemId}"`) is already established and
consistent with the current session key format — no nav arg changes needed.