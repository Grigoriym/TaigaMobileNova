package com.grappim.taigamobile.feature.workitem.ui.delegates.sprint

import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.time.LocalDate

class WorkItemSprintDelegateImpl(
    private val dateTimeUtils: DateTimeUtils,
    private val sprintsRepository: SprintsRepository
) : WorkItemSprintDelegate {

    private val _sprintDialogState = MutableStateFlow(
        SprintDialogState(
            onSetSprintNameValue = ::onSetSprintNameValue,
            onDismiss = ::onDismiss,
            setIsStartDateDialogVisible = ::setIsStartDateDialogVisible,
            onStartDateDismissRequest = ::onStartDateDismissRequest,
            onStartDateDismissButonClick = ::onStartDateDismissButonClick,
            onStartDateConfirmButtonClick = ::onStartDateConfirmButtonClick,
            setIsEndDateDialogVisible = ::setIsEndDateDialogVisible,
            onEndDateDismissRequest = ::onEndDateDismissRequest,
            onEndDateDismissButonClick = ::onEndDateDismissButonClick,
            onEndDateConfirmButtonClick = ::onEndDateConfirmButtonClick
        )
    )
    override val sprintDialogState: StateFlow<SprintDialogState> = _sprintDialogState.asStateFlow()

    override fun setInitialSprint(start: LocalDate?, end: LocalDate?, sprintName: String) {
        val defaultStart = start ?: dateTimeUtils.getLocalDateNow()
        val defaultEnd = end ?: dateTimeUtils.getLocalDateNow().plusDays(14)

        val startToDisplay = dateTimeUtils.formatToMediumFormat(defaultStart)
        val endToDisplay = dateTimeUtils.formatToMediumFormat(defaultEnd)

        _sprintDialogState.update {
            it.copy(
                sprintNameValue = sprintName,
                startDate = defaultStart,
                endDate = defaultEnd,
                startDateToDisplay = startToDisplay,
                endDateToDisplay = endToDisplay
            )
        }
    }

    override suspend fun editSprint(
        sprintId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (suspend () -> Unit)?,
        doOnError: (() -> Unit)?
    ) {
        _sprintDialogState.update {
            it.copy(
                sprintNameError = NativeText.Empty,
                dialogError = NativeText.Empty
            )
        }

        val normalizedName = _sprintDialogState.value.sprintNameValue.trim()
        if (normalizedName.isEmpty()) {
            _sprintDialogState.update {
                it.copy(sprintNameError = NativeText.Resource(RString.sprint_name_empty))
            }
            return
        }

        val startDate = _sprintDialogState.value.startDate
        if (startDate == null) {
            _sprintDialogState.update {
                it.copy(dialogError = NativeText.Resource(RString.sprint_start_date_empty))
            }
            return
        }

        val endDate = _sprintDialogState.value.endDate
        if (endDate == null) {
            _sprintDialogState.update {
                it.copy(dialogError = NativeText.Resource(RString.sprint_end_date_empty))
            }
            return
        }

        doOnPreExecute?.invoke()

        resultOf {
            sprintsRepository.editSprint(
                sprintId = sprintId,
                name = normalizedName,
                start = startDate,
                end = endDate
            )
        }.onSuccess {
            setSprintDialogVisibility(false)
            doOnSuccess?.invoke()
        }.onFailure { error ->
            Timber.e(error)
            doOnError?.invoke()
            _sprintDialogState.update {
                it.copy(
                    dialogError = getErrorMessage(error)
                )
            }
        }
    }

    override suspend fun createSprint(
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (suspend () -> Unit)?,
        doOnError: (() -> Unit)?
    ) {
        _sprintDialogState.update {
            it.copy(
                sprintNameError = NativeText.Empty,
                dialogError = NativeText.Empty
            )
        }

        val normalizedName = _sprintDialogState.value.sprintNameValue.trim()
        if (normalizedName.isEmpty()) {
            _sprintDialogState.update {
                it.copy(sprintNameError = NativeText.Resource(RString.sprint_name_empty))
            }
            return
        }

        val startDate = _sprintDialogState.value.startDate
        if (startDate == null) {
            _sprintDialogState.update {
                it.copy(dialogError = NativeText.Resource(RString.sprint_start_date_empty))
            }
            return
        }

        val endDate = _sprintDialogState.value.endDate
        if (endDate == null) {
            _sprintDialogState.update {
                it.copy(dialogError = NativeText.Resource(RString.sprint_end_date_empty))
            }
            return
        }

        doOnPreExecute?.invoke()

        resultOf {
            sprintsRepository.createSprint(
                name = normalizedName,
                start = startDate,
                end = endDate
            )
        }.onSuccess {
            setSprintDialogVisibility(false)
            doOnSuccess?.invoke()
        }.onFailure { error ->
            Timber.e(error)
            doOnError?.invoke()
            _sprintDialogState.update {
                it.copy(
                    dialogError = getErrorMessage(error)
                )
            }
        }
    }

    override fun setSprintDialogVisibility(isVisible: Boolean) {
        _sprintDialogState.update {
            it.copy(isSprintDialogVisible = isVisible)
        }
    }

    private fun onSetSprintNameValue(value: String) {
        _sprintDialogState.update {
            it.copy(
                sprintNameValue = value,
                sprintNameError = NativeText.Empty,
                dialogError = NativeText.Empty
            )
        }
    }

    private fun onDismiss() {
        setSprintDialogVisibility(false)
        _sprintDialogState.update {
            it.copy(
                sprintNameValue = "",
                sprintNameError = NativeText.Empty,
                dialogError = NativeText.Empty,
                startDate = null,
                endDate = null,
                startDateToDisplay = "",
                endDateToDisplay = ""
            )
        }
    }

    private fun setIsStartDateDialogVisible(isVisible: Boolean) {
        _sprintDialogState.update {
            it.copy(isStartDateDialogVisible = isVisible)
        }
    }

    private fun onStartDateDismissRequest() {
        setIsStartDateDialogVisible(false)
    }

    private fun onStartDateDismissButonClick() {
        setIsStartDateDialogVisible(false)
    }

    private fun onStartDateConfirmButtonClick(millis: Long?) {
        setIsStartDateDialogVisible(false)
        if (millis != null) {
            val localDate = dateTimeUtils.fromMillisToLocalDate(millis)
            val dateToDisplay = dateTimeUtils.formatToMediumFormat(localDate)
            _sprintDialogState.update {
                it.copy(
                    startDate = localDate,
                    startDateToDisplay = dateToDisplay
                )
            }
        }
    }

    private fun setIsEndDateDialogVisible(isVisible: Boolean) {
        _sprintDialogState.update {
            it.copy(isEndDateDialogVisible = isVisible)
        }
    }

    private fun onEndDateDismissRequest() {
        setIsEndDateDialogVisible(false)
    }

    private fun onEndDateDismissButonClick() {
        setIsEndDateDialogVisible(false)
    }

    private fun onEndDateConfirmButtonClick(millis: Long?) {
        setIsEndDateDialogVisible(false)
        if (millis != null) {
            val localDate = dateTimeUtils.fromMillisToLocalDate(millis)
            val dateToDisplay = dateTimeUtils.formatToMediumFormat(localDate)
            _sprintDialogState.update {
                it.copy(
                    endDate = localDate,
                    endDateToDisplay = dateToDisplay
                )
            }
        }
    }
}
