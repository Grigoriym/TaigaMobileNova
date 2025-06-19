package com.grappim.taigamobile.uikit.widgets.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import com.grappim.taigamobile.uikit.widgets.text.MarkdownText
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText

@Suppress("FunctionName")
fun LazyListScope.Description(description: String) {
    item {
        if (description.isNotEmpty()) {
            MarkdownText(
                text = description,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            NothingToSeeHereText()
        }
    }
}
