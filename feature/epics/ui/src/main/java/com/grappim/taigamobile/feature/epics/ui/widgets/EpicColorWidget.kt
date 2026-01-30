package com.grappim.taigamobile.feature.epics.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.utils.ui.toColor
import com.grappim.taigamobile.utils.ui.toHex

@Composable
fun EpicColorWidget(
    isEpicColorLoading: Boolean,
    epicColor: String?,
    modifier: Modifier = Modifier,
    canModify: Boolean = true,
    onColorPick: (Color) -> Unit = {}
) {
    val colorToUse = epicColor.orEmpty().toColor()
    var isDialogVisible by remember { mutableStateOf(false) }

    if (isDialogVisible) {
        EpicColorPickerDialog(
            initialColor = colorToUse,
            onDismiss = { isDialogVisible = false },
            onColorSelect = { color ->
                onColorPick(color)
                isDialogVisible = false
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = stringResource(RString.epic_color))
        Spacer(modifier = Modifier.width(16.dp))
        Spacer(
            Modifier
                .size(32.dp)
                .clip(MaterialTheme.shapes.small)
                .background(color = colorToUse)
                .then(
                    if (canModify) {
                        Modifier.clickable { isDialogVisible = true }
                    } else {
                        Modifier
                    }
                )
        )

        if (isEpicColorLoading) {
            Spacer(modifier = Modifier.width(8.dp))
            CircularLoaderWidget(modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun EpicColorPickerDialog(initialColor: Color, onDismiss: () -> Unit, onColorSelect: (Color) -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        EpicColorPickerDialogContent(
            initialColor = initialColor,
            onDismiss = onDismiss,
            onColorSelect = onColorSelect
        )
    }
}

@Composable
private fun EpicColorPickerDialogContent(initialColor: Color, onDismiss: () -> Unit, onColorSelect: (Color) -> Unit) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

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
                text = stringResource(RString.select_color),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                controller = controller,
                onColorChanged = { colorEnvelope ->
                    selectedColor = colorEnvelope.color
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(RString.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { onColorSelect(selectedColor) }) {
                    Text(stringResource(RString.ok))
                }
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun EpicColorWidgetPreview() {
    TaigaMobileTheme {
        Surface {
            EpicColorWidget(
                isEpicColorLoading = false,
                epicColor = "#E57373"
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun EpicColorWidgetLoadingPreview() {
    TaigaMobileTheme {
        Surface {
            EpicColorWidget(
                isEpicColorLoading = true,
                epicColor = "#81C784"
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun EpicColorWidgetNotModifiablePreview() {
    TaigaMobileTheme {
        Surface {
            EpicColorWidget(
                isEpicColorLoading = false,
                epicColor = "#64B5F6",
                canModify = false
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun EpicColorPickerDialogPreview() {
    TaigaMobileTheme {
        Surface {
            EpicColorPickerDialogContent(
                initialColor = Color(0xFFE57373),
                onDismiss = {},
                onColorSelect = {}
            )
        }
    }
}
