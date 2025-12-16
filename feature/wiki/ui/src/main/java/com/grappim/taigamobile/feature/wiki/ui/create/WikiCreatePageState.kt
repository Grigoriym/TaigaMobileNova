package com.grappim.taigamobile.feature.wiki.ui.create

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.utils.ui.NativeText

data class WikiCreatePageState(
    val title: TextFieldValue = TextFieldValue(""),
    val setTitle: (TextFieldValue) -> Unit = {},

    val description: TextFieldValue = TextFieldValue(""),
    val setDescription: (TextFieldValue) -> Unit = {},

    val onCreateWikiPage: () -> Unit = {},
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
