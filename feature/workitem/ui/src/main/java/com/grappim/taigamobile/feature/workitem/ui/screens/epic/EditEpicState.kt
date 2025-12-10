package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import com.grappim.taigamobile.feature.epics.domain.Epic
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditEpicState(
    val itemsToShow: PersistentList<Epic> = persistentListOf(),
    val isItemSelected: (epicId: Long) -> Boolean,
    val selectedItems: PersistentList<Long> = persistentListOf(),
    val originalSelectedItems: PersistentList<Long> = persistentListOf(),

    val onEpicClick: (id: Long) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,

    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit
)
