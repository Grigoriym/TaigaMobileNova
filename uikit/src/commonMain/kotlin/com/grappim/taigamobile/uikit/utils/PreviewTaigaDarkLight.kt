package com.grappim.taigamobile.uikit.utils

import androidx.compose.ui.tooling.preview.AndroidUiModes
import androidx.compose.ui.tooling.preview.Preview
import com.grappim.taigamobile.uikit.theme.DarkBackgroundColorForPreview

@Preview(
    showBackground = true,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_NO,
    name = "Light theme"
)
@Preview(
    showBackground = true,
    backgroundColor = DarkBackgroundColorForPreview,
    uiMode = AndroidUiModes.UI_MODE_NIGHT_YES,
    name = "Dark theme"
)
annotation class PreviewTaigaDarkLight
