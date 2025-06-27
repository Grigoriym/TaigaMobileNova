package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.User

data class KanbanData(
    val statuses: List<Status>,
    val stories: List<CommonTaskExtended>,
    val swimlanes: List<Swimlane>,
    val team: List<User>
)
