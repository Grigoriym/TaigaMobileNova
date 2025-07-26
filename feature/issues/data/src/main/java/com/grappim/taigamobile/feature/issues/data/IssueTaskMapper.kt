package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.api.UserMapper
import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Priority
import com.grappim.taigamobile.feature.filters.domain.model.Severity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.Type
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IssueTaskMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper
) {
    suspend fun toDomain(
        resp: CommonTaskResponse,
        server: String,
        filters: FiltersData
    ): IssueTask = withContext(ioDispatcher) {
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.Issue)
        }/${resp.ref}"

        val creatorId = resp.owner ?: error("Owner field is null")

        IssueTask(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            dueDate = resp.dueDate,
            creatorId = creatorId,
            dueDateStatus = when (resp.dueDateStatusDTO) {
                DueDateStatusDTO.Set -> DueDateStatus.Set
                DueDateStatusDTO.DueSoon -> DueDateStatus.DueSoon
                DueDateStatusDTO.PastDue -> DueDateStatus.PastDue
                DueDateStatusDTO.NoLongerApplicable -> DueDateStatus.NoLongerApplicable
                DueDateStatusDTO.NotSet -> DueDateStatus.NotSet
                else -> null
            },
            title = resp.subject,
            description = resp.description ?: "",
            ref = resp.ref,
            project = toProject(resp.projectDTOExtraInfo),
            colors = resp.color?.let { listOf(it) } ?: resp.epics.orEmpty().map { it.color },
            isClosed = resp.isClosed,
            tags = toTags(resp.tags).toImmutableList(),
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked },
            assignee = resp.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            assignedUserIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherUserIds = resp.watchers.orEmpty(),
            milestone = resp.milestone,
            copyLinkUrl = url,
            status = getStatus(filtersData = filters, resp = resp),
            type = getType(filtersData = filters, resp = resp),
            severity = getSeverity(filtersData = filters, resp = resp),
            priority = getPriority(filtersData = filters, resp = resp)
        )
    }

    private fun getStatus(filtersData: FiltersData, resp: CommonTaskResponse): Status? {
        val currentStatus = filtersData.statuses.find { resp.status == it.id } ?: return null
        return Status(
            id = resp.status,
            name = resp.statusExtraInfo.name,
            color = resp.statusExtraInfo.color,
            count = currentStatus.count,
            order = currentStatus.order
        )
    }

    private fun getType(filtersData: FiltersData, resp: CommonTaskResponse): Type? {
        val typeId = resp.type ?: return null
        val currentItem = filtersData.types.find { typeId == it.id } ?: return null
        return Type(
            id = typeId,
            name = currentItem.name,
            color = currentItem.color,
            count = currentItem.count,
            order = currentItem.order
        )
    }

    private fun getSeverity(filtersData: FiltersData, resp: CommonTaskResponse): Severity? {
        val severityId = resp.severity ?: return null
        val currentItem = filtersData.severities.find { severityId == it.id } ?: return null
        return Severity(
            id = severityId,
            name = currentItem.name,
            color = currentItem.color,
            count = currentItem.count,
            order = currentItem.order
        )
    }

    private fun getPriority(filtersData: FiltersData, resp: CommonTaskResponse): Priority? {
        val priorityId = resp.priority ?: return null
        val currentItem = filtersData.priorities.find { priorityId == it.id } ?: return null
        return Priority(
            id = priorityId,
            name = currentItem.name,
            color = currentItem.color,
            count = currentItem.count,
            order = currentItem.order
        )
    }

    private fun toProject(dto: ProjectDTO): Project = Project(
        id = dto.id,
        name = dto.name,
        slug = dto.slug,
        isMember = dto.isMember,
        isAdmin = dto.isAdmin,
        isOwner = dto.isOwner,
        description = dto.description,
        avatarUrl = dto.avatarUrl,
        members = dto.members,
        fansCount = dto.fansCount,
        watchersCount = dto.watchersCount,
        isPrivate = dto.isPrivate
    )

    private fun toTags(tags: List<List<String?>>?): List<Tag> = tags.orEmpty()
        .map { tag ->
            Tag(
                name = tag.getOrNull(0).orEmpty(),
                color = tag.getOrNull(1).fixNullColor(),
                count = tag.getOrNull(2)?.toLong() ?: 0
            )
        }
}
