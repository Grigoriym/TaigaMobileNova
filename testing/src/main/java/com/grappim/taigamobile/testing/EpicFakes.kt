package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsData
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

fun getEpic(
    id: Long = getRandomLong(),
    version: Long = getRandomLong()
): Epic = Epic(
    id = id,
    version = version,
    ref = getRandomLong(),
    creatorId = getRandomLong(),
    title = getRandomString(),
    description = getRandomString(),
    createdDateTime = LocalDateTime.now(),
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
    epicColor = getRandomString()
)

fun getEpicDetailsData(
    epic: Epic = getEpic()
): EpicDetailsData = EpicDetailsData(
    epic = epic,
    attachments = persistentListOf(
        getAttachment(),
        getAttachment()
    ),
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
    userStories = persistentListOf(),
    canDeleteEpic = true,
    canModifyEpic = true,
    canComment = true
)
