package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.feature.workitem.data.StatusExtraInfoDTO
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import java.time.LocalDate
import java.time.LocalDateTime

fun getCommonTaskResponse(): CommonTaskResponse =
    CommonTaskResponse(
        id = getRandomLong(),
        subject = getRandomString(),
        createdDate = LocalDateTime.of(2024, 12, 15, 10, 30, 0),
        status = getRandomLong(),
        ref = getRandomInt(),
        assignedToExtraInfo = getUserDTO(),
        statusExtraInfo = CommonTaskResponse.StatusExtra(
            color = "#FF5722",
            name = getRandomString()
        ),
        projectDTOExtraInfo = getProjectDTO(),
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
        dueDate = LocalDate.of(2024, 12, 20),
        dueDateStatusDTO = null,
        blockedNote = getRandomString(),
        isBlocked = getRandomBoolean(),
        color = "#4CAF50",
        type = getRandomLong(),
        severity = getRandomLong(),
        priority = getRandomLong()
    )

fun getCommonTask(newId: Long = getRandomLong()): CommonTask = CommonTask(
    id = newId,
    createdDate = LocalDateTime.of(2024, 12, 15, 9, 15, 30),
    title = getRandomString(),
    ref = getRandomInt(),
    statusOld = StatusOld(
        id = getRandomLong(),
        name = getRandomString(),
        color = "#2196F3",
        type = StatusType.Status
    ),
    assignee = getUserDTO(),
    projectDTOInfo = getProjectDTO(),
    taskType = CommonTaskType.Issue,
    isClosed = getRandomBoolean(),
    tags = listOf(
        Tag(name = getRandomString(), color = "#FF9800"),
        Tag(name = getRandomString(), color = "#4CAF50")
    ),
    colors = listOf("#E91E63", "#9C27B0"),
    blockedNote = getRandomString()
)

fun getWorkItemResponseDTO(): WorkItemResponseDTO =
    WorkItemResponseDTO(
        id = getRandomLong(),
        subject = getRandomString(),
        createdDate = LocalDateTime.of(2024, 12, 15, 10, 30, 0),
        status = getRandomLong(),
        ref = getRandomInt(),
        assignedToExtraInfo = getUserDTO(),
        statusExtraInfo = StatusExtraInfoDTO(
            color = "#FF5722",
            name = getRandomString()
        ),
        projectDTOExtraInfo = getProjectDTO(),
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
        dueDate = LocalDate.of(2024, 12, 20),
        dueDateStatusDTO = null,
        blockedNote = getRandomString(),
        isBlocked = getRandomBoolean(),
        color = "#4CAF50",
        type = getRandomLong(),
        severity = getRandomLong(),
        priority = getRandomLong()
    )
