package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class TagsScreenState(
    val isLoading: Boolean = false,
    val isOperationLoading: Boolean = false,
    val tags: ImmutableList<TagUI> = persistentListOf(),

    val refresh: () -> Unit = {},

    val onTagEditClick: (TagUI) -> Unit = {},

    val deleteTag: () -> Unit = {},
    val onTagDeleteClick: (TagUI) -> Unit = {},
    val isDeleteTagDialogVisible: Boolean = false,
    val closeDeleteDialog: () -> Unit = {},

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit = {},

    val onAddTagClick: () -> Unit = {},
    val onMergeTagsClick: () -> Unit = {},

    val isMergeMode: Boolean = false,
    val mainTagName: String? = null,
    val tagsToMerge: ImmutableSet<String> = persistentSetOf(),
    val onMainTagSelect: (TagUI) -> Unit = {},
    val onTagToMergeToggle: (TagUI) -> Unit = {},
    val onCancelMerge: () -> Unit = {},
    val onConfirmMerge: () -> Unit = {},

    val onSaveClick: (name: String, color: Color) -> Unit = { _, _ -> }
)
