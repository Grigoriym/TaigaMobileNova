package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.User

data class KanbanState(
    val isLoading: Boolean = false,
    val team: List<User> = emptyList(),
    val statuses: List<Status> = emptyList(),
    val stories: List<CommonTaskExtended> = emptyList(),
    val swimlanes: List<Swimlane> = emptyList(),
    val selectedSwimlane: Swimlane? = null,
    val error: Throwable? = null,
    val onRefresh: () -> Unit,
    val onSelectSwimlane: (Swimlane?) -> Unit
)
