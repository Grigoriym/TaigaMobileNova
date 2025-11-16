package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemTagsDelegateImpl(
    private val workItemEditShared: WorkItemEditShared
) : WorkItemTagsDelegate {

    private val _tagsState = MutableStateFlow(
        WorkItemTagsState(
            onGoingToEditTags = ::onGoingToEditTags,
        )
    )
    override val tagsState: StateFlow<WorkItemTagsState> = _tagsState.asStateFlow()

    override fun setInitialTags(tags: PersistentList<TagUI>) {
        _tagsState.update {
            it.copy(tags = tags)
        }
    }

    private fun onGoingToEditTags() {
        workItemEditShared.setTags(_tagsState.value.tags)
    }

    override fun handleTagRemove(tag: TagUI, onRemoveTagFromBackend: () -> Unit) {
        _tagsState.update {
            it.copy(areTagsLoading = true)
        }
        onRemoveTagFromBackend()
    }

    override fun handleNewTagsUpdate(
        newTags: PersistentList<TagUI>,
        onUpdateTagsToBackend: () -> Unit
    ) {
        _tagsState.update {
            it.copy(areTagsLoading = true)
        }
        onUpdateTagsToBackend()
    }

    override fun onTagsUpdateSuccess(newTags: PersistentList<TagUI>) {
        _tagsState.update {
            it.copy(
                tags = newTags,
                areTagsLoading = false
            )
        }
    }

    override fun onTagsUpdateError() {
        _tagsState.update {
            it.copy(areTagsLoading = false)
        }
    }
}