package com.grappim.taigamobile.feature.workitem.ui.screens

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A temporary solution for sharing data between screens, since savedStateHandle has limitations
 */
@Singleton
class WorkItemEditShared @Inject constructor() {

    private var _originalTags: PersistentList<TagUI> = persistentListOf()
    val originalTags: ImmutableList<TagUI>
        get() = _originalTags

    val originalTagsNames: ImmutableList<String>
        get() = _originalTags.map { it.name }.toImmutableList()
    private var _currentTags: PersistentList<TagUI> = persistentListOf()
    val currentTags: ImmutableList<TagUI>
        get() = _currentTags

    fun setTags(tags: ImmutableList<TagUI>) {
        _originalTags = tags.toPersistentList()
        _currentTags = tags.toPersistentList()
    }

    fun clear() {
        _originalTags = _originalTags.clear()
        _currentTags = _currentTags.clear()
    }
}
