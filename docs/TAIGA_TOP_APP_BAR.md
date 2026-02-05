# TaigaTopAppBar Component Guide

This document explains how the `TaigaTopAppBar` component is implemented and used in TaigaMobileNova. Use this as a reference for implementing a similar global top bar pattern in other Compose applications.

## Overview

`TaigaTopAppBar` is a global, screen-controlled top bar component that uses Material3's `CenterAlignedTopAppBar`. Each screen configures the top bar through a centralized controller, eliminating prop drilling and providing consistent behavior across the app.

**Key characteristics:**
- Global state via `CompositionLocal`
- Screen-level configuration ownership
- Immutable configuration objects
- Localization-ready with `NativeText`
- Integrated drawer and keyboard handling

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  MainContent                                                │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  CompositionLocalProvider(LocalTopBarConfig)          │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │  Scaffold                                       │  │  │
│  │  │  ├── TaigaTopAppBar (reads topBarController)    │  │  │
│  │  │  └── NavHost                                    │  │  │
│  │  │      ├── ScreenA (updates topBarController)     │  │  │
│  │  │      ├── ScreenB (updates topBarController)     │  │  │
│  │  │      └── ...                                    │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. TopBarConfig

Immutable data class containing all top bar configuration:

```kotlin
data class TopBarConfig(
    val title: NativeText = NativeText.Empty,
    val subtitle: NativeText = NativeText.Empty,
    val navigationIcon: NavigationIconConfig = NavigationIconConfig.None,
    val actions: ImmutableList<TopBarAction> = persistentListOf()
)
```

### 2. NavigationIconConfig

Sealed interface for navigation icon options:

```kotlin
sealed interface NavigationIconConfig {
    object None : NavigationIconConfig
    data class Back(val onBackClick: (() -> Unit)? = null) : NavigationIconConfig
    object Menu : NavigationIconConfig
    data class Custom(
        @DrawableRes val icon: Int,
        val contentDescription: String,
        val onClick: () -> Unit
    ) : NavigationIconConfig
}
```

- **None**: No navigation icon displayed
- **Back**: Arrow back icon; uses custom callback or falls back to `defaultGoBack`
- **Menu**: Hamburger menu icon; toggles drawer state automatically
- **Custom**: Any drawable with custom click handler

### 3. TopBarAction

Sealed interface for action buttons (right side):

```kotlin
sealed interface TopBarAction {
    val onClick: () -> Unit
}

data class TopBarActionIconButton(
    @DrawableRes val drawable: Int,
    val contentDescription: String = "",
    override val onClick: () -> Unit
) : TopBarAction

data class TopBarActionTextButton(
    val text: NativeText,
    override val onClick: () -> Unit
) : TopBarAction
```

### 4. TopBarController

Mutable state holder for the current configuration:

```kotlin
val LocalTopBarConfig = compositionLocalOf<TopBarController> {
    error("TopBarController not provided")
}

class TopBarController {
    var config by mutableStateOf(TopBarConfig())
        private set

    fun update(config: TopBarConfig) {
        this.config = config
    }

    fun reset() {
        config = TopBarConfig()
    }
}
```

### 5. TaigaTopAppBar

The actual composable that renders the top bar:

```kotlin
@Composable
fun TaigaTopAppBar(
    isVisible: Boolean,
    drawerState: DrawerState,
    topBarConfig: TopBarConfig,
    defaultGoBack: () -> Unit,
    modifier: Modifier = Modifier
)
```

**Parameters:**
- `isVisible`: Controls whether the top bar is shown
- `drawerState`: Material3 drawer state for menu icon integration
- `topBarConfig`: Current configuration from controller
- `defaultGoBack`: Fallback navigation callback (typically `navController.popBackStack()`)

## NativeText (Localization)

`NativeText` enables ViewModel-friendly localized strings:

```kotlin
sealed class NativeText {
    data object Empty : NativeText()
    data class Simple(val text: String) : NativeText()
    data class Resource(@StringRes val id: Int) : NativeText()
    data class Plural(@PluralsRes val id: Int, val number: Int, val args: List<Any>) : NativeText()
    data class Arguments(@StringRes val id: Int, val args: List<Any>) : NativeText()
    data class Multi(val text: List<NativeText>) : NativeText()
}

// Resolve in UI layer
fun NativeText.asString(context: Context): String
```

## Setup

### 1. Create and Provide the Controller

In your main composable (typically where your `Scaffold` lives):

```kotlin
@Composable
fun MainContent() {
    val topBarController = remember { TopBarController() }

    CompositionLocalProvider(LocalTopBarConfig provides topBarController) {
        val topBarConfig = topBarController.config

        Scaffold(
            topBar = {
                TaigaTopAppBar(
                    isVisible = /* your visibility logic */,
                    topBarConfig = topBarConfig,
                    drawerState = drawerState,
                    defaultGoBack = { navController.popBackStack() }
                )
            }
        ) { paddingValues ->
            NavHost(
                modifier = Modifier.padding(paddingValues),
                // ...
            )
        }
    }
}
```

### 2. Configure from Screens

Each screen updates the top bar via `LaunchedEffect`:

```kotlin
@Composable
fun MyScreen() {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(R.string.my_screen_title),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    // Screen content...
}
```

## Usage Patterns

### Pattern 1: Menu Navigation (Root/List Screens)

For screens accessible via navigation drawer:

```kotlin
LaunchedEffect(Unit) {
    topBarController.update(
        TopBarConfig(
            title = NativeText.Resource(RString.kanban),
            navigationIcon = NavigationIconConfig.Menu
        )
    )
}
```

### Pattern 2: Back Navigation with Options Menu

For detail screens with overflow menu:

```kotlin
LaunchedEffect(Unit) {
    topBarController.update(
        TopBarConfig(
            title = state.toolbarTitle,
            navigationIcon = NavigationIconConfig.Back(
                onBackClick = { goBack() }
            ),
            actions = persistentListOf(
                TopBarActionIconButton(
                    drawable = RDrawable.ic_options,
                    contentDescription = "Options",
                    onClick = { state.setDropdownMenuExpanded(true) }
                )
            )
        )
    )
}
```

### Pattern 3: Back Navigation with Save Button

For edit screens:

```kotlin
LaunchedEffect(Unit) {
    topBarController.update(
        TopBarConfig(
            title = NativeText.Resource(RString.edit_sprint),
            navigationIcon = NavigationIconConfig.Back(
                onBackClick = { state.setIsDialogVisible(true) }  // Show discard dialog
            ),
            actions = persistentListOf(
                TopBarActionTextButton(
                    text = NativeText.Resource(RString.save),
                    onClick = { state.onSave() }
                )
            )
        )
    )
}
```

### Pattern 4: Dynamic Title with Subtitle

For screens with changing content:

```kotlin
// Re-run when title or subtitle changes
LaunchedEffect(state.title, state.subtitle) {
    topBarController.update(
        TopBarConfig(
            title = state.title,
            subtitle = state.subtitle,
            navigationIcon = NavigationIconConfig.Back(onBackClick = { goBack() }),
            actions = buildList {
                if (state.canShowActions) {
                    add(TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "",
                        onClick = { state.setIsMenuExpanded(true) }
                    ))
                }
            }.toImmutableList()
        )
    )
}
```

### Pattern 5: Conditional Navigation Icon

For screens with multiple entry points:

```kotlin
LaunchedEffect(Unit) {
    topBarController.update(
        TopBarConfig(
            title = NativeText.Resource(RString.project_selector),
            navigationIcon = if (state.isFromLogin) {
                NavigationIconConfig.Back(onBackClick = { goBack() })
            } else {
                NavigationIconConfig.Menu
            }
        )
    )
}
```

## Implementation Details

### Menu Icon Behavior

When `NavigationIconConfig.Menu` is used, the component:
1. Hides the software keyboard
2. Toggles the drawer open/closed state

```kotlin
IconButton(onClick = {
    keyboardController?.hide()
    scope.launch {
        if (drawerState.isClosed) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }
})
```

### Back Icon Fallback

If `NavigationIconConfig.Back.onBackClick` is null, `defaultGoBack` is used:

```kotlin
IconButton(
    onClick = navigationIconConfig.onBackClick ?: defaultGoBack
)
```

### Title and Subtitle

Both are center-aligned with ellipsis overflow:

```kotlin
Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
        text = topBarConfig.title.asString(context),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    if (topBarConfig.subtitle !is NativeText.Empty) {
        Text(
            text = topBarConfig.subtitle.asString(context),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
```

## Dependencies

```kotlin
// build.gradle.kts
implementation("androidx.compose.material3:material3:...")
implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:...")
```

Required imports:
```kotlin
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.compositionLocalOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
```

## Best Practices

1. **Use `LaunchedEffect` keys wisely**: Include state values that affect the top bar config as keys to ensure updates propagate.

2. **Prefer `persistentListOf`**: Use immutable lists from kotlinx-collections-immutable to avoid unnecessary recompositions.

3. **Keep callbacks in config**: Embed onClick lambdas directly in the config rather than passing them separately.

4. **Use `NativeText`**: Never pass raw strings from ViewModels; use `NativeText` for proper localization.

5. **Handle back navigation carefully**: For edit screens, consider showing a discard dialog via custom `onBackClick`.

6. **Match `LaunchedEffect` keys**: When title/subtitle come from state, include them in `LaunchedEffect` keys:
   ```kotlin
   LaunchedEffect(state.title, state.subtitle) { ... }
   ```

## File Locations (TaigaMobileNova)

- Component: `uikit/src/main/java/.../widgets/topbar/TaigaTopAppBar.kt`
- Config: `uikit/src/main/java/.../widgets/topbar/TopBarConfig.kt`
- Controller: `uikit/src/main/java/.../widgets/topbar/TopBarController.kt`
- NativeText: `utils/ui/src/main/java/.../NativeText.kt`
- Main setup: `app/src/main/kotlin/.../main/MainScreen.kt`
