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

    private val _sprintState = Channel<Long?>()
    val sprintState = _sprintState.receiveAsFlow()

    private val _epicsState = Channel<PersistentList<Long>>()
    val epicsState = _epicsState.receiveAsFlow()

    private var _currentType: TeamMemberEditType? = null
    val currentType: TeamMemberEditType
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

    private var _currentSprint: Long? = null
    val currentSprint: Long?
        get() = _currentSprint

    private var _currentEpics: PersistentList<Long> = persistentListOf()
    val currentEpics: ImmutableList<Long>
        get() = _currentEpics

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
        _currentType = TeamMemberEditType.Watchers
        _currentWatchers = ids ?: persistentListOf()
    }

    fun setCurrentAssignee(id: Long?) {
        _currentType = TeamMemberEditType.Assignee
        _currentAssignee = id
    }

    fun setCurrentAssignees(ids: ImmutableList<Long>) {
        _currentType = TeamMemberEditType.Assignees
        _currentAssignees = ids.toPersistentList()
    }

    fun setCurrentSprint(id: Long?) {
        _currentSprint = id
    }

    fun updateSprint(id: Long?) {
        scope.launch {
            _sprintState.send(id)
        }
        clear()
    }

    fun setCurrentEpics(ids: ImmutableList<Long>?) {
        _currentEpics = ids.orEmpty().toPersistentList()
    }

    fun updateEpics(ids: PersistentList<Long>) {
        scope.launch {
            _epicsState.send(ids)
        }
        clear()
    }

    fun clear() {
        _originalTags = _originalTags.clear()
        _currentTags = _currentTags.clear()
        _currentAssignee = null
        _currentWatchers = _currentWatchers.clear()
        _currentAssignees = _currentAssignees.clear()
        _currentSprint = null
        _currentEpics = _currentEpics.clear()
        _currentType = null
    }
}

sealed interface TeamMemberEditType {
    data object Assignee : TeamMemberEditType
    data object Assignees : TeamMemberEditType
    data object Watchers : TeamMemberEditType
}

sealed interface TeamMemberUpdate {
    data class Assignee(val id: Long?) : TeamMemberUpdate
    data class Assignees(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data class Watchers(val ids: ImmutableList<Long>) : TeamMemberUpdate
    data object Clear : TeamMemberUpdate
}
