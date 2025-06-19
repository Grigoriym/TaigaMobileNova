package com.grappim.taigamobile.scrum

import kotlin.enums.EnumEntries

data class ScrumState(
    val tabs: EnumEntries<ScrumTabs> = ScrumTabs.entries,

    val isCreateSprintDialogVisible: Boolean = false,
    val setIsCreateSprintDialogVisible: (Boolean) -> Unit
)
