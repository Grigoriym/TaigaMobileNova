package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.User
import kotlinx.collections.immutable.ImmutableList

data class AssigneesData(
    val assignees: ImmutableList<User>,
    val isAssignedToMe: Boolean,
    val newVersion: Long
)
