package com.grappim.taigamobile.feature.profile.ui

import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProfileState(
    val user: User? = null,
    val userStats: UserStats? = null,
    val projects: ImmutableList<Project> = persistentListOf(),
    val currentProjectId: Long = -1,

    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val onReload: () -> Unit = {}
)
