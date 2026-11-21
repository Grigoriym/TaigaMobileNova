package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.filled.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.edit
import com.grappim.taigamobile.strings.generated.resources.markdown_bold
import com.grappim.taigamobile.strings.generated.resources.markdown_bullet_list
import com.grappim.taigamobile.strings.generated.resources.markdown_code_block
import com.grappim.taigamobile.strings.generated.resources.markdown_heading
import com.grappim.taigamobile.strings.generated.resources.markdown_indent
import com.grappim.taigamobile.strings.generated.resources.markdown_inline_code
import com.grappim.taigamobile.strings.generated.resources.markdown_italic
import com.grappim.taigamobile.strings.generated.resources.markdown_link
import com.grappim.taigamobile.strings.generated.resources.markdown_numbered_list
import com.grappim.taigamobile.strings.generated.resources.markdown_outdent
import com.grappim.taigamobile.strings.generated.resources.markdown_preview
import com.grappim.taigamobile.strings.generated.resources.markdown_quote
import com.grappim.taigamobile.strings.generated.resources.markdown_strikethrough
import com.grappim.taigamobile.strings.generated.resources.markdown_task_list
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Markdown formatting toolbar for a text field holding Markdown source.
 *
 * Each formatting button applies one of the `MarkdownFormatting.kt` transforms to [value] and emits
 * the result through [onValueChange]. The Edit/Preview toggle stays pinned at the start while the
 * formatting buttons scroll horizontally; the formatting buttons are disabled while [isPreview]
 * is true, since there is no editable selection to act on.
 */
@Composable
fun MarkdownToolbar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isPreview: Boolean,
    onPreviewToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(dialogTonalElevation)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviewToggle) {
            Icon(
                imageVector = if (isPreview) Icons.Filled.Edit else Icons.Filled.Visibility,
                contentDescription = stringResource(if (isPreview) RString.edit else RString.markdown_preview)
            )
        }
        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkdownToolbarAction.entries.forEach { action ->
                IconButton(
                    onClick = { onValueChange(action.transform(value)) },
                    enabled = !isPreview
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = stringResource(action.contentDescription)
                    )
                }
            }
        }
    }
}

private enum class MarkdownToolbarAction(
    val icon: ImageVector,
    val contentDescription: StringResource,
    val transform: (TextFieldValue) -> TextFieldValue
) {
    Bold(Icons.Filled.FormatBold, RString.markdown_bold, ::toggleBold),
    Italic(Icons.Filled.FormatItalic, RString.markdown_italic, ::toggleItalic),
    Strikethrough(Icons.Filled.FormatStrikethrough, RString.markdown_strikethrough, ::toggleStrikethrough),
    InlineCode(Icons.Filled.Code, RString.markdown_inline_code, ::toggleInlineCode),
    Heading(Icons.Filled.Title, RString.markdown_heading, ::toggleHeading),
    BulletList(Icons.AutoMirrored.Filled.FormatListBulleted, RString.markdown_bullet_list, ::toggleBulletList),
    NumberedList(Icons.Filled.FormatListNumbered, RString.markdown_numbered_list, ::toggleNumberedList),
    Quote(Icons.Filled.FormatQuote, RString.markdown_quote, ::toggleQuote),
    TaskList(Icons.Filled.CheckBox, RString.markdown_task_list, ::toggleTaskList),
    Indent(Icons.AutoMirrored.Filled.FormatIndentIncrease, RString.markdown_indent, ::indentLines),
    Outdent(Icons.AutoMirrored.Filled.FormatIndentDecrease, RString.markdown_outdent, ::outdentLines),
    Link(Icons.Filled.Link, RString.markdown_link, ::insertLink),
    CodeBlock(Icons.Filled.DataObject, RString.markdown_code_block, ::insertCodeBlock)
}

@PreviewTaigaDarkLight
@Composable
private fun MarkdownToolbarPreview() = TaigaMobilePreviewTheme {
    var value by remember { mutableStateOf(TextFieldValue("hello", TextRange(0, 5))) }
    var isPreview by remember { mutableStateOf(false) }
    MarkdownToolbar(
        value = value,
        onValueChange = { value = it },
        isPreview = isPreview,
        onPreviewToggle = { isPreview = !isPreview }
    )
}
