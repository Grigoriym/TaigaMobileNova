package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class WorkItemTitleDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemTitleDelegate {

    private val _titleState = MutableStateFlow(
        WorkItemTitleState(
            onTitleChange = ::onTitleChange,
            setIsTitleEditable = ::setIsTitleEditable,
            onCancelClick = ::onCancelClick
        )
    )
    override val titleState: StateFlow<WorkItemTitleState> = _titleState.asStateFlow()

    override suspend fun handleTitleSave(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        val currentState = _titleState.value

        if (currentState.originalTitle == currentState.currentTitle) {
            onTitleSaveCancel()
            return
        }
        _titleState.update {
            it.copy(
                isTitleLoading = true,
                titleError = NativeText.Empty
            )
        }

        resultOf {
            val payload = patchDataGenerator.getTitle(currentState.currentTitle)
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData.newVersion)

            _titleState.update { state ->
                state.copy(
                    originalTitle = state.currentTitle,
                    isTitleEditable = false,
                    isTitleLoading = false,
                    titleError = NativeText.Empty
                )
            }
        }.onFailure { error ->
            _titleState.update {
                it.copy(
                    titleError = getErrorMessage(error),
                    isTitleLoading = false
                )
            }
            doOnError(error)
        }
    }

    override fun setInitialTitle(title: String) {
        _titleState.update { currentState ->
            currentState.copy(
                currentTitle = title,
                originalTitle = title
            )
        }
    }

    private fun onCancelClick() {
        _titleState.update {
            it.copy(
                isTitleEditable = false,
                currentTitle = it.originalTitle,
                titleError = NativeText.Empty,
                isTitleLoading = false
            )
        }
    }

    private fun onTitleChange(newTitle: String) {
        _titleState.update {
            it.copy(currentTitle = newTitle)
        }
    }

    private fun setIsTitleEditable(isEditable: Boolean) {
        _titleState.update {
            it.copy(isTitleEditable = isEditable)
        }
    }

    private fun onTitleSaveCancel() {
        _titleState.update { currentState ->
            currentState.copy(
                isTitleEditable = false,
                isTitleLoading = false,
                titleError = NativeText.Empty
            )
        }
    }
}
