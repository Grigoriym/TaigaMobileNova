package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemTagsDelegate {
    val tagsState: StateFlow<WorkItemTagsState>

    fun setInitialTags(tags: PersistentList<TagUI>)

    suspend fun handleTagRemove(
        tag: TagUI,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleTagsUpdate(
        newTags: PersistentList<TagUI>,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

//    fun onGoingToEditTags()
}

data class WorkItemTagsState(
    val tags: PersistentList<TagUI> = persistentListOf(),
    val areTagsLoading: Boolean = false
//    val onGoingToEditTags: () -> Unit = {}
)
