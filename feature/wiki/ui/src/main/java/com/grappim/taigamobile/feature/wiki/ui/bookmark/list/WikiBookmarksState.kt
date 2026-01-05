package com.grappim.taigamobile.feature.wiki.ui.bookmark.list

import com.grappim.taigamobile.feature.wiki.ui.model.WikiUIItem
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WikiBookmarksState(
    val bookmarks: ImmutableList<WikiUIItem> = persistentListOf(),
    val onOpen: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val canAddWikiLink: Boolean = false,
    val canDeleteWikiLink: Boolean = false,

    val onDeleteClick: (Long) -> Unit = {},
    val isRemoveBookmarkDialogVisible: Boolean = false,
    val bookmarkIdToDelete: Long? = null,
    val onConfirmDelete: () -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {}
)
