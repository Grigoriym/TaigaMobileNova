package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList

data class WatchersListUpdateData(val version: Long, val isWatchedByMe: Boolean, val watchers: ImmutableList<User>)
