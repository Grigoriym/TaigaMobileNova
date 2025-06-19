package com.grappim.taigamobile.uikit.widgets.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.clickableUnindicated
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.color.ARGBPickerState
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

/**
 * Color picker with material dialog
 */
@Composable
fun ColorPicker(size: Dp, color: Color, onColorPick: (Color) -> Unit = {}) {
    val dialogState = rememberMaterialDialogState()

    MaterialDialog(
        dialogState = dialogState,
        buttons = {
            // TODO update buttons to comply with material3 color schema?
            positiveButton(res = RString.ok)
            negativeButton(res = RString.cancel)
        }
    ) {
        title(stringResource(RString.select_color))

        colorChooser(
            colors = (listOf(color) + ColorPalette.Primary).toSet().toList(),
            onColorSelected = onColorPick,
            argbPickerState = ARGBPickerState.WithoutAlphaSelector
        )
    }

    Spacer(
        Modifier
            .size(size)
            .background(color = color, shape = MaterialTheme.shapes.small)
            .clickableUnindicated { dialogState.show() }
    )
}
