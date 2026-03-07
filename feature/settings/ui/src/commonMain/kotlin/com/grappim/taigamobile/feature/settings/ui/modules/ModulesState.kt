package com.grappim.taigamobile.feature.settings.ui.modules

import com.grappim.taigamobile.utils.ui.NativeText

data class ModulesState(
    val isEpicsActivated: Boolean = false,
    val isBacklogActivated: Boolean = false,
    val isKanbanActivated: Boolean = false,
    val isIssuesActivated: Boolean = false,
    val isWikiActivated: Boolean = false,
    val totalMilestones: String = "",
    val totalStoryPoints: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val onEpicsActivatedChange: (Boolean) -> Unit = {},
    val onBacklogActivatedChange: (Boolean) -> Unit = {},
    val onKanbanActivatedChange: (Boolean) -> Unit = {},
    val onIssuesActivatedChange: (Boolean) -> Unit = {},
    val onWikiActivatedChange: (Boolean) -> Unit = {},
    val onTotalMilestonesChange: (String) -> Unit = {},
    val onTotalStoryPointsChange: (String) -> Unit = {},
    val onSaveClick: () -> Unit = {}
)
