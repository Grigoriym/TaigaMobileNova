package com.grappim.taigamobile.uikit.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import com.grappim.taigamobile.uikit.theme.DarkBackgroundColorForPreview

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Light theme"
)
@Preview(
    showBackground = true,
    backgroundColor = DarkBackgroundColorForPreview,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark theme"
)
annotation class ThemePreviews
