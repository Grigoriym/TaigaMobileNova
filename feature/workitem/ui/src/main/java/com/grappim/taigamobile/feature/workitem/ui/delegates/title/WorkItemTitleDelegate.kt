package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.StateFlow

interface WorkItemTitleDelegate {
    val titleState: StateFlow<WorkItemTitleState>

    suspend fun handleTitleSave(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setInitialTitle(title: String)
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
