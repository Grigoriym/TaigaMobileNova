package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.tasks.domain.Task
import com.grappim.taigamobile.feature.tasks.domain.TaskDetailsData
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.LocalDateTime

fun getTask(
    id: Long = getRandomLong(),
    version: Long = getRandomLong()
): Task = Task(
    id = id,
    version = version,
    ref = getRandomLong(),
    creatorId = getRandomLong(),
    title = getRandomString(),
    description = getRandomString(),
    createdDateTime = LocalDateTime.now(),
    dueDate = LocalDate.now().plusDays(getRandomLong() % 30),
    dueDateStatus = DueDateStatus.Set,
    project = getProjectExtraInfo(),
    isClosed = getRandomBoolean(),
    tags = persistentListOf(getTag()),
    blockedNote = getRandomString(),
    assignee = getUser(),
    assignedUserIds = listOf(getRandomLong()),
    watcherUserIds = listOf(getRandomLong()),
    milestone = getRandomLong(),
    copyLinkUrl = getRandomString(),
    status = getStatus(),
    userStory = null
)

fun getTaskDetailsData(
    task: Task = getTask()
): TaskDetailsData = TaskDetailsData(
    task = task,
    attachments = persistentListOf(
        getAttachment(),
        getAttachment()
    ),
    sprint = getSprint(),
    customFields = getCustomFields(),
    comments = persistentListOf(
        getComment(),
        getComment()
    ),
    creator = getUser(),
    assignees = persistentListOf(
        getUser(),
        getUser()
    ),
    watchers = persistentListOf(
        getUser(),
        getUser()
    ),
    isAssignedToMe = getRandomBoolean(),
    isWatchedByMe = getRandomBoolean(),
    filtersData = getFiltersData(),
    canDeleteTask = true,
    canModifyTask = true,
    canComment = true
)
