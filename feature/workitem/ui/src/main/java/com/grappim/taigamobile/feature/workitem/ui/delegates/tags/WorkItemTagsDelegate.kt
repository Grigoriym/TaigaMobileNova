package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemTagsDelegate {
    val tagsState: StateFlow<WorkItemTagsState>

    fun setInitialTags(tags: PersistentList<SelectableTagUI>)

    suspend fun handleTagRemove(
        tag: SelectableTagUI,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleTagsUpdate(
        newTags: PersistentList<SelectableTagUI>,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )
}

data class WorkItemTagsState(
    val tags: PersistentList<SelectableTagUI> = persistentListOf(),
    val areTagsLoading: Boolean = false
)
