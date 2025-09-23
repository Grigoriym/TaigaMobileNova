package com.grappim.taigamobile.feature.workitem.ui.screens

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A temporary solution for sharing data between screens, since savedStateHandle has limitations
 */
@Singleton
class WorkItemEditShared @Inject constructor() {

    private val scope = MainScope()

    private val _teamMemberUpdateState = Channel<TeamMemberUpdate>()
    val teamMemberUpdateState = _teamMemberUpdateState.receiveAsFlow()

    private val _tagsState = Channel<PersistentList<TagUI>>()
    val tagsState = _tagsState.receiveAsFlow()

    private val _descriptionState = Channel<String>()
    val descriptionState = _descriptionState.receiveAsFlow()

    private var _currentType: EditType? = null
    val currentType: EditType
        get() = requireNotNull(_currentType)

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

    private var _currentAssignees: PersistentList<Long> = persistentListOf()
    val currentAssignees: ImmutableList<Long>
        get() = _currentAssignees

    private var _currentWatchers: PersistentList<Long> = persistentListOf()
    val currentWatchers: ImmutableList<Long>
        get() = _currentWatchers

    fun setTags(tags: ImmutableList<TagUI>) {
        _originalTags = tags.toPersistentList()
        _currentTags = tags.toPersistentList()
    }

    fun updateDescription(description: String) {
        scope.launch {
            _descriptionState.send(description)
        }
        clear()
    }

    fun updateTags(tags: ImmutableList<TagUI>) {
        scope.launch {
            _tagsState.send(tags.toPersistentList())
        }
        clear()
    }

    fun updateWatchers(ids: PersistentList<Long>) {
        scope.launch {
            _teamMemberUpdateState.send(TeamMemberUpdate.Watchers(ids))
        }
        clear()
    }

    fun updateAssignee(id: Long?) {
        scope.launch {
            _teamMemberUpdateState.send(TeamMemberUpdate.Assignee(id))
        }
        clear()
    }

    fun updateAssignees(ids: PersistentList<Long>) {
        scope.launch {
            _teamMemberUpdateState.send(TeamMemberUpdate.Assignees(ids))
        }
        clear()
    }

    fun setCurrentWatchers(ids: PersistentList<Long>?) {
        _currentType = EditType.Watchers
        _currentWatchers = ids ?: persistentListOf()
    }

    fun setCurrentAssignee(id: Long?) {
        _currentType = EditType.Assignee
        _currentAssignee = id
    }

    fun setCurrentAssignees(ids: ImmutableList<Long>) {
        _currentType = EditType.Assignees
        _currentAssignees = ids.toPersistentList()
    }

    fun clear() {
        _originalTags = _originalTags.clear()
        _currentTags = _currentTags.clear()
        _currentAssignee = null
        _currentWatchers = _currentWatchers.clear()
        _currentAssignees = _currentAssignees.clear()
        _currentType = null
    }
}

sealed interface EditType {
    data object Assignee : EditType
    data object Assignees : EditType
    data object Watchers : EditType
}

sealed interface TeamMemberUpdate {
    data class Assignee(val id: Long?) : TeamMemberUpdate
    data class Assignees(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data class Watchers(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data object Clear : TeamMemberUpdate
}
