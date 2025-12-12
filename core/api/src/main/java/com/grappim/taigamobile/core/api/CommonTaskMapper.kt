package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.TagOld
import com.grappim.taigamobile.core.domain.nullOwnerError
import com.grappim.taigamobile.core.domain.toStatus
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommonTaskMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    suspend fun toDomain(resp: CommonTaskResponse, commonTaskType: CommonTaskType): CommonTask =
        withContext(ioDispatcher) {
            CommonTask(
                id = resp.id,
                createdDate = resp.createdDate,
                title = resp.subject,
                ref = resp.ref,
                statusOld = StatusOld(
                    id = resp.status,
                    name = resp.statusExtraInfo.name,
                    color = resp.statusExtraInfo.color,
                    type = StatusType.Status
                ),
                assignee = resp.assignedToExtraInfo,
                projectDTOInfo = resp.projectDTOExtraInfo,
                taskType = commonTaskType,
                colors = resp.color?.let {
                    listOf(it)
                } ?: resp.epics.orEmpty().map { it.color },
                isClosed = resp.isClosed,
                tagOlds = resp.tags.orEmpty().map {
                    TagOld(name = it[0]!!, color = it[1].fixNullColor())
                },
                blockedNote = resp.blockedNote.takeIf { resp.isBlocked }
            )
        }

    suspend fun toDomainExtended(
        resp: CommonTaskResponse,
        commonTaskType: CommonTaskType,
        filters: FiltersDataDTO,
        swimlaneDTOS: List<SwimlaneDTO>,
        sprint: Sprint? = null,
        tagOlds: List<TagOld>,
        url: String
    ): CommonTaskExtended = withContext(ioDispatcher) {
        CommonTaskExtended(
            id = resp.id,
            statusOld = StatusOld(
                id = resp.status,
                name = resp.statusExtraInfo.name,
                color = resp.statusExtraInfo.color,
                type = StatusType.Status
            ),
            taskType = commonTaskType,
            createdDateTime = resp.createdDate,
            sprint = sprint,
            assignedIds = resp.assignedUsers ?: listOfNotNull(resp.assignedTo),
            watcherIds = resp.watchers.orEmpty(),
            creatorId = resp.owner ?: throw nullOwnerError,
            ref = resp.ref,
            title = resp.subject,
            isClosed = resp.isClosed,
            description = resp.description ?: "",
            epicsShortInfo = resp.epics.orEmpty(),
            projectSlug = resp.projectDTOExtraInfo.slug,
            tagOlds = tagOlds,
            swimlaneDTO = swimlaneDTOS.find { it.id == resp.swimlane },
            dueDate = resp.dueDate,
            dueDateStatusDTO = resp.dueDateStatusDTO,
            userStoryShortInfo = resp.userStoryExtraInfo,
            version = resp.version,
            color = resp.color,
            type = resp.type?.let { id -> filters.types.find { it.id == id } }
                ?.toStatus(StatusType.Type),
            severity = resp.severity?.let { id -> filters.severities.find { it.id == id } }
                ?.toStatus(StatusType.Severity),
            priority = resp.priority?.let { id -> filters.priorities.find { it.id == id } }
                ?.toStatus(StatusType.Priority),
            url = url,
            blockedNote = resp.blockedNote.takeIf { resp.isBlocked }
        )
    }
}
