package com.grappim.taigamobile.feature.wiki.ui.page.create

import com.grappim.taigamobile.utils.ui.NativeText

data class WikiCreatePageState(
    val slug: String = "",
    val setSlug: (String) -> Unit = {},

    val content: String = "",
    val setContent: (String) -> Unit = {},

    val onCreateWikiPage: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
