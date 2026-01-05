package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import androidx.compose.material3.MaterialTheme
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.utils.ui.ColorSource
import com.grappim.taigamobile.utils.ui.DynamicColor
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface WorkItemDueDateDelegate {
    val dueDateState: StateFlow<WorkItemDueDateState>

    fun setInitialDueDate(dueDate: LocalDate?, dueDateStatus: DueDateStatus?)

    suspend fun handleDueDateSave(
        newDate: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((PatchedData) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setDueDateDatePickerVisibility(isVisible: Boolean)
}

data class WorkItemDueDateState(
    val dueDate: LocalDate? = null,
    val dueDateStatus: DueDateStatus? = null,
    val dueDateText: NativeText = NativeText.Empty,
    val isDueDateDatePickerVisible: Boolean = false,
    val isDueDateLoading: Boolean = false,
    val backgroundColor: ColorSource = DynamicColor { MaterialTheme.colorScheme.primary },
    val setDueDateDatePickerVisibility: (Boolean) -> Unit = {}
)
