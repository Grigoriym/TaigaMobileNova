package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemTagsDelegate {
    val tagsState: StateFlow<WorkItemTagsState>

    fun setInitialTags(tags: PersistentList<TagUI>)
    fun handleTagRemove(tag: TagUI, onRemoveTagFromBackend: () -> Unit)
    fun handleNewTagsUpdate(newTags: PersistentList<TagUI>, onUpdateTagsToBackend: () -> Unit)
    fun onTagsUpdateSuccess(newTags: PersistentList<TagUI>)
    fun onTagsUpdateError()
}

data class WorkItemTagsState(
    val tags: PersistentList<TagUI> = persistentListOf(),
    val areTagsLoading: Boolean = false,
    val onGoingToEditTags: () -> Unit = {},
)
