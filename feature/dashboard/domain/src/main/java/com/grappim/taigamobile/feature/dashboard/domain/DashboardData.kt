package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Project

data class DashboardData(
    val workingOn: List<CommonTask> = emptyList(),
    val watching: List<CommonTask> = emptyList(),
    val myProjects: List<Project> = emptyList()
)
