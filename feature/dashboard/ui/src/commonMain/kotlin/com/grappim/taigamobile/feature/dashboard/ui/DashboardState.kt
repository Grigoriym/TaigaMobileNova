package com.grappim.taigamobile.feature.dashboard.ui

import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DashboardSectionState(
    val items: ImmutableList<WorkItem> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isExpanded: Boolean = false,
    val isRetrying: Boolean = false
)

data class DashboardState(
    val currentProjectId: Long = -1,

    val watchingSection: DashboardSectionState = DashboardSectionState(),
    val myWorkSection: DashboardSectionState = DashboardSectionState(),
    val recentActivitySection: DashboardSectionState = DashboardSectionState(),
    val recentlyCompletedSection: DashboardSectionState = DashboardSectionState(),

    val onToggleWatching: () -> Unit = {},
    val onToggleMyWork: () -> Unit = {},
    val onToggleRecentActivity: () -> Unit = {},
    val onToggleRecentlyCompleted: () -> Unit = {},

    val onRetryWatching: () -> Unit = {},
    val onRetryMyWork: () -> Unit = {},
    val onRetryRecentActivity: () -> Unit = {},
    val onRetryRecentlyCompleted: () -> Unit = {},

    val retry: () -> Unit = {}
)
