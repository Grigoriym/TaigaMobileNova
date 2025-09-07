package com.grappim.taigamobile.feature.sprint.ui

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.utils.ui.NativeText

data class SprintState(
    val sprint: Sprint? = null,
    val sprintToolbarTitle: NativeText = NativeText.Empty,
    val sprintToolbarSubtitle: NativeText = NativeText.Empty,

    val statusOlds: List<StatusOld> = emptyList(),
    val storiesWithTasks: Map<CommonTask, List<CommonTask>> = emptyMap(),
    val issues: List<CommonTask> = emptyList(),
    val storylessTasks: List<CommonTask> = emptyList(),

    val isLoading: Boolean = false,
    val isMenuExpanded: Boolean = false,
    val setIsMenuExpanded: (Boolean) -> Unit = {},
    val isEditDialogVisible: Boolean = false,
    val setIsEditDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val setIsDeleteDialogVisible: (Boolean) -> Unit = {}
)
