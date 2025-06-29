package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import coil.imageLoader
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
@Deprecated("markdown uses coil2, while the project uses coil3")
fun MarkdownTextWidget(text: String, modifier: Modifier = Modifier, isSelectable: Boolean = true) {
    val context = LocalContext.current

    MarkdownText(
        modifier = modifier,
        markdown = text,
        isTextSelectable = isSelectable,
        imageLoader = context.imageLoader,
        style = TextStyle(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            color = MaterialTheme.colorScheme.onSurface
        )
    )
}
