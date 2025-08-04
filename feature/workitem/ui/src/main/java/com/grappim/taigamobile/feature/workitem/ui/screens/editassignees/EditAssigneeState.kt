package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditAssigneeState(
    val itemsToShow: PersistentList<TeamMemberUI> = persistentListOf(),
    val isItemSelected: (memberId: Long) -> Boolean,
    val wasItemChanged: (shouldReturnCurrentValue: Boolean) -> Boolean,
    val selectedItems: PersistentList<Long> = persistentListOf(),
    val originalSelectedItems: PersistentList<Long> = persistentListOf(),

    val onTeamMemberClick: (id: Long) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit
)
