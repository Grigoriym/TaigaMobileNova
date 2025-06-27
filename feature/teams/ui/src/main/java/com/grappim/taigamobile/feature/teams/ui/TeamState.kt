package com.grappim.taigamobile.feature.teams.ui

import com.grappim.taigamobile.core.domain.TeamMember

data class TeamState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val teamMembers: List<TeamMember> = emptyList(),
    val onRefresh: () -> Unit
)
