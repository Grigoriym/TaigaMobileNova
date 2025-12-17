package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.utils.getDueDateText
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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

    override fun setInitialDueDate(dueDateText: NativeText) {
        _dueDateState.update {
            it.copy(dueDateText = dueDateText)
        }
    }

    override suspend fun handleDueDateSave(
        newDate: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((DueDateSaveResult) -> Unit)?,
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
            doOnSuccess?.invoke(
                DueDateSaveResult(
                    dueDate = localDate,
                    patchedData = patchedData
                )
            )

            _dueDateState.update {
                it.copy(
                    dueDateText = dateTimeUtils.getDueDateText(localDate),
                    isDueDateLoading = false
                )
            }
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
}
