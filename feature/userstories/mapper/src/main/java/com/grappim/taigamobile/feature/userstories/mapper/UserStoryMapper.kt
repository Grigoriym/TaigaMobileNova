package com.grappim.taigamobile.feature.userstories.mapper

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.epics.dto.EpicShortInfoDTO
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class UserStoryMapper @Inject constructor(
    private val userMapper: UserMapper,
    private val statusesMapper: StatusesMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper,
    private val dueDateStatusMapper: DueDateStatusMapper,
    private val serverStorage: ServerStorage
) {

    fun toListDomain(list: List<WorkItemResponseDTO>): ImmutableList<UserStory> =
        list.map { toDomain(it) }.toImmutableList()

    fun toDomain(resp: WorkItemResponseDTO): UserStory {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.UserStory)
        }/${resp.ref}"

        return UserStory(
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
            userStoryEpics = epicsToDomain(resp.epics),
            swimlane = resp.swimlane,
            wasPromotedFromTask = resp.fromTaskRef?.isNotEmpty() == true,
            kanbanOrder = resp.kanbanOrder
        )
    }

    private fun epicsToDomain(epics: List<EpicShortInfoDTO>?): ImmutableList<UserStoryEpic> = epics?.map {
        UserStoryEpic(
            id = it.id,
            title = it.title,
            ref = it.ref,
            color = it.color
        )
    }.orEmpty().toImmutableList()
}
