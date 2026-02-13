package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectExtraInfoDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import kotlinx.collections.immutable.persistentListOf

fun getProjectResponseDTO(): ProjectResponseDTO = ProjectResponseDTO(
    id = getRandomLong(),
    name = getRandomString(),
    members = listOf(getProjectMemberDTO(), getProjectMemberDTO())
)

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
    isWikiActivated = getRandomBoolean(),
    defaultSwimlane = getRandomLong()
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

fun getProjectEntity(): ProjectEntity = ProjectEntity(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    myPermissions = listOf(TaigaPermission.VIEW_PROJECT, TaigaPermission.ADD_US),
    isEpicsActivated = getRandomBoolean(),
    isBacklogActivated = getRandomBoolean(),
    isKanbanActivated = getRandomBoolean(),
    isIssuesActivated = getRandomBoolean(),
    isWikiActivated = getRandomBoolean(),
    defaultSwimlane = getRandomLong(),
    isAdmin = getRandomBoolean(),
    isMember = getRandomBoolean(),
    isOwner = getRandomBoolean(),
    description = getRandomString(),
    avatarUrl = getRandomString()
)

fun getProjectSimple(): ProjectSimple = ProjectSimple(
    id = getRandomLong(),
    name = getRandomString(),
    slug = getRandomString(),
    myPermissions = persistentListOf(TaigaPermission.VIEW_PROJECT),
    isEpicsActivated = getRandomBoolean(),
    isBacklogActivated = getRandomBoolean(),
    isKanbanActivated = getRandomBoolean(),
    isIssuesActivated = getRandomBoolean(),
    isWikiActivated = getRandomBoolean(),
    defaultSwimlane = getRandomLong(),
    isAdmin = getRandomBoolean()
)
