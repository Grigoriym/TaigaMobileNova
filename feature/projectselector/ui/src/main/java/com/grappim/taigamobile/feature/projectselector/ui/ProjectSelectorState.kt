package com.grappim.taigamobile.feature.projectselector.ui

import androidx.compose.runtime.Stable

@Stable
data class ProjectSelectorState(
    val isFromLogin: Boolean = false,
    val currentProjectId: Long,

    val setProjectsQuery: (String) -> Unit
)
