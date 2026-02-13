package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditTagsState(
    val tags: ImmutableList<SelectableTagUI> = persistentListOf(),
    val originalSelectedTags: ImmutableList<SelectableTagUI> = persistentListOf(),
    val currentSelectedTags: PersistentList<SelectableTagUI> = persistentListOf(),
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit = {},
    val onTagClick: (SelectableTagUI) -> Unit = {},
    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit = {},

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit = {},
    val onAddTagDropdownClick: () -> Unit = {},
    val onSaveTagDropdownClick: () -> Unit = {},
    val onBackClick: () -> Unit = {},

    val onSaveClick: (name: String, color: Color) -> Unit = { _, _ -> }
)
