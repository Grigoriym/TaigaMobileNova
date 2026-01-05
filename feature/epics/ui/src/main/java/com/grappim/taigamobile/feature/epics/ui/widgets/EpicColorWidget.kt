package com.grappim.taigamobile.feature.epics.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.utils.ui.toColor
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.color.ARGBPickerState
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Composable
fun EpicColorWidget(
    isEpicColorLoading: Boolean,
    epicColor: String?,
    modifier: Modifier = Modifier,
    canModify: Boolean = true,
    onColorPick: (Color) -> Unit = {}
) {
    val colorToUse = epicColor.orEmpty().toColor()
    val dialogState = rememberMaterialDialogState()

    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            positiveButton(res = RString.ok)
            negativeButton(res = RString.cancel)
        }
    ) {
        title(stringResource(RString.select_color))

        colorChooser(
            colors = (listOf(colorToUse) + ColorPalette.Primary).toSet().toList(),
            onColorSelected = onColorPick,
            argbPickerState = ARGBPickerState.WithoutAlphaSelector
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
                .background(color = colorToUse, shape = MaterialTheme.shapes.small)
                .then(
                    if (canModify) {
                        Modifier.clickable { dialogState.show() }
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
