package com.grappim.taigamobile.feature.epics.domain

import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList

data class EpicColorUpdateData(val patchedData: PatchedData, val userStories: ImmutableList<WorkItem>)
