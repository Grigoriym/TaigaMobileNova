package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.User
import kotlinx.collections.immutable.ImmutableList

data class WatchersData(val watchers: ImmutableList<User>, val isWatchedByMe: Boolean)
