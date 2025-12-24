package com.grappim.taigamobile.feature.wiki.ui.list

import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WikiListState(
    val allPages: ImmutableList<WikiUIItem> = persistentListOf(),
    val bookmarks: ImmutableList<WikiUIItem> = persistentListOf(),

    val onOpen: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
