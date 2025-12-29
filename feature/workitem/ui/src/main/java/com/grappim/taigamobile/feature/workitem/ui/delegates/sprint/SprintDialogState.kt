package com.grappim.taigamobile.feature.workitem.ui.delegates.sprint

import com.grappim.taigamobile.utils.ui.NativeText
import java.time.LocalDate

data class SprintDialogState(
    val sprintNameValue: String = "",
    val onSetSprintNameValue: (String) -> Unit = {},
    val isSprintDialogVisible: Boolean = false,
    val sprintNameError: NativeText = NativeText.Empty,
    val dialogError: NativeText = NativeText.Empty,
    val onDismiss: () -> Unit = {},

    val startDate: LocalDate? = null,
    val startDateToDisplay: String = "",
    val isStartDateDialogVisible: Boolean = false,
    val setIsStartDateDialogVisible: (Boolean) -> Unit,
    val onStartDateDismissRequest: () -> Unit,
    val onStartDateDismissButonClick: () -> Unit,
    val onStartDateConfirmButtonClick: (Long?) -> Unit,

    val endDate: LocalDate? = null,
    val endDateToDisplay: String = "",
    val isEndDateDialogVisible: Boolean = false,
    val setIsEndDateDialogVisible: (Boolean) -> Unit,
    val onEndDateDismissRequest: () -> Unit,
    val onEndDateDismissButonClick: () -> Unit,
    val onEndDateConfirmButtonClick: (Long?) -> Unit
)
