package com.grappim.taigamobile.feature.workitem.ui.screens

import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel

class WorkItemEditSession(val workItemId: Long, val taskIdentifier: TaskIdentifier) {
    val tagsChannel: Channel<PersistentList<SelectableTagUI>> = Channel()
    val descriptionChannel: Channel<String> = Channel()
    val teamMemberUpdateChannel: Channel<TeamMemberUpdate> = Channel()
    val sprintChannel: Channel<Long?> = Channel()
    val epicsChannel: Channel<PersistentList<Long>> = Channel()

    private var _originalTags: PersistentList<SelectableTagUI> = persistentListOf()
    val originalTags: ImmutableList<SelectableTagUI>
        get() = _originalTags

    val originalTagsNames: ImmutableList<String>
        get() = _originalTags.map { it.name }.toImmutableList()

    private var _currentTags: PersistentList<SelectableTagUI> = persistentListOf()
    val currentTags: ImmutableList<SelectableTagUI>
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

    private var _currentType: TeamMemberEditType? = null
    val currentType: TeamMemberEditType
        get() = requireNotNull(_currentType)

    internal fun setTags(tags: PersistentList<SelectableTagUI>) {
        _originalTags = tags
        _currentTags = tags
    }

    internal fun setCurrentWatchers(ids: PersistentList<Long>) {
        _currentType = TeamMemberEditType.Watchers
        _currentWatchers = ids
    }

    internal fun setCurrentAssignee(id: Long?) {
        _currentType = TeamMemberEditType.Assignee
        _currentAssignee = id
    }

    internal fun setCurrentAssignees(ids: PersistentList<Long>) {
        _currentType = TeamMemberEditType.Assignees
        _currentAssignees = ids
    }

    internal fun setCurrentSprint(id: Long?) {
        _currentSprint = id
    }

    internal fun setCurrentEpics(ids: PersistentList<Long>) {
        _currentEpics = ids
    }

    internal fun clear() {
        _originalTags = persistentListOf()
        _currentTags = persistentListOf()
        _currentAssignee = null
        _currentAssignees = persistentListOf()
        _currentWatchers = persistentListOf()
        _currentSprint = null
        _currentEpics = persistentListOf()
        _currentType = null
    }

    internal fun close() {
        tagsChannel.close()
        descriptionChannel.close()
        teamMemberUpdateChannel.close()
        sprintChannel.close()
        epicsChannel.close()
    }
}
