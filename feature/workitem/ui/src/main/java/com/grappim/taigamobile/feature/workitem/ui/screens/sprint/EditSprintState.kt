package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import com.grappim.taigamobile.feature.sprint.domain.Sprint
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditSprintState(
    val itemsToShow: PersistentList<Sprint> = persistentListOf(),
    val isItemSelected: (sprintId: Long) -> Boolean,
    val selectedItem: Long? = null,
    val originalSelectedItem: Long? = null,

    val onSprintClick: (id: Long) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,

    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit
)
