---
name: uikit-guide
description: Use this agent when building new UI in TaigaMobileNova to check what reusable components exist in the uikit module, how to use TopBarController, offline state, previews, and theme/styling. Consult before creating a new Composable widget to avoid reinventing something that already exists.
tools: Read, Grep, Glob
model: sonnet
---

You are the uikit reference for TaigaMobileNova. Help write consistent UI by surfacing existing components and patterns. Always read source files before giving specific advice.

All uikit source: `uikit/src/commonMain/kotlin/com/grappim/taigamobile/uikit/`

---

## Theme & Styling

**Theme wrappers** (`theme/Theme.kt`):
```kotlin
TaigaMobileTheme(darkTheme: Boolean = isSystemInDarkTheme(), content)  // production
TaigaMobilePreviewTheme(content)  // previews — wraps with TaigaMobileTheme + Surface
```

**Colors** (`theme/Colors.kt`):
- `taigaGreen` (#25A28C) — primary brand
- `taigaGreenDark` (#00796D) — secondary
- `taigaGreenLight` (#2CC0A6)
- `taigaOrange` (#FF9900) — offline/warning
- `taigaGreenPositive` (#9DCE0A) — success
- `taigaRed` (#E44057) — error/blocked
- `taigaGray` (#A9AABC) — neutral

**Dimensions** (`theme/Dimens.kt`):
```kotlin
mainHorizontalScreenPadding = 12.dp
commonVerticalPadding = 24.dp
cardShadowElevation = 8.dp
dialogTonalElevation = 6.dp
kanbanBoardTonalElevation = 4.dp
```

---

## Annotations

**Preview** (`utils/PreviewTaigaDarkLight.kt`):
```kotlin
@PreviewTaigaDarkLight
@Composable
private fun MyWidgetPreview() {
    TaigaMobilePreviewTheme {
        MyWidget(...)
    }
}
```
Generates two previews: light + dark. Always pair with `TaigaMobilePreviewTheme`.

---

## Composition Locals

| Local | Type | Usage |
|-------|------|-------|
| `LocalOfflineState` | `Boolean` | `val isOffline = LocalOfflineState.current` — disable write actions when true |
| `LocalTopBarConfig` | `TopBarController` | `val topBarController = LocalTopBarConfig.current` |
| `LocalScreenReadySignal` | `ScreenReadySignalController` | `utils/ScreenReadySignalController.kt` — call `signalReady()` when the screen is ready, so the splash can hide without flashing an intermediate backstack screen |

`LocalOfflineState` is in `state/LocalOfflineState.kt`, `LocalTopBarConfig` in `widgets/topbar/TopBarController.kt`.

---

## TopBar

**Controller** (`widgets/topbar/TopBarController.kt`):
```kotlin
val topBarController = LocalTopBarConfig.current
topBarController.update(TopBarConfig(
    title = NativeText.Resource(Res.string.my_title),
    navigationIcon = NavigationIconConfig.Back(),
    actions = buildList {
        if (state.canAdd) {
            add(TopBarActionIconButton(
                drawable = RDrawable.ic_add,
                enabled = !isOffline,
                onClick = { ... }
            ))
        }
    }.toImmutableList()
))
```
- Update in `LaunchedEffect` keyed on state that affects the bar
- Call `topBarController.reset()` if the screen doesn't own the top bar

**Config types** (`widgets/topbar/TopBarConfig.kt`) — the rendering lives in `TaigaTopAppBar.kt`:

```kotlin
data class TopBarConfig(
    val title: NativeText = NativeText.Empty,
    val subtitle: NativeText = NativeText.Empty,       // second line, often the project name
    val navigationIcon: NavigationIconConfig = NavigationIconConfig.None,
    val actions: ImmutableList<TopBarAction> = persistentListOf()
)
```

| `TopBarAction` | Params |
|----------------|--------|
| `TopBarActionIconButton` | `drawable: DrawableResource, contentDescription, enabled, onClick` |
| `TopBarActionTextButton` | `text: NativeText, enabled, onClick` |

| `NavigationIconConfig` | Use |
|------------------------|-----|
| `None` | no navigation icon (default) |
| `Back(onBackClick: (() -> Unit)? = null)` | back arrow; omit the lambda for default pop |
| `Menu` | drawer hamburger |
| `Custom(icon, contentDescription, onClick)` | anything else |

---

## Buttons

| Component | File | Key params |
|-----------|------|------------|
| `AddButtonWidget` | `widgets/button/AddButtonWidget.kt` | `isOffline, text, onClick` — text button with + icon |
| `PlusButtonWidget` | `widgets/button/PlusButtonWidget.kt` | `isOffline, tint, onClick` — 32dp circular icon button |
| `TaigaOutlinedButton` | `widgets/button/TaigaOutlinedButton.kt` | `text, onClick, painter/imageVector?` — outlined with optional icon |
| `TaigaTextButtonWidget` | `widgets/button/TaigaTextButtonWidget.kt` | `text, isOffline, onClick, icon?` — filled tonal, disabled when offline |

---

## Input

| Component | File | Key params |
|-----------|------|------------|
| `HintTextField` | `widgets/editor/TextFieldWithHint.kt` | `value, onValueChange, hint: NativeText, error: NativeText, singleLine` |
| `TextFieldWithHint` | `widgets/editor/TextFieldWithHint.kt` | `hintId: StringResource, value: TextFieldValue` — lower-level with more options |
| `TextFieldStringWithHint` | `widgets/editor/TextFieldWithHint.kt` | Same as above but `value: String` |
| `CreateCommentBar` | `widgets/CreateCommentBar.kt` | `isOffline, onButtonClick, canComment` — only renders if `canComment = true` |
| `DatePickerDialogWidget` | `widgets/DatePickerDialogWidget.kt` | `isVisible, onConfirmButtonClick: (Long?) -> Unit, initialDate?` |
| `DropdownSelector<T>` | `widgets/DropdownSelector.kt` | `items, selectedItem, onItemSelect, isOffline, canModify, itemContent, selectedItemContent` |

---

## Dialogs & Loaders

| Component | File | Notes |
|-----------|------|-------|
| `ConfirmActionDialog` | `widgets/dialog/ConfirmActionDialog.kt` | `isVisible, onConfirm, onDismiss, title?, description?, iconId?` — Yes/No dialog |
| `TaigaLoadingDialog` | `widgets/dialog/TaigaLoadingDialog.kt` | `isVisible` — full-screen centered progress indicator |
| `LoadingDialog` | `widgets/dialog/LoadingDialog.kt` | no params — compact Row dialog: spinner + "Loading" text, non-dismissible; use for blocking operations |
| `CircularLoaderWidget` | `widgets/loader/CircularLoaderWidget.kt` | 40dp centered spinner |
| `DotsLoaderWidget` | `widgets/loader/DotsLoaderWidget.kt` | Three pulsing dots animation |

---

## State Widgets

| Component | File | Notes |
|-----------|------|-------|
| `EmptyStateWidget` | `widgets/emptystate/EmptyStateWidget.kt` | `message: NativeText, icon?, action: EmptyStateAction?` |
| `ErrorStateWidget` | `widgets/ErrorStateWidget.kt` | `message: NativeText, onRetry` |
| `OfflineIndicatorBanner` | `widgets/banner/OfflineIndicatorBanner.kt` | `isOffline` — animated orange banner |

---

## Text & Lists

| Component | File | Notes |
|-----------|------|-------|
| `SectionTitle` | `widgets/text/SectionTitle.kt` | `text, horizontalPadding, onAddClick?` — section header with optional + |
| `SectionTitleExpandable` | `widgets/text/SectionTitle.kt` | `text, isExpanded, onExpandClick` — with animated arrow |
| `CommonTaskTitle` | `widgets/text/CommonTaskTitle.kt` | `ref, title, isInactive, indicatorColorsHex, tags, isBlocked` |
| `MarkdownTextWidget` | `widgets/text/MarkdownTextWidget.kt` | `text, onClick?` — full markdown renderer |
| `ExpandableMarkdownText` | `widgets/text/ExpandableMarkdownText.kt` | `text, maxLinesCollapsed` — collapses with "Show more" |
| `UserItem` | `widgets/list/UserItem.kt` | `displayName, avatarUrl, dateTime?, onUserItemClick` |
| `CommonTaskItem` | `widgets/list/CommonTaskItem.kt` | ⚠️ Deprecated — use `simpleTasksListWithTitle` instead |
| `simpleTasksListWithTitle` | `widgets/list/SimpleTasksListWithTitle.kt` | `LazyListScope` extension for task lists with title, paging, dividers |

---

## Misc Widgets

| Component | File | Notes |
|-----------|------|-------|
| `BadgeWidget` | `widgets/badge/BadgeWidget.kt` | `text, isActive` — simple status badge |
| `ClickableBadge` | `widgets/badge/ClickableBadge.kt` | `text, color/colorHex, isLoading, isClickable, onClick` |
| `ChipWidget` | `widgets/ChipWidget.kt` | `onClick?, color, content` — rounded chip, optionally clickable |
| `TaigaIcon` | `widgets/icon/TaigaIcon.kt` | `painter/imageVector, tint` — icon with testTag support |
| `TaigaHeightSpacer` | `widgets/TaigaSpacers.kt` | `height: Dp` |
| `TaigaWidthSpacer` | `widgets/TaigaSpacers.kt` | `width: Dp` |

---

## Drag & Drop (Kanban)

For the Kanban board use the multi-column drag-drop system (`dragdrop/`):
- `rememberMultiColumnDragDropState<T>(onMove)` — state holder
- `DragDropContainer` — root container with overlay
- `DragDropColumn` — column that accepts drops
- `DraggableItem` — individual draggable item
- `DropIndicator` — visual drop target

---

## Rules

- Always check this guide before creating a new widget — something likely already exists
- `NativeText` (from `utils:ui`) for all strings from ViewModels; resolve with `.asString()` in Composables
- `ImmutableList` / `persistentListOf()` in state classes and Composable params
- No early returns in Composables — use conditional wrapping
- Lambda params: present tense (`onClick` not `onClicked`)
- When offline: **disable** write actions (pass `isOffline` to widgets). When no permission: **hide** the action entirely.
