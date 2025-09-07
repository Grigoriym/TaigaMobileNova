package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.UserDTO

data class KanbanState(
    val isLoading: Boolean = false,
    val team: List<UserDTO> = emptyList(),
    val statusOlds: List<StatusOld> = emptyList(),
    val stories: List<CommonTaskExtended> = emptyList(),
    val swimlaneDTOS: List<SwimlaneDTO> = emptyList(),
    val selectedSwimlaneDTO: SwimlaneDTO? = null,
    val error: Throwable? = null,
    val onRefresh: () -> Unit,
    val onSelectSwimlane: (SwimlaneDTO?) -> Unit
)
