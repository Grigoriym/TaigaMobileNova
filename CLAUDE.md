# CLAUDE.md

TaigaMobileNova is an unofficial Android client for Taiga.io. Built with Kotlin, Jetpack Compose, and follows a modular MVVM + Clean Architecture.

## Build Commands

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Build specific flavor
./gradlew :app:assembleGplayDebug
./gradlew :app:assembleFdroidDebug

# Run tests (use fdroid or gplay variant)
./gradlew :module:path:testFdroidDebugUnitTest --tests "com.package.TestClass"
```

## Architecture

**Module structure:** `app/` → `feature/` → `core/` → `utils/`
- Features have data/domain/ui layers (sometimes dto/mapper)
- Use `NativeText` (in `utils:ui`) for localized strings in ViewModels
- Dependencies via Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Build plugins in `build-logic/`

**Tech Stack:**
- Kotlin 2.3.0, JDK 21, Target SDK 36, Min SDK 24
- Jetpack Compose with Material Design 3
- Hilt 2.59 for DI (with KSP)
- Retrofit 3.0.0 + OkHttp for networking
- Navigation Compose 2.9.7 with type-safe routes
- Kotlin Serialization for JSON
- Coroutines, Coil 3.x for images

**Convention Plugins** (in `build-logic/`):
- `taigamobile.android.application` - Main app module
- `taigamobile.android.library` - Android library with KSP
- `taigamobile.android.library.compose` - Android library + Compose
- `taigamobile.android.hilt` - Hilt DI for Android modules
- `taigamobile.kotlin.hilt` - Hilt DI for Kotlin modules
- `taigamobile.kotlin.serialization` - Kotlin Serialization setup
- `taigamobile.kotlin.library` - Pure Kotlin library (no Android)

## Navigation Pattern

Destinations use `@Serializable` data classes/objects:
```kotlin
@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long)

fun NavController.navigateToTask(taskId: Long, ref: Long) {
    navigate(route = TaskDetailsNavDestination(taskId, ref))
}
```

ViewModels extract arguments via `SavedStateHandle.toRoute<T>()`:
```kotlin
private val route = savedStateHandle.toRoute<TaskDetailsNavDestination>()
private val taskId = route.taskId
```

## ViewModel + State Pattern

State class contains data AND callback functions:
```kotlin
data class FeatureState(
    val data: String = "",
    val onDataChange: (String) -> Unit,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
```

ViewModel exposes `StateFlow`, updates via `.update {}`:
```kotlin
private val _state = MutableStateFlow(FeatureState(onDataChange = ::setData, ...))
val state = _state.asStateFlow()

private fun setData(value: String) {
    _state.update { it.copy(data = value) }
}
```

Use `NativeText` for strings from ViewModel → resolve in UI with `text.asString(context)`.

## One-off Events Pattern

Use `Channel` + `receiveAsFlow()` for navigation, snackbars, and other one-off events. Never put these in UI state.

```kotlin
// ViewModel - use SnackbarDelegate or create Channel directly
private val _navigateBack = Channel<Unit>()
val navigateBack = _navigateBack.receiveAsFlow()

// Screen - observe with ObserveAsEvents from utils.ui
ObserveAsEvents(viewModel.navigateBack) { onNavigateBack() }
```

For snackbars, use `SnackbarDelegate` from `utils.ui`:
```kotlin
class MyViewModel @Inject constructor() : ViewModel(), SnackbarDelegate by SnackbarDelegateImpl() {
    // Use showSnackbarSuspend(message) to show snackbars
}
```

## Use Cases

Use cases only when multiple repository calls are needed. For single repo calls, call repository directly from ViewModel.

## Feature Module Structure

```
feature/{name}/
├── data/     → API, DTOs, RepositoryImpl, Hilt module
├── domain/   → Models, Repository interface
└── ui/       → NavDestination, Screen, State, ViewModel
```

## Permissions Pattern

`TaigaPermission` enum defines all Taiga project permissions (VIEW_*, ADD_*, MODIFY_*, COMMENT_*, DELETE_* for each entity type).

**Extension functions** in `ProjectPermissions.kt` on `ImmutableList<TaigaPermission>`:
```kotlin
permissions.canAddEpic()      // checks ADD_EPIC
permissions.canModifyTask()   // checks MODIFY_TASK
permissions.hasPermission(TaigaPermission.COMMENT_US)
```

**Usage in ViewModels:** Map permission checks to state booleans:
```kotlin
data class FeatureState(
    val canAddItem: Boolean = false,
    val canModify: Boolean = false
)

// In ViewModel init or when project changes:
_state.update {
    it.copy(
        canAddItem = permissions.canAddItem(),
        canModify = permissions.canModifyItem()
    )
}
```

**UI behavior:** When permission is false, **hide** the action (don't show disabled buttons):
```kotlin
actions = buildList {
    if (state.canAddItem) {
        add(TopBarActionIconButton(...))
    }
}
```

## Offline State Pattern

Use `LocalOfflineState` (from `uikit`) to disable write actions when offline.

**Key difference from permissions:**
- No permission → **hide** action (user can never do this)
- Offline → **disable** action (user can do this, just not right now)

**Reading offline state:**
```kotlin
val isOffline = LocalOfflineState.current
```

**List screens - disable top bar add button:**
```kotlin
LaunchedEffect(state.canAddItem, isOffline) {
    topBarController.update(
        TopBarConfig(
            actions = buildList {
                if (state.canAddItem) {
                    add(TopBarActionIconButton(
                        enabled = !isOffline,
                        onClick = { ... }
                    ))
                }
            }.toImmutableList()
        )
    )
}
```

**Details screens - pass to widgets:**
```kotlin
WorkItemDropdownMenuWidget(
    canDelete = state.canDelete,
    canModify = state.canModify,
    isOffline = isOffline  // disables delete, block, promote actions
)

CreateCommentBar(
    canComment = state.canComment,
    isOffline = isOffline  // disables text field and send button
)
```

## Testing

- `:testing` module has utilities: `getRandomString()`, `MainDispatcherRule`, fake generators
- JUnit 4 + kotlin.test assertions + MockK
- Test dependencies added automatically via convention plugins

## Coding Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.
- Don't add UI elements or navigation that weren't asked for — if asked to create a settings screen, don't add a settings button to other screens unless explicitly requested.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## Error Handling

- Never swallow exceptions silently. Every `catch` block must at least log the exception with `Timber.e(e)`.

## Android/Compose Rules

- Do not use early returns in Composable functions — use conditional wrapping
- Lambda parameters: present tense (`onClick` not `onClicked`)
- Prefer `kotlinx-collections-immutable` (`ImmutableList`, `persistentListOf()`) over `List`/`MutableList` in state classes and Composable parameters for stable recomposition
- For Composable Previews, use `@PreviewTaigaDarkLight` annotation and wrap content with `TaigaMobileThemePreview` (both from `uikit`):
```kotlin
@PreviewTaigaDarkLight
@Composable
private fun MyWidgetPreview() {
    TaigaMobileThemePreview {
        MyWidget(...)
    }
}
```
- Settings screens with fixed items use `Column` instead of `LazyColumn` — lazy loading unnecessary when item count is known and small
