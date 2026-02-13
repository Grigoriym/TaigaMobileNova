package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
fun TaigaHeightSpacer(height: Dp) {
    Spacer(Modifier.height(height))
}

@Composable
fun TaigaWidthSpacer(width: Dp) {
    Spacer(Modifier.width(width))
}
