package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.data.StatusMapper
import com.grappim.taigamobile.feature.filters.data.TagsMapper
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.projects.data.ProjectMapper
import com.grappim.taigamobile.feature.users.data.mappers.UserMapper
import com.grappim.taigamobile.feature.workitem.data.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IssueMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper,
    private val statusMapper: StatusMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper,
    private val dueDateStatusMapper: DueDateStatusMapper,
    private val serverStorage: ServerStorage
) {
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
            status = statusMapper.getStatus(resp = resp),
            type = getType(filtersData = filters, resp = resp),
            severity = getSeverity(filtersData = filters, resp = resp),
            priority = getPriority(filtersData = filters, resp = resp)
        )
    }

    private fun getType(filtersData: FiltersData, resp: WorkItemResponseDTO): Type? {
        val typeId = resp.type ?: return null
        val currentItem = filtersData.types.find { typeId == it.id } ?: return null
        return Type(
            id = typeId,
            name = currentItem.name,
            color = currentItem.color
        )
    }

    private fun getSeverity(filtersData: FiltersData, resp: WorkItemResponseDTO): Severity? {
        val severityId = resp.severity ?: return null
        val currentItem = filtersData.severities.find { severityId == it.id } ?: return null
        return Severity(
            id = severityId,
            name = currentItem.name,
            color = currentItem.color
        )
    }

    private fun getPriority(filtersData: FiltersData, resp: WorkItemResponseDTO): Priority? {
        val priorityId = resp.priority ?: return null
        val currentItem = filtersData.priorities.find { priorityId == it.id } ?: return null
        return Priority(
            id = priorityId,
            name = currentItem.name,
            color = currentItem.color
        )
    }
}
