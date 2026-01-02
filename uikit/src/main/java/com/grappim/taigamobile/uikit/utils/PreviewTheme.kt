package com.grappim.taigamobile.uikit.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.grappim.taigamobile.uikit.FilePicker
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalFilePicker provides object : FilePicker() {}
    ) {
        TaigaMobileTheme {
            content()
        }
    }
}
