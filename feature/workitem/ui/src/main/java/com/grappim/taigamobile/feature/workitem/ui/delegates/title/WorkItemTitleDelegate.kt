package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.StateFlow

interface WorkItemTitleDelegate {
    val titleState: StateFlow<WorkItemTitleState>

    fun onTitleSave(onSaveTitleToBackend: () -> Unit)
    fun onTitleError(error: NativeText)
    fun setInitialTitle(title: String)
    fun onTitleSaveSuccess()
}

data class WorkItemTitleState(
    val currentTitle: String = "",
    val originalTitle: String = "",
    val isTitleEditable: Boolean = false,
    val isTitleLoading: Boolean = false,
    val titleError: NativeText = NativeText.Empty,
    val onTitleChange: (String) -> Unit = {},
    val setIsTitleEditable: (Boolean) -> Unit = {},
    val onCancelClick: () -> Unit = {}
)
