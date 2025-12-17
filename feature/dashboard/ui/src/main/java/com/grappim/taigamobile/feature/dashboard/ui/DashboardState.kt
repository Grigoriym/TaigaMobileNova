package com.grappim.taigamobile.feature.dashboard.ui

import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DashboardState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val currentProjectId: Long = -1,

    val workingOn: ImmutableList<WorkItem> = persistentListOf(),
    val watching: ImmutableList<WorkItem> = persistentListOf(),

    val onLoad: () -> Unit = {}
)
