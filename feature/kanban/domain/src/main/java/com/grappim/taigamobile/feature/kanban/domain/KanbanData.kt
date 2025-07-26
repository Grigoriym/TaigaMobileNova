package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.UserDTO

data class KanbanData(
    val statusOlds: List<StatusOld>,
    val stories: List<CommonTaskExtended>,
    val swimlaneDTOS: List<SwimlaneDTO>,
    val team: List<UserDTO>
)
