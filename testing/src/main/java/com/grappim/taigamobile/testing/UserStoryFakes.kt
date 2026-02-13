package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsData
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.userstories.dto.UserStoryShortInfoDTO
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

fun getUserStoryShortInfoDTO(): UserStoryShortInfoDTO = UserStoryShortInfoDTO(
    id = getRandomLong(),
    ref = getRandomLong(),
    title = getRandomString(),
    epics = null
)

fun getUserStory(
    id: Long = getRandomLong(),
    version: Long = getRandomLong()
): UserStory = UserStory(
    id = id,
    version = version,
    createdDateTime = LocalDateTime.now(),
    title = getRandomString(),
    ref = getRandomLong(),
    status = Status(
        color = getRandomString(),
        id = getRandomLong(),
        name = getRandomString()
    ),
    assignee = getUser(),
    project = getProjectExtraInfo(),
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
            ref = getRandomLong(),
            color = getRandomString()
        )
    ),
    swimlane = null,
    kanbanOrder = getRandomLong()
)

fun getUserStoryDetailsData(
    userStory: UserStory = getUserStory()
): UserStoryDetailsData = UserStoryDetailsData(
    userStory = userStory,
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
    canDeleteUserStory = true,
    canModifyUserStory = true,
    canComment = true,
    canModifyRelatedEpic = true
)
