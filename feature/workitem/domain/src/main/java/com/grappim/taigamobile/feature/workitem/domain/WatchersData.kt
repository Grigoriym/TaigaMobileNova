package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList

data class WatchersData(val watchers: ImmutableList<User>, val isWatchedByMe: Boolean)
