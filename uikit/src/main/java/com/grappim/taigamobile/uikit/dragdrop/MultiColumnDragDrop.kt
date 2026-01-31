package com.grappim.taigamobile.uikit.dragdrop

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned

@Composable
fun <T> DragDropContainer(
    state: MultiColumnDragDropState<T>,
    modifier: Modifier = Modifier,
    dragOverlay: @Composable (item: T, offset: Offset) -> Unit = { _, _ -> },
    content: @Composable () -> Unit
) {
    var containerOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            containerOffset = coordinates.boundsInRoot().topLeft
        }
    ) {
        content()

        if (state.isDragging) {
            state.draggedItem?.let { draggedItem ->
                val adjustedPosition = state.overlayPosition - containerOffset
                dragOverlay(draggedItem.data, adjustedPosition)
            }
        }
    }
}

@Composable
fun <T> DragDropColumn(
    state: MultiColumnDragDropState<T>,
    columnId: Any,
    itemCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    SideEffect {
        state.clearColumnItems(columnId)
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            state.registerColumnBounds(columnId, coordinates.boundsInRoot(), itemCount)
        }
    ) {
        content()
    }
}

@Composable
fun <T> DraggableItem(
    state: MultiColumnDragDropState<T>,
    item: T,
    itemKey: Any,
    columnId: Any,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    val isBeingDragged = state.isItemBeingDragged(itemKey)
    var itemOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val bounds = coordinates.boundsInRoot()
                itemOffset = bounds.topLeft
                state.registerItemBounds(columnId, index, itemKey, bounds)
            }
            .pointerInput(itemKey, columnId, index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { touchOffset ->
                        state.onDragStart(item, itemKey, columnId, index, itemOffset, touchOffset)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        state.onDrag(dragAmount)
                    },
                    onDragEnd = { state.onDragEnd() },
                    onDragCancel = { state.onDragCancel() }
                )
            }
            .graphicsLayer {
                alpha = if (isBeingDragged) 0.3f else 1f
            }
    ) {
        content(state.isDragging)
    }
}

@Composable
fun DropIndicator(visible: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (visible) {
        Box(modifier = modifier) {
            content()
        }
    }
}
