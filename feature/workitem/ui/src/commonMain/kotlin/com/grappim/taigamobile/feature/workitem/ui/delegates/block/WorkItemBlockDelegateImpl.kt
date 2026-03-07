package com.grappim.taigamobile.feature.workitem.ui.delegates.block

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemBlockDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemBlockDelegate {

    private val _blockState = MutableStateFlow(
        WorkItemBlockState(
            setIsBlockDialogVisible = ::setIsBlockDialogVisible
        )
    )
    override val blockState: StateFlow<WorkItemBlockState> = _blockState.asStateFlow()

    override suspend fun handleBlockToggle(
        isBlocked: Boolean,
        blockNote: String?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((BlockToggleResult) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()

        resultOf {
            val payload = patchDataGenerator.getBlockedPatchPayload(isBlocked, blockNote)
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(
                BlockToggleResult(
                    blockNote = blockNote,
                    patchedData = patchedData
                )
            )
        }.onFailure { error ->
            doOnError(error)
        }
    }

    override fun setIsBlockDialogVisible(isVisible: Boolean) {
        _blockState.update {
            it.copy(isBlockDialogVisible = isVisible)
        }
    }
}
