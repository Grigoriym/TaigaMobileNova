package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.utils.ui.textColor
import com.grappim.taigamobile.utils.ui.toColor

/**
 * Text with colored dots (indicators) at the end and tags
 */
@Composable
fun CommonTaskTitle(
    ref: Int,
    title: String,
    modifier: Modifier = Modifier,
    isInactive: Boolean = false,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    indicatorColorsHex: List<String> = emptyList(),
    tags: List<Tag> = emptyList(),
    isBlocked: Boolean = false
) {
    Column(modifier = modifier) {
        val space = 4.dp

        Text(
            text = buildAnnotatedString {
                if (isInactive) {
                    pushStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                }
                append(stringResource(RString.title_with_ref_pattern).format(ref, title))
                if (isInactive) pop()

                append(" ")

                indicatorColorsHex.forEach {
                    pushStyle(SpanStyle(color = it.toColor()))
                    append("⬤") // 2B24
                    pop()
                }
            },
            color = if (isBlocked) taigaRed else textColor,
            style = MaterialTheme.typography.titleMedium
        )

        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(space))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(space),
                verticalArrangement = Arrangement.spacedBy(space)
            ) {
                tags.forEach {
                    val bgColor = it.color.toColor()

                    Chip(color = bgColor) {
                        Text(
                            text = it.name,
                            color = bgColor.textColor(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommonTaskTitlePreview() = TaigaMobileTheme {
    CommonTaskTitle(
        ref = 42,
        title = "Some title",
        tags = listOf(Tag("one", "#25A28C"), Tag("two", "#25A28C")),
        isBlocked = true
    )
}
