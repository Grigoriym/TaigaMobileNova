package com.grappim.taigamobile.feature.wiki.ui.page.list

import com.grappim.taigamobile.feature.wiki.ui.model.WikiUIItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WikiPagesState(
    val allPages: ImmutableList<WikiUIItem> = persistentListOf(),
    val refresh: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val canAddWikiPage: Boolean = false,
    val canDeleteWikiPage: Boolean = false,

    val onDeleteClick: (Long) -> Unit = {},
    val isRemovePageDialogVisible: Boolean = false,
    val pageIdToDelete: Long? = null,
    val onConfirmDelete: () -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {}
)
