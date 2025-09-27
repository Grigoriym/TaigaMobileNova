package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemTitleDelegateImpl : WorkItemTitleDelegate {

    private val _titleState = MutableStateFlow(
        WorkItemTitleState(
            onTitleChange = ::onTitleChange,
            setIsTitleEditable = ::setIsTitleEditable,
            onCancelClick = ::onCancelClick
        )
    )
    override val titleState: StateFlow<WorkItemTitleState> = _titleState.asStateFlow()

    override fun onTitleSave(onSaveTitleToBackend: () -> Unit) {
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

        onSaveTitleToBackend()
    }

    override fun onTitleError(error: NativeText) {
        _titleState.update {
            it.copy(
                titleError = error,
                isTitleLoading = false
            )
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

    override fun onTitleSaveSuccess() {
        _titleState.update { currentState ->
            currentState.copy(
                originalTitle = currentState.currentTitle,
                isTitleEditable = false,
                isTitleLoading = false,
                titleError = NativeText.Empty
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
