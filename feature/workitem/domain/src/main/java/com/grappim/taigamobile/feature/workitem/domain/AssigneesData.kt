package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList

data class AssigneesData(val assignees: PersistentList<User>, val isAssignedToMe: Boolean, val newVersion: Long)
