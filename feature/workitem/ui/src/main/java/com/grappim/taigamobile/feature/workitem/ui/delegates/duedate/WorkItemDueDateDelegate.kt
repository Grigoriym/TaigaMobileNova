package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface WorkItemDueDateDelegate {
    val dueDateState: StateFlow<WorkItemDueDateState>

    fun setInitialDueDate(dueDateText: NativeText)

    suspend fun handleDueDateSave(
        newDate: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((DueDateSaveResult) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setDueDateDatePickerVisibility(isVisible: Boolean)
}

data class WorkItemDueDateState(
    val dueDateText: NativeText = NativeText.Empty,
    val isDueDateDatePickerVisible: Boolean = false,
    val isDueDateLoading: Boolean = false,
    val setDueDateDatePickerVisibility: (Boolean) -> Unit = {}
)

data class DueDateSaveResult(val dueDate: LocalDate?, val patchedData: PatchedData)
