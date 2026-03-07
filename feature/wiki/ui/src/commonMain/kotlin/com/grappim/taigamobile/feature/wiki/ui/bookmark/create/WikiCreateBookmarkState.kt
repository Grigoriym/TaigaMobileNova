package com.grappim.taigamobile.feature.wiki.ui.bookmark.create

import com.grappim.taigamobile.utils.ui.NativeText

data class WikiCreateBookmarkState(
    val title: String = "",
    val setTitle: (String) -> Unit = {},

    val onCreateWikiBookmark: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
