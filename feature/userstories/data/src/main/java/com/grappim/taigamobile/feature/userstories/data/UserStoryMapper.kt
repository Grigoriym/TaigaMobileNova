package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.EpicShortInfoDTO
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.data.StatusMapper
import com.grappim.taigamobile.feature.filters.data.TagsMapper
import com.grappim.taigamobile.feature.projects.data.ProjectMapper
import com.grappim.taigamobile.feature.users.data.mappers.UserMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.data.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserStoryMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper,
    private val statusMapper: StatusMapper,
    private val projectMapper: ProjectMapper,
    private val tagsMapper: TagsMapper,
    private val dueDateStatusMapper: DueDateStatusMapper,
    private val serverStorage: ServerStorage
) {

    suspend fun toListDomain(list: List<WorkItemResponseDTO>): ImmutableList<UserStory> =
        list.map { toDomain(it) }.toImmutableList()

    suspend fun toDomain(resp: WorkItemResponseDTO): UserStory = withContext(ioDispatcher) {
        val creatorId = resp.owner ?: error("Owner field is null")

        val server = serverStorage.server
        val url = "$server/project/${resp.projectDTOExtraInfo.slug}/${
            transformTaskTypeForCopyLink(CommonTaskType.UserStory)
        }/${resp.ref}"

        UserStory(
            id = resp.id,
            version = resp.version,
            createdDateTime = resp.createdDate,
            title = resp.subject,
            ref = resp.ref,
            status = statusMapper.getStatus(resp = resp),
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
            userStoryEpics = epicsToDomain(resp.epics),
            swimlane = resp.swimlane
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
