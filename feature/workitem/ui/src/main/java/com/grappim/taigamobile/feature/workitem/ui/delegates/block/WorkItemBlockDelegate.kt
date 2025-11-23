package com.grappim.taigamobile.feature.workitem.ui.delegates.block

import com.grappim.taigamobile.core.domain.patch.PatchedData
import kotlinx.coroutines.flow.StateFlow

interface WorkItemBlockDelegate {
    val blockState: StateFlow<WorkItemBlockState>

    suspend fun handleBlockToggle(
        isBlocked: Boolean,
        blockNote: String?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((BlockToggleResult) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setIsBlockDialogVisible(isVisible: Boolean)
}

data class WorkItemBlockState(
    val isBlockDialogVisible: Boolean = false,
    val setIsBlockDialogVisible: (Boolean) -> Unit = {}
)

data class BlockToggleResult(val blockNote: String?, val patchedData: PatchedData)
