package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditAssigneeState(
    val assignees: PersistentList<TeamMemberUI> = persistentListOf(),
    val selectedTeamMemberId: Long? = null,
    val originalTeamMemberId: Long? = null,
    val onTeamMemberClick: (TeamMemberUI) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,
    val wasAssigneeChanged: (shouldReturnCurrentValue: Boolean) -> Boolean
)
