package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.ProjectDTO

fun getProjectDTO(): ProjectDTO = ProjectDTO(
    id = 6498,
    name = "Jeremy Reilly",
    slug = "elit",
    isMember = false,
    isAdmin = false,
    isOwner = false,
    description = null,
    avatarUrl = null,
    members = listOf(),
    fansCount = 4902,
    watchersCount = 3986,
    isPrivate = false
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
    members = listOf(getRandomLong()),
    fansCount = getRandomInt(),
    watchersCount = getRandomInt(),
    isPrivate = getRandomBoolean()
)
