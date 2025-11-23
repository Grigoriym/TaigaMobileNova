package com.grappim.taigamobile.feature.workitem.ui.delegates.customfields

import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemCustomFieldsDelegate {
    val customFieldsState: StateFlow<WorkItemCustomFieldsState>

    fun setInitialCustomFields(customFieldStateItems: ImmutableList<CustomFieldItemState>, version: Long)

    suspend fun handleCustomFieldSave(
        item: CustomFieldItemState,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setIsCustomFieldsWidgetExpanded(isExpanded: Boolean)
}

data class WorkItemCustomFieldsState(
    val customFieldStateItems: ImmutableList<CustomFieldItemState> = persistentListOf(),
    val customFieldsVersion: Long = 0L,
    val editingItemIds: ImmutableSet<Long> = persistentSetOf(),
    val isCustomFieldsWidgetExpanded: Boolean = false,
    val isCustomFieldsLoading: Boolean = false,
    val setIsCustomFieldsWidgetExpanded: (Boolean) -> Unit = {},
    val onCustomFieldChange: (CustomFieldItemState) -> Unit = {},
    val onCustomFieldEditToggle: (CustomFieldItemState) -> Unit = {}
)
