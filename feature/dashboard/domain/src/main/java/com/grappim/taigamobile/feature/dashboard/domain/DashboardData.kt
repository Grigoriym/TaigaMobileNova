package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList

data class DashboardData(val workingOn: ImmutableList<WorkItem>, val watching: ImmutableList<WorkItem>)
