package com.grappim.taigamobile.feature.sprint.domain

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.StatusOld

data class SprintData(
    val sprint: Sprint,
    val statusOlds: List<StatusOld>,
    val storiesWithTasks: Map<CommonTask, List<CommonTask>>,
    val issues: List<CommonTask>,
    val storylessTasks: List<CommonTask>
)
