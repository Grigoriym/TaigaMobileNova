package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.User
import kotlinx.collections.immutable.ImmutableList

data class WatchersListUpdateData(
    val version: Long,
    val isWatchedByMe: Boolean,
    val watchers: ImmutableList<User>
)
