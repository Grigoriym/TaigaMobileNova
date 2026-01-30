package com.grappim.taigamobile.feature.epics.mapper

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class EpicMapper @Inject constructor(
    private val serverStorage: ServerStorage,
    private val statusesMapper: StatusesMapper,
    private val userMapper: UserMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper
) {
    fun toDomain(resp: WorkItemResponseDTO): Epic {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.Epic)
        }/${resp.ref}"

        return Epic(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            title = resp.subject,
            ref = resp.ref,
            status = statusesMapper.getStatus(resp = resp),
            assignee = resp.assignedToExtraInfo?.let { assigned ->
                userMapper.toUser(assigned)
            },
            project = projectMapper.toProjectExtraInfo(resp.projectDTOExtraInfo),
            isClosed = resp.isClosed,
            epicColor = resp.color,
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked },
            milestone = resp.milestone,
            creatorId = creatorId,
            assignedUserIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherUserIds = resp.watchers.orEmpty(),
            description = resp.description ?: "",
            tags = tagsMapper.toTags(resp.tags),
            copyLinkUrl = url
        )
    }

    fun toDomainList(dtos: List<WorkItemResponseDTO>): ImmutableList<Epic> = dtos.map { toDomain(it) }.toImmutableList()
}
