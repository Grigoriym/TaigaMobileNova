package com.grappim.taigamobile.testing

import com.grappim.taigamobile.core.domain.ProjectDTO

fun getProject(): ProjectDTO = ProjectDTO(
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
