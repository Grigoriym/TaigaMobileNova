package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

fun getUserStory(): UserStory = UserStory(
    id = getRandomLong(),
    version = getRandomLong(),
    createdDateTime = LocalDateTime.now(),
    title = getRandomString(),
    ref = getRandomInt(),
    status = Status(
        color = getRandomString(),
        id = getRandomLong(),
        name = getRandomString()
    ),
    assignee = getUser(),
    project = getProject(),
    isClosed = getRandomBoolean(),
    blockedNote = null,
    description = getRandomString(),
    milestone = null,
    creatorId = getRandomLong(),
    assignedUserIds = listOf(getRandomLong(), getRandomLong()),
    watcherUserIds = listOf(getRandomLong()),
    tags = persistentListOf(
        Tag(color = getRandomString(), name = getRandomString()),
        Tag(color = getRandomString(), name = getRandomString())
    ),
    dueDate = nowLocalDate,
    dueDateStatus = DueDateStatus.Set,
    copyLinkUrl = getRandomString(),
    userStoryEpics = persistentListOf(
        UserStoryEpic(
            id = getRandomLong(),
            title = getRandomString(),
            ref = getRandomInt(),
            color = getRandomString()
        )
    ),
    swimlane = null
)
