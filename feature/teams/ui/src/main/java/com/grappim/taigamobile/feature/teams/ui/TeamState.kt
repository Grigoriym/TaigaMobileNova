package com.grappim.taigamobile.feature.teams.ui

import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TeamState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val teamMembers: ImmutableList<TeamMember> = persistentListOf(),
    val onRefresh: () -> Unit
)
