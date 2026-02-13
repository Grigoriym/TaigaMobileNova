package com.grappim.taigamobile.feature.sprint.ui

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

data class SprintState(
    val sprint: Sprint? = null,
    val sprintToolbarTitle: NativeText = NativeText.Empty,
    val sprintToolbarSubtitle: NativeText = NativeText.Empty,

    val statuses: ImmutableList<Statuses> = persistentListOf(),
    val storiesWithTasks: ImmutableMap<WorkItem, List<WorkItem>> = persistentMapOf(),
    val issues: ImmutableList<WorkItem> = persistentListOf(),
    val storylessTasks: ImmutableList<WorkItem> = persistentListOf(),

    val onRefresh: () -> Unit = {},

    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isMenuExpanded: Boolean = false,
    val setIsMenuExpanded: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val onDeleteSprint: () -> Unit = {},

    val onEditSprintConfirm: () -> Unit = {},
    val onEditSprintClick: () -> Unit = {},

    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val canShowTopBarActions: Boolean = false,
    val canCreateIssue: Boolean = false,
    val canCreateTasks: Boolean = false
)
