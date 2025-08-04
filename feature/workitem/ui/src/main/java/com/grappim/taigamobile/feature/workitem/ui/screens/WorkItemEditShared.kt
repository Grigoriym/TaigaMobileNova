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

    private var _currentType: EditType? = null
    val currentType: EditType?
        get() = _currentType

    private var _originalTags: PersistentList<TagUI> = persistentListOf()
    val originalTags: ImmutableList<TagUI>
        get() = _originalTags

    val originalTagsNames: ImmutableList<String>
        get() = _originalTags.map { it.name }.toImmutableList()
    private var _currentTags: PersistentList<TagUI> = persistentListOf()
    val currentTags: ImmutableList<TagUI>
        get() = _currentTags

    private var _currentAssignee: Long? = null
    val currentAssignee: Long?
        get() = _currentAssignee

    private var _currentWatchers: PersistentList<Long> = persistentListOf()
    val currentWatchers: ImmutableList<Long>
        get() = _currentWatchers

    fun setTags(tags: ImmutableList<TagUI>) {
        _originalTags = tags.toPersistentList()
        _currentTags = tags.toPersistentList()
    }

    fun setCurrentWatchers(ids: PersistentList<Long>?) {
        _currentType = EditType.Watchers
        _currentWatchers = ids ?: persistentListOf()
    }

    fun setCurrentAssignee(id: Long?) {
        _currentType = EditType.Assignee
        _currentAssignee = id
    }

    fun clear() {
        _originalTags = _originalTags.clear()
        _currentTags = _currentTags.clear()
        _currentAssignee = null
        _currentWatchers = _currentWatchers.clear()
        _currentType = null
    }
}

sealed interface EditType {
    data object Assignee : EditType
    data object Watchers : EditType
}
