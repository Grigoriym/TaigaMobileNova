package com.grappim.taigamobile.uikit.dragdrop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

data class DragDropItem<T>(val data: T, val key: Any)

data class DropTargetInfo(val columnId: Any, val insertIndex: Int, val beforeItemKey: Any?, val afterItemKey: Any?)

@Stable
class MultiColumnDragDropState<T>(
    private val onMove: (item: T, targetColumnId: Any, beforeItemKey: Any?, afterItemKey: Any?) -> Unit
) {
    var draggedItem by mutableStateOf<DragDropItem<T>?>(null)
        private set

    var sourceColumnId by mutableStateOf<Any?>(null)
        private set

    var sourceIndex by mutableIntStateOf(-1)
        private set

    var dragOffset by mutableStateOf(Offset.Zero)
        private set

    var initialOffset by mutableStateOf(Offset.Zero)
        private set

    var touchOffsetInItem by mutableStateOf(Offset.Zero)
        private set

    var targetColumnId by mutableStateOf<Any?>(null)
        private set

    var targetInsertIndex by mutableIntStateOf(-1)
        private set

    internal val columnBounds = mutableStateMapOf<Any, Rect>()
    internal val columnItemCounts = mutableStateMapOf<Any, Int>()
    internal val itemBounds = mutableStateMapOf<Any, MutableMap<Int, Rect>>()
    internal val itemKeys = mutableStateMapOf<Any, MutableMap<Int, Any>>()

    val isDragging: Boolean
        get() = draggedItem != null

    val currentDragPosition: Offset
        get() = initialOffset + touchOffsetInItem + dragOffset

    val overlayPosition: Offset
        get() = initialOffset + dragOffset

    fun isItemBeingDragged(itemKey: Any): Boolean = draggedItem?.key == itemKey

    fun isTargetColumn(columnId: Any): Boolean = targetColumnId == columnId

    fun getTargetIndexForColumn(columnId: Any): Int = if (targetColumnId == columnId) targetInsertIndex else -1

    internal fun onDragStart(item: T, key: Any, columnId: Any, index: Int, itemOffset: Offset, touchOffset: Offset) {
        draggedItem = DragDropItem(item, key)
        sourceColumnId = columnId
        sourceIndex = index
        initialOffset = itemOffset
        touchOffsetInItem = touchOffset
        dragOffset = Offset.Zero
    }

    internal fun onDrag(change: Offset) {
        dragOffset += change
        updateTargetPosition()
    }

    internal fun onDragEnd() {
        val item = draggedItem
        val target = findTargetPosition(currentDragPosition)

        if (item != null && target != null && !isNoOpMove(target)) {
            onMove(
                item.data,
                target.columnId,
                target.beforeItemKey,
                target.afterItemKey
            )
        }

        resetDragState()
    }

    internal fun onDragCancel() {
        resetDragState()
    }

    internal fun registerColumnBounds(columnId: Any, bounds: Rect, itemCount: Int) {
        columnBounds[columnId] = bounds
        columnItemCounts[columnId] = itemCount
    }

    internal fun clearColumnItems(columnId: Any) {
        itemBounds[columnId]?.clear()
        itemKeys[columnId]?.clear()
    }

    internal fun registerItemBounds(columnId: Any, index: Int, key: Any, bounds: Rect) {
        val columnItems = itemBounds.getOrPut(columnId) { mutableMapOf() }
        columnItems[index] = bounds

        val columnKeys = itemKeys.getOrPut(columnId) { mutableMapOf() }
        columnKeys[index] = key
    }

    private fun updateTargetPosition() {
        val target = findTargetPosition(currentDragPosition)
        if (target != null && !isNoOpMove(target)) {
            targetColumnId = target.columnId
            targetInsertIndex = target.insertIndex
        } else {
            targetColumnId = null
            targetInsertIndex = -1
        }
    }

    private fun findTargetPosition(position: Offset): DropTargetInfo? {
        val (columnId, _) = columnBounds.entries.find { it.value.contains(position) } ?: return null
        return findInsertPositionInColumn(columnId, position.y)
    }

    private fun findInsertPositionInColumn(columnId: Any, yPosition: Float): DropTargetInfo {
        val itemCount = columnItemCounts[columnId] ?: 0
        val columnItemBounds = itemBounds[columnId]
        val columnItemKeys = itemKeys[columnId]

        if (itemCount == 0 || columnItemBounds.isNullOrEmpty() || columnItemKeys.isNullOrEmpty()) {
            return emptyColumnDropTarget(columnId)
        }

        val draggedKey = draggedItem?.key
        val insertIndex = findInsertIndex(columnItemBounds, itemCount, yPosition)

        return if (insertIndex < itemCount) {
            DropTargetInfo(
                columnId = columnId,
                insertIndex = insertIndex,
                beforeItemKey = getKeyAt(columnItemBounds, columnItemKeys, insertIndex, draggedKey),
                afterItemKey = getKeyAt(columnItemBounds, columnItemKeys, insertIndex - 1, draggedKey)
            )
        } else {
            DropTargetInfo(
                columnId = columnId,
                insertIndex = itemCount,
                beforeItemKey = null,
                afterItemKey = getKeyAt(columnItemBounds, columnItemKeys, itemCount - 1, draggedKey)
            )
        }
    }

    private fun emptyColumnDropTarget(columnId: Any) = DropTargetInfo(
        columnId = columnId,
        insertIndex = 0,
        beforeItemKey = null,
        afterItemKey = null
    )

    private fun findInsertIndex(columnItemBounds: Map<Int, Rect>, itemCount: Int, yPosition: Float): Int {
        for (index in 0 until itemCount) {
            val itemRect = columnItemBounds[index] ?: continue
            if (yPosition < itemRect.center.y) return index
        }
        return itemCount
    }

    private fun getKeyAt(bounds: Map<Int, Rect>, keys: Map<Int, Any>, index: Int, excludeKey: Any?): Any? {
        if (index < 0) return null
        return bounds[index]?.let { keys[index] }?.takeIf { it != excludeKey }
    }

    private fun isNoOpMove(target: DropTargetInfo): Boolean {
        if (sourceColumnId != target.columnId) return false
        return target.insertIndex == sourceIndex || target.insertIndex == sourceIndex + 1
    }

    private fun resetDragState() {
        draggedItem = null
        sourceColumnId = null
        sourceIndex = -1
        dragOffset = Offset.Zero
        initialOffset = Offset.Zero
        touchOffsetInItem = Offset.Zero
        targetColumnId = null
        targetInsertIndex = -1
    }
}

@Composable
fun <T> rememberMultiColumnDragDropState(
    onMove: (item: T, targetColumnId: Any, beforeItemKey: Any?, afterItemKey: Any?) -> Unit
): MultiColumnDragDropState<T> = remember {
    MultiColumnDragDropState(onMove)
}
