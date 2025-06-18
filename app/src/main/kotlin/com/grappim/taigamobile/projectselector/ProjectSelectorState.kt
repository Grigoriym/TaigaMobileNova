package com.grappim.taigamobile.projectselector

import androidx.compose.runtime.Stable

@Stable
data class ProjectSelectorState(
    val isFromLogin: Boolean = false,
    val currentProjectId: Long,

    val setProjectsQuery: (String) -> Unit
)
