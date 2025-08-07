package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditTeamMemberState(
    val itemsToShow: PersistentList<TeamMemberUI> = persistentListOf(),
    val isItemSelected: (memberId: Long) -> Boolean,
    val selectedItems: PersistentList<Long> = persistentListOf(),
    val originalSelectedItems: PersistentList<Long> = persistentListOf(),

    val onTeamMemberClick: (id: Long) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,

    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit
)
