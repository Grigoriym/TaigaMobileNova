package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectExtraInfoDTO
import kotlinx.collections.immutable.persistentListOf

fun getProjectDTO(): ProjectDTO = ProjectDTO(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    isMember = false,
    isAdmin = false,
    isOwner = false,
    description = getRandomString(),
    avatarUrl = null,
    members = listOf(),
    fansCount = 4902,
    watchersCount = 3986,
    isPrivate = getRandomBoolean(),
    myPermissions = persistentListOf(),
    isEpicsActivated = getRandomBoolean(),
    isBacklogActivated = getRandomBoolean(),
    isKanbanActivated = getRandomBoolean(),
    isIssuesActivated = getRandomBoolean(),
    isWikiActivated = getRandomBoolean()
)

fun getProject(): Project = Project(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    isMember = getRandomBoolean(),
    isAdmin = getRandomBoolean(),
    isOwner = getRandomBoolean(),
    description = getRandomString(),
    avatarUrl = getRandomString(),
    members = persistentListOf(getRandomLong()),
    fansCount = getRandomInt(),
    watchersCount = getRandomInt(),
    isPrivate = getRandomBoolean(),
    myPermissions = persistentListOf(),
    isEpicsActivated = getRandomBoolean(),
    isBacklogActivated = getRandomBoolean(),
    isKanbanActivated = getRandomBoolean(),
    isIssuesActivated = getRandomBoolean(),
    isWikiActivated = getRandomBoolean()
)


fun getProjectExtraInfo(): ProjectExtraInfo = ProjectExtraInfo(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    logoSmallUrl = getRandomString()
)

fun getProjectExtraInfoDTO(): ProjectExtraInfoDTO = ProjectExtraInfoDTO(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    logoSmallUrl = getRandomString()
)
