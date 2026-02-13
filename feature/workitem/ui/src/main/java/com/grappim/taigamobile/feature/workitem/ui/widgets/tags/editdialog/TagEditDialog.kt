package com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.utils.ui.toHex
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TagEditDialog(state: TagEditDialogState, onSaveClick: (name: String, color: Color) -> Unit) {
    if (state.isVisible) {
        Dialog(onDismissRequest = state.onDismiss) {
            TagEditDialogContent(state = state, onSaveClick = onSaveClick)
        }
    }
}

@Composable
private fun TagEditDialogContent(state: TagEditDialogState, onSaveClick: (name: String, color: Color) -> Unit) {
    var name by remember { mutableStateOf(state.tagUI?.name ?: "") }
    val initialColor = (state.tagUI?.color ?: state.defaultColor).asColor()
    var selectedColor by remember { mutableStateOf(initialColor) }
    var isCustomColorExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = state.dialogTitle.asString(LocalContext.current),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TaigaHeightSpacer(16.dp)

            if (state.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage.asString(LocalContext.current),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                TaigaHeightSpacer(16.dp)
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(RString.tag_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            TaigaHeightSpacer(16.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(RString.selected_color),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedColor.toHex(),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace
                )
            }

            TaigaHeightSpacer(16.dp)

            Text(
                text = stringResource(RString.presets),
                style = MaterialTheme.typography.labelMedium
            )
            TaigaHeightSpacer(8.dp)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.presetColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 3.dp else 1.dp,
                                color = if (selectedColor == color) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                selectedColor = color
                            }
                    )
                }
            }

            TaigaHeightSpacer(12.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isCustomColorExpanded = !isCustomColorExpanded }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isCustomColorExpanded) {
                        Icons.Default.ExpandLess
                    } else {
                        Icons.Default.ExpandMore
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Custom color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (!state.presetColors.contains(selectedColor)) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(RString.tag_color_in_use),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            if (isCustomColorExpanded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val controller = rememberColorPickerController()
                        HsvColorPicker(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            controller = controller,
                            onColorChanged = { colorEnvelope ->
                                selectedColor = colorEnvelope.color
                            }
                        )
                    }
                }
            }

            TaigaHeightSpacer(24.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = state.onDismiss,
                    enabled = !state.isLoading
                ) {
                    Text(stringResource(RString.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onSaveClick(name, selectedColor)
                    },
                    enabled = name.isNotBlank() && !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(RString.save))
                }
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TagEditDialogAddPreview() {
    TaigaMobileTheme {
        Surface {
            TagEditDialogContent(
                state = TagEditDialogState(
                    isVisible = true,
                    presetColors = persistentListOf(
                        Color(0xFFE57373),
                        Color(0xFF81C784),
                        Color(0xFF64B5F6),
                        Color(0xFFFFD54F),
                        Color(0xFFBA68C8),
                        Color(0xFF4DB6AC),
                        Color(0xFFFF8A65),
                        Color(0xFF90A4AE)
                    )
                ),
                onSaveClick = { _, _ -> }
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TagEditDialogEditPreview() {
    TaigaMobileTheme {
        Surface {
            TagEditDialogContent(
                state = TagEditDialogState(
                    isVisible = true,
                    tagUI = TagUI(
                        name = "tag to edit",
                        color = StaticColor(Color(0xFFE57345))
                    ),
                    presetColors = persistentListOf(
                        Color(0xFFE57373),
                        Color(0xFF81C784),
                        Color(0xFF64B5F6),
                        Color(0xFFFFD54F),
                        Color(0xFFBA68C8),
                        Color(0xFF4DB6AC),
                        Color(0xFFFF8A65),
                        Color(0xFF90A4AE)
                    )
                ),
                onSaveClick = { _, _ -> }
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TagEditDialogLoadingPreview() {
    TaigaMobileTheme {
        Surface {
            TagEditDialogContent(
                state = TagEditDialogState(
                    isVisible = true,
                    tagUI = TagUI(
                        name = "saving tag",
                        color = StaticColor(Color(0xFFE57345))
                    ),
                    presetColors = persistentListOf(
                        Color(0xFFE57373),
                        Color(0xFF81C784),
                        Color(0xFF64B5F6),
                        Color(0xFFFFD54F),
                        Color(0xFFBA68C8),
                        Color(0xFF4DB6AC),
                        Color(0xFFFF8A65),
                        Color(0xFF90A4AE)
                    ),
                    isLoading = true
                ),
                onSaveClick = { _, _ -> }
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TagEditDialogErrorPreview() {
    TaigaMobileTheme {
        Surface {
            TagEditDialogContent(
                state = TagEditDialogState(
                    isVisible = true,
                    tagUI = TagUI(
                        name = "my tag",
                        color = StaticColor(Color(0xFFE57345))
                    ),
                    presetColors = persistentListOf(
                        Color(0xFFE57373),
                        Color(0xFF81C784),
                        Color(0xFF64B5F6),
                        Color(0xFFFFD54F),
                        Color(0xFFBA68C8),
                        Color(0xFF4DB6AC),
                        Color(0xFFFF8A65),
                        Color(0xFF90A4AE)
                    ),
                    errorMessage = NativeText.Simple("Failed to save tag. Please try again.")
                ),
                onSaveClick = { _, _ -> }
            )
        }
    }
}
