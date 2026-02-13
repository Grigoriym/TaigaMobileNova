package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.feature.workitem.dto.StatusExtraInfoDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDateTime

fun getWorkItemResponseDTO(): WorkItemResponseDTO =
    WorkItemResponseDTO(
        id = getRandomLong(),
        subject = getRandomString(),
        createdDate = nowLocalDateTime,
        status = getRandomLong(),
        ref = getRandomLong(),
        assignedToExtraInfo = getUserDTO(),
        statusExtraInfo = StatusExtraInfoDTO(
            color = "#FF5722",
            name = getRandomString()
        ),
        projectDTOExtraInfo = getProjectExtraInfoDTO(),
        milestone = getRandomLong(),
        assignedUsers = listOf(getRandomLong(), getRandomLong()),
        assignedTo = getRandomLong(),
        watchers = listOf(getRandomLong(), getRandomLong(), getRandomLong()),
        owner = getRandomLong(),
        description = getRandomString(),
        epics = null,
        userStoryExtraInfo = null,
        version = getRandomLong(),
        isClosed = getRandomBoolean(),
        tags = listOf(
            listOf(getRandomString(), "#FF0000"),
            listOf(getRandomString(), "#FFA500")
        ),
        swimlane = getRandomLong(),
        dueDate = nowLocalDate,
        dueDateStatusDTO = null,
        blockedNote = getRandomString(),
        isBlocked = getRandomBoolean(),
        color = "#4CAF50",
        type = getRandomLong(),
        severity = getRandomLong(),
        priority = getRandomLong(),
        generatedUserStories = persistentListOf(),
        fromTaskRef = null,
        kanbanOrder = getRandomLong()
    )

fun getWorkItem(
    id: Long = getRandomLong(),
    taskType: CommonTaskType = CommonTaskType.Task,
    createdDate: LocalDateTime = getRandomLocalDateTime(),
    isBlocked: Boolean = getRandomBoolean(),
    isClosed: Boolean = getRandomBoolean()
): WorkItem = WorkItem(
    id = id,
    taskType = taskType,
    createdDate = createdDate,
    status = getStatus(),
    ref = getRandomLong(),
    title = getRandomString(),
    isBlocked = isBlocked,
    tags = persistentListOf(getTag(), getTag()),
    isClosed = isClosed,
    colors = persistentListOf("#FF0000", "#00FF00"),
    assignee = getUser(),
    blockedNote = getRandomString(),
    project = getProjectExtraInfo()
)
