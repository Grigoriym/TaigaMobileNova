# CLAUDE.md

TaigaMobileNova is an unofficial Android client for Taiga.io. Built with Kotlin, Jetpack Compose, and follows a modular MVVM + Clean Architecture.

## Architecture

**Module structure:** `app/` → `feature/` → `core/` → `utils/`
- Features have data/domain/ui layers (sometimes dto/mapper)
- Use `NativeText` (in `utils:ui`) for localized strings in ViewModels
- Dependencies via Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Build plugins in `build-logic/`

**Stack:** Kotlin, Compose + Material3, Hilt, Navigation Compose, Retrofit + kotlinx.serialization, Coroutines, Coil

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

## Feature Module Structure

```
feature/{name}/
├── data/     → API, DTOs, RepositoryImpl, Hilt module
├── domain/   → Models, Repository interface
└── ui/       → NavDestination, Screen, State, ViewModel
```

## Testing

```bash
# Run tests (use fdroid or gplay variant)
./gradlew :module:path:testFdroidDebugUnitTest --tests "com.package.TestClass"
```

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

---

**These guidelines are working if:** fewer unnecessary changes in diffs, fewer rewrites due to overcomplication, and clarifying questions come before implementation rather than after mistakes.

## Android/Compose Rules

- Do not use early returns in Composable functions — use conditional wrapping
- Lambda parameters: present tense (`onClick` not `onClicked`)
