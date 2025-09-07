package com.grappim.taigamobile.feature.teams.ui

import com.grappim.taigamobile.core.domain.TeamMemberDTO

data class TeamState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val teamMemberDTOS: List<TeamMemberDTO> = emptyList(),
    val onRefresh: () -> Unit
)
