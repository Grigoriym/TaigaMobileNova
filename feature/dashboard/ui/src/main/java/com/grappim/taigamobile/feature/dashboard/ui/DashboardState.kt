package com.grappim.taigamobile.feature.dashboard.ui

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.ProjectDTO

data class DashboardState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val currentProjectId: Long = -1,

    val workingOn: List<CommonTask> = emptyList(),
    val watching: List<CommonTask> = emptyList(),
    val myProjectDTOS: List<ProjectDTO> = emptyList()
)
