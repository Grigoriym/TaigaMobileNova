package com.grappim.taigamobile.feature.users.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class UserStats(
    val roles: ImmutableList<String> = persistentListOf(),
    val totalNumClosedUserStories: Int,
    val totalNumContacts: Int,
    val totalNumProjects: Int
)
