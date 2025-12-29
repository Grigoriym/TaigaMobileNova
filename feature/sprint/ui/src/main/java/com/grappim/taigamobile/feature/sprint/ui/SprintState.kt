package com.grappim.taigamobile.feature.sprint.ui

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class SprintState(
    val sprint: Sprint? = null,
    val sprintToolbarTitle: NativeText = NativeText.Empty,
    val sprintToolbarSubtitle: NativeText = NativeText.Empty,

    val statuses: ImmutableList<Statuses> = persistentListOf(),
    val storiesWithTasks: Map<WorkItem, List<WorkItem>> = emptyMap(),
    val issues: ImmutableList<WorkItem> = persistentListOf(),
    val storylessTasks: ImmutableList<WorkItem> = persistentListOf(),

    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isMenuExpanded: Boolean = false,
    val setIsMenuExpanded: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val onDeleteSprint: () -> Unit = {},

    val onEditSprintConfirm: () -> Unit = {},
    val onEditSprintClick: () -> Unit = {}
)
