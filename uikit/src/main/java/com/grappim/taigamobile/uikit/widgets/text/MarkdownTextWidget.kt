package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown

/**
 * If you define onClick, either internally or externally, make isSelectable = false as well
 */
@Composable
fun MarkdownTextWidget(text: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Markdown(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable { onClick() }
                } else {
                    modifier
                }
            ),
        content = text,
        imageTransformer = Coil3ImageTransformerImpl
    )
}
