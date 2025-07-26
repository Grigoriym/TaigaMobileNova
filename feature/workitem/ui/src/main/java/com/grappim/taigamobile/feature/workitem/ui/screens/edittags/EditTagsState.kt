package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class EditTagsState(
    val tags: ImmutableList<TagUI> = persistentListOf(),
    val originalSelectedTags: ImmutableList<TagUI>,
    val currentSelectedTags: PersistentList<TagUI>,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,
    val onTagClick: (TagUI) -> Unit,
    val wereTagsChanged: (Boolean) -> Boolean
)
