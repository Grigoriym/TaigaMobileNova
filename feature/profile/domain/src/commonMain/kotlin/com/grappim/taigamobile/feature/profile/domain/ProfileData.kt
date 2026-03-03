package com.grappim.taigamobile.feature.profile.domain

import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UserStats
import kotlinx.collections.immutable.ImmutableList

data class ProfileData(val user: User, val userStats: UserStats, val projects: ImmutableList<Project>)
