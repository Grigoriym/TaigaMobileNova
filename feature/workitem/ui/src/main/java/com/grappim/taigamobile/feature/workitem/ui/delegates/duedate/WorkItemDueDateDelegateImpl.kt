package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import androidx.compose.material3.MaterialTheme
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.DynamicColor
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class WorkItemDueDateDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val dateTimeUtils: DateTimeUtils
) : WorkItemDueDateDelegate {

    private val _dueDateState = MutableStateFlow(
        WorkItemDueDateState(
            setDueDateDatePickerVisibility = ::setDueDateDatePickerVisibility
        )
    )
    override val dueDateState: StateFlow<WorkItemDueDateState> = _dueDateState.asStateFlow()

    override fun setInitialDueDate(dueDate: LocalDate?, dueDateStatus: DueDateStatus?) {
        _dueDateState.update {
            it.copy(
                dueDateText = getDueDateText(dueDate),
                dueDate = dueDate,
                dueDateStatus = dueDateStatus
            )
        }
        updateDueDateColor(dueDateStatus, dueDate)
    }

    private fun getDueDateText(dueDate: LocalDate?): NativeText = if (dueDate == null) {
        NativeText.Resource(id = RString.no_due_date)
    } else {
        NativeText.Simple(dateTimeUtils.formatToMediumFormat(dueDate))
    }

    override suspend fun handleDueDateSave(
        newDate: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((PatchedData) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _dueDateState.update {
            it.copy(isDueDateLoading = true)
        }

        val localDate = if (newDate != null) {
            dateTimeUtils.fromMillisToLocalDate(newDate)
        } else {
            null
        }
        val jsonLocalDate = if (localDate != null) {
            dateTimeUtils.parseLocalDateToString(localDate)
        } else {
            null
        }

        resultOf {
            val payload = patchDataGenerator.getDueDatePatchPayload(jsonLocalDate)
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData)

            _dueDateState.update {
                it.copy(
                    isDueDateLoading = false
                )
            }

            setInitialDueDate(
                dueDate = localDate,
                dueDateStatus = patchedData.dueDateStatus
            )
        }.onFailure { error ->
            _dueDateState.update {
                it.copy(isDueDateLoading = false)
            }
            doOnError(error)
        }
    }

    override fun setDueDateDatePickerVisibility(isVisible: Boolean) {
        _dueDateState.update {
            it.copy(isDueDateDatePickerVisible = isVisible)
        }
    }

    private fun updateDueDateColor(dueDateStatus: DueDateStatus?, dueDate: LocalDate?) {
        val backgroundColor = when (dueDateStatus) {
            DueDateStatus.NotSet, DueDateStatus.NoLongerApplicable, null ->
                if (dueDate != null) {
                    DynamicColor {
                        MaterialTheme.colorScheme.surface
                    }
                } else {
                    DynamicColor {
                        MaterialTheme.colorScheme.primary
                    }
                }

            DueDateStatus.Set -> StaticColor(
                taigaGreenPositive
            )

            DueDateStatus.DueSoon -> StaticColor(
                taigaOrange
            )

            DueDateStatus.PastDue -> StaticColor(
                taigaRed
            )
        }

        _dueDateState.update {
            it.copy(backgroundColor = backgroundColor)
        }
    }
}
