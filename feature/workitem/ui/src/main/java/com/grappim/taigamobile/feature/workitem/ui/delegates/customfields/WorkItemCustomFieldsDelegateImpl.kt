package com.grappim.taigamobile.feature.workitem.ui.delegates.customfields

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.NumberItemState
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class WorkItemCustomFieldsDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val dateTimeUtils: DateTimeUtils
) : WorkItemCustomFieldsDelegate {

    private val _customFieldsState = MutableStateFlow(
        WorkItemCustomFieldsState(
            setIsCustomFieldsWidgetExpanded = ::setIsCustomFieldsWidgetExpanded,
            onCustomFieldChange = ::onCustomFieldChange,
            onCustomFieldEditToggle = ::onCustomFieldEditToggle
        )
    )
    override val customFieldsState: StateFlow<WorkItemCustomFieldsState> =
        _customFieldsState.asStateFlow()

    override fun setInitialCustomFields(
        customFieldStateItems: ImmutableList<CustomFieldItemState>,
        version: Long
    ) {
        _customFieldsState.update {
            it.copy(
                customFieldStateItems = customFieldStateItems,
                customFieldsVersion = version
            )
        }
    }

    private fun onCustomFieldEditToggle(item: CustomFieldItemState) {
        _customFieldsState.update { currentState ->
            val currentIds = currentState.editingItemIds
            val newIds = if (item.id in currentIds) {
                currentIds - item.id
            } else {
                currentIds + item.id
            }.toImmutableSet()
            currentState.copy(editingItemIds = newIds)
        }
    }

    private fun onCustomFieldChange(updatedItem: CustomFieldItemState) {
        _customFieldsState.update { currentState ->
            val updatedList = currentState.customFieldStateItems.map { item ->
                if (item.id == updatedItem.id) {
                    updatedItem
                } else {
                    item
                }
            }.toImmutableList()
            currentState.copy(customFieldStateItems = updatedList)
        }
    }

    override suspend fun handleCustomFieldSave(
        item: CustomFieldItemState,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _customFieldsState.update {
            it.copy(isCustomFieldsLoading = true)
        }

        val currentState = _customFieldsState.value

        val patchedData = currentState.customFieldStateItems.associate { stateItem ->
            stateItem.id.toString() to if (stateItem.id == item.id) {
                getCustomFieldValue(stateItem, true)
            } else if (stateItem.isModified) {
                getCustomFieldValue(stateItem, false)
            } else {
                getCustomFieldValue(stateItem, true)
            }
        }

        resultOf {
            val payload = patchDataGenerator.getAttributesPatchPayload(patchedData)
            workItemRepository.patchCustomAttributes(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { result ->
            doOnSuccess?.invoke()

            onCustomFieldEditToggle(item)
            onCustomFieldSaved(item.getSavedItem())
            _customFieldsState.update {
                it.copy(
                    isCustomFieldsLoading = false,
                    customFieldsVersion = result.version
                )
            }
        }.onFailure { error ->
            _customFieldsState.update {
                it.copy(isCustomFieldsLoading = false)
            }
            doOnError(error)
        }
    }

    override fun setIsCustomFieldsWidgetExpanded(isExpanded: Boolean) {
        _customFieldsState.update {
            it.copy(isCustomFieldsWidgetExpanded = isExpanded)
        }
    }

    private fun onCustomFieldSaved(newItem: CustomFieldItemState) {
        _customFieldsState.update { currentState ->
            val updatedList = currentState.customFieldStateItems.map { item ->
                if (item.id == newItem.id) {
                    newItem
                } else {
                    item
                }
            }.toImmutableList()
            currentState.copy(customFieldStateItems = updatedList)
        }
    }

    private fun getCustomFieldValue(
        stateItem: CustomFieldItemState,
        takeCurrentValue: Boolean
    ): Any? {
        val valueToUse = if (takeCurrentValue) {
            stateItem.currentValue
        } else {
            stateItem.originalValue
        }
        return when (stateItem) {
            is DateItemState -> {
                if (valueToUse != null) {
                    dateTimeUtils.parseLocalDateToString(valueToUse as LocalDate)
                } else {
                    null
                }
            }

            is NumberItemState -> {
                valueToUse?.toString()?.toLongOrNull()
            }

            else -> valueToUse
        }
    }
}
