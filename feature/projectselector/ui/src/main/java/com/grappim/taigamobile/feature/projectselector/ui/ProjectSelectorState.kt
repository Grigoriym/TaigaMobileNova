package com.grappim.taigamobile.feature.projectselector.ui

import androidx.compose.runtime.Stable
import com.grappim.taigamobile.core.domain.Project

@Stable
data class ProjectSelectorState(
    val isFromLogin: Boolean = false,
    val currentProjectId: Long = -1,

    val setProjectsQuery: (String) -> Unit,
    val setProject: (Project) -> Unit
)
