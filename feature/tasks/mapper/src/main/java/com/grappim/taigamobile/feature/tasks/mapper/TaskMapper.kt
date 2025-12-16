package com.grappim.taigamobile.feature.tasks.mapper

import com.grappim.taigamobile.core.async.DefaultDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.tasks.domain.Task
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.UserStoryShortInfoMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskMapper @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    private val serverStorage: ServerStorage,
    private val userMapper: UserMapper,
    private val statusesMapper: StatusesMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper,
    private val dueDateStatusMapper: DueDateStatusMapper,
    private val userStoryShortInfoMapper: UserStoryShortInfoMapper
) {

    suspend fun toDomainList(dtos: List<WorkItemResponseDTO>): ImmutableList<Task> =
        dtos.map { toDomain(it) }.toImmutableList()

    suspend fun toDomain(resp: WorkItemResponseDTO): Task = withContext(defaultDispatcher) {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.Task)
        }/${resp.ref}"

        Task(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            title = resp.subject,
            ref = resp.ref,
            status = statusesMapper.getStatus(resp = resp),
            assignee = resp.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            project = projectMapper.toProject(resp.projectDTOExtraInfo),
            isClosed = resp.isClosed,
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked },
            milestone = resp.milestone,
            creatorId = creatorId,
            assignedUserIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherUserIds = resp.watchers.orEmpty(),
            description = resp.description ?: "",
            tags = tagsMapper.toTags(resp.tags),
            dueDate = resp.dueDate,
            dueDateStatus = dueDateStatusMapper.toDomain(resp.dueDateStatusDTO),
            copyLinkUrl = url,
            userStory = resp.userStoryExtraInfo?.let { userStoryShortInfoMapper.toDomain(it) }
        )
    }
}
