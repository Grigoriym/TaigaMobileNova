package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight

@Composable
fun ExpandableMarkdownText(text: String, modifier: Modifier = Modifier, maxLinesCollapsed: Int = 6) {
    var isExpanded by remember { mutableStateOf(false) }
    var naturalHeight by remember { mutableStateOf(0.dp) }
    val maxHeight = (maxLinesCollapsed * 24).dp
    val density = LocalDensity.current

    val isContentTooLong = naturalHeight > maxHeight

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (!isExpanded && isContentTooLong) {
                            Modifier.heightIn(max = maxHeight)
                        } else {
                            Modifier
                        }
                    )
            ) {
                MarkdownTextWidget(
                    text = text,
                    modifier = Modifier.onSizeChanged { size ->
                        with(density) {
                            val currentHeight = size.height.toDp()
                            if (isExpanded || naturalHeight == 0.dp) {
                                if (currentHeight > naturalHeight) {
                                    naturalHeight = currentHeight
                                }
                            }
                        }
                    }
                )
            }

            if (!isExpanded && isContentTooLong) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                )
            }
        }

        if (isContentTooLong) {
            TextButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(
                        if (isExpanded) RString.show_less else RString.show_more
                    ),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private const val SAMPLE_MARKDOWN = """
# Task Description

This is a **long description** with multiple paragraphs and various markdown elements to test the expandable functionality.

## Requirements

- First requirement with *italic* text
- Second requirement with `inline code`
- Third requirement with ~~strikethrough~~

The description continues here with more details about the task at hand. This should definitely exceed the 6-line limit to demonstrate the collapsible behavior.

### Additional Notes

> This is a blockquote that provides important context about the task.

And here's some more text to make it even longer and ensure we trigger the collapse behavior properly.
"""

@PreviewTaigaDarkLight
@Composable
private fun PreviewExpandableMarkdownText() {
    MaterialTheme {
        ExpandableMarkdownText(text = SAMPLE_MARKDOWN)
    }
}

@PreviewTaigaDarkLight
@Composable
private fun PreviewExpandableMarkdownTextShortText() {
    MaterialTheme {
        ExpandableMarkdownText(text = "Short text that doesn't need expansion")
    }
}
