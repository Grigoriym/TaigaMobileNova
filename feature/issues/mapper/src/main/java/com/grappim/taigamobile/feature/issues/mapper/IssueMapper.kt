package com.grappim.taigamobile.feature.issues.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.PromotedUserStoryInfo
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IssueMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper,
    private val statusesMapper: StatusesMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper,
    private val dueDateStatusMapper: DueDateStatusMapper,
    private val serverStorage: ServerStorage
) {

    suspend fun toDomainList(dtos: List<WorkItemResponseDTO>): ImmutableList<Issue> =
        dtos.map { toDomain(it) }.toImmutableList()

    suspend fun toDomain(resp: WorkItemResponseDTO): Issue = withContext(ioDispatcher) {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.Issue)
        }/${resp.ref}"

        Issue(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            title = resp.subject,
            dueDate = resp.dueDate,
            creatorId = creatorId,
            dueDateStatus = dueDateStatusMapper.toDomain(resp.dueDateStatusDTO),
            description = resp.description ?: "",
            ref = resp.ref,
            project = projectMapper.toProject(resp.projectDTOExtraInfo),
            isClosed = resp.isClosed,
            tags = tagsMapper.toTags(resp.tags),
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked },
            assignee = resp.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            assignedUserIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherUserIds = resp.watchers.orEmpty(),
            milestone = resp.milestone,
            copyLinkUrl = url,
            status = statusesMapper.getStatus(resp = resp)
        )
    }

    suspend fun toDomain(resp: WorkItemResponseDTO, filters: FiltersData): Issue = withContext(ioDispatcher) {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.Issue)
        }/${resp.ref}"

        Issue(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            title = resp.subject,
            dueDate = resp.dueDate,
            creatorId = creatorId,
            dueDateStatus = dueDateStatusMapper.toDomain(resp.dueDateStatusDTO),
            description = resp.description ?: "",
            ref = resp.ref,
            project = projectMapper.toProject(resp.projectDTOExtraInfo),
            isClosed = resp.isClosed,
            tags = tagsMapper.toTags(resp.tags),
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked },
            assignee = resp.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            assignedUserIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherUserIds = resp.watchers.orEmpty(),
            milestone = resp.milestone,
            copyLinkUrl = url,
            status = statusesMapper.getStatus(resp = resp),
            type = statusesMapper.getType(filtersData = filters, resp = resp),
            severity = statusesMapper.getSeverity(filtersData = filters, resp = resp),
            priority = statusesMapper.getPriority(filtersData = filters, resp = resp),
            promotedUserStories = resp.generatedUserStories?.map { story ->
                PromotedUserStoryInfo(
                    id = story.id,
                    ref = requireNotNull(story.ref),
                    subject = story.subject,
                    titleToDisplay = "#${story.ref} ${story.subject}"
                )
            }.orEmpty().toImmutableList()
        )
    }
}
