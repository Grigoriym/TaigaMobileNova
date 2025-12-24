package com.grappim.taigamobile.feature.workitem.ui.screens

import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Though it is better than my initial attempt for such behavior
 * I hope I will find a way to do this the way I want it
 * What I want is shown in docs/workitem_edit_scoping.puml file
 */
@Singleton
class WorkItemEditStateRepository @Inject constructor() {

    private val sessions = ConcurrentHashMap<String, WorkItemEditSession>()

    private fun getSessionKey(workItemId: Long, type: TaskIdentifier): String = when (type) {
        is TaskIdentifier.WorkItem -> "${type.commonTaskType.name}_$workItemId"
        TaskIdentifier.Wiki -> "WIKI_$workItemId"
    }

    private fun getOrCreateSession(workItemId: Long, type: TaskIdentifier): WorkItemEditSession {
        val key = getSessionKey(workItemId, type)
        return sessions.getOrPut(key) {
            Timber.d("Creating new session for key: $key")
            WorkItemEditSession(workItemId = workItemId, taskIdentifier = type)
        }
    }

    fun getTagsFlow(workItemId: Long, type: TaskIdentifier): Flow<PersistentList<TagUI>> =
        getOrCreateSession(workItemId, type).tagsChannel.receiveAsFlow()

    fun getDescriptionFlow(workItemId: Long, type: TaskIdentifier): Flow<String> =
        getOrCreateSession(workItemId, type).descriptionChannel.receiveAsFlow()

    fun getTeamMemberUpdateFlow(workItemId: Long, type: TaskIdentifier): Flow<TeamMemberUpdate> =
        getOrCreateSession(workItemId, type).teamMemberUpdateChannel.receiveAsFlow()

    fun getSprintFlow(workItemId: Long, type: TaskIdentifier): Flow<Long?> =
        getOrCreateSession(workItemId, type).sprintChannel.receiveAsFlow()

    fun getEpicsFlow(workItemId: Long, type: TaskIdentifier): Flow<PersistentList<Long>> =
        getOrCreateSession(workItemId, type).epicsChannel.receiveAsFlow()

    suspend fun updateTags(workItemId: Long, type: TaskIdentifier, tags: ImmutableList<TagUI>) {
        val session = getOrCreateSession(workItemId, type)
        session.tagsChannel.send(tags.toPersistentList())
        session.clear()
    }

    suspend fun updateDescription(workItemId: Long, type: TaskIdentifier, description: String) {
        val session = getOrCreateSession(workItemId, type)
        session.descriptionChannel.send(description)
        session.clear()
    }

    suspend fun updateWatchers(workItemId: Long, type: TaskIdentifier, ids: PersistentList<Long>) {
        val session = getOrCreateSession(workItemId, type)
        session.teamMemberUpdateChannel.send(TeamMemberUpdate.Watchers(ids))
        session.clear()
    }

    suspend fun updateAssignee(workItemId: Long, type: TaskIdentifier, id: Long?) {
        val session = getOrCreateSession(workItemId, type)
        session.teamMemberUpdateChannel.send(TeamMemberUpdate.Assignee(id))
        session.clear()
    }

    suspend fun updateAssignees(workItemId: Long, type: TaskIdentifier, ids: PersistentList<Long>) {
        val session = getOrCreateSession(workItemId, type)
        session.teamMemberUpdateChannel.send(TeamMemberUpdate.Assignees(ids))
        session.clear()
    }

    suspend fun updateSprint(workItemId: Long, type: TaskIdentifier, id: Long?) {
        val session = getOrCreateSession(workItemId, type)
        session.sprintChannel.send(id)
        session.clear()
    }

    suspend fun updateEpics(workItemId: Long, type: TaskIdentifier, ids: PersistentList<Long>) {
        val session = getOrCreateSession(workItemId, type)
        session.epicsChannel.send(ids)
        session.clear()
    }

    fun setTags(workItemId: Long, type: TaskIdentifier, tags: ImmutableList<TagUI>) {
        val session = getOrCreateSession(workItemId, type)
        session.setTags(tags.toPersistentList())
    }

    fun setCurrentWatchers(workItemId: Long, type: TaskIdentifier, ids: PersistentList<Long>?) {
        val session = getOrCreateSession(workItemId, type)
        session.setCurrentWatchers(ids ?: kotlinx.collections.immutable.persistentListOf())
    }

    fun setCurrentAssignee(workItemId: Long, type: TaskIdentifier, id: Long?) {
        val session = getOrCreateSession(workItemId, type)
        session.setCurrentAssignee(id)
    }

    fun setCurrentAssignees(workItemId: Long, type: TaskIdentifier, ids: ImmutableList<Long>) {
        val session = getOrCreateSession(workItemId, type)
        session.setCurrentAssignees(ids.toPersistentList())
    }

    fun setCurrentSprint(workItemId: Long, type: TaskIdentifier, id: Long?) {
        val session = getOrCreateSession(workItemId, type)
        session.setCurrentSprint(id)
    }

    fun setCurrentEpics(workItemId: Long, type: TaskIdentifier, ids: ImmutableList<Long>?) {
        val session = getOrCreateSession(workItemId, type)
        session.setCurrentEpics(ids.orEmpty().toPersistentList())
    }

    fun getCurrentType(workItemId: Long, type: TaskIdentifier): TeamMemberEditType =
        getOrCreateSession(workItemId, type).currentType

    fun getOriginalTags(workItemId: Long, type: TaskIdentifier): ImmutableList<TagUI> =
        getOrCreateSession(workItemId, type).originalTags

    fun getOriginalTagsNames(workItemId: Long, type: TaskIdentifier): ImmutableList<String> =
        getOrCreateSession(workItemId, type).originalTagsNames

    fun getCurrentTags(workItemId: Long, type: TaskIdentifier): ImmutableList<TagUI> =
        getOrCreateSession(workItemId, type).currentTags

    fun getCurrentAssignee(workItemId: Long, type: TaskIdentifier): Long? =
        getOrCreateSession(workItemId, type).currentAssignee

    fun getCurrentAssignees(workItemId: Long, type: TaskIdentifier): ImmutableList<Long> =
        getOrCreateSession(workItemId, type).currentAssignees

    fun getCurrentWatchers(workItemId: Long, type: TaskIdentifier): ImmutableList<Long> =
        getOrCreateSession(workItemId, type).currentWatchers

    fun getCurrentSprint(workItemId: Long, type: TaskIdentifier): Long? =
        getOrCreateSession(workItemId, type).currentSprint

    fun getCurrentEpics(workItemId: Long, type: TaskIdentifier): ImmutableList<Long> =
        getOrCreateSession(workItemId, type).currentEpics

    fun clearSession(workItemId: Long, type: TaskIdentifier) {
        val key = getSessionKey(workItemId, type)
        sessions.remove(key)?.let { session ->
            Timber.d("Clearing session for key: $key")
            session.close()
        }
    }
}
