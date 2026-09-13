package com.grappim.taigamobile.feature.wiki.ui.page.create

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.kit.uikit.NativeText

data class WikiCreatePageState(
    val slug: String = "",
    val setSlug: (String) -> Unit = {},

    val content: TextFieldValue = TextFieldValue(),
    val setContent: (TextFieldValue) -> Unit = {},

    val onCreateWikiPage: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
