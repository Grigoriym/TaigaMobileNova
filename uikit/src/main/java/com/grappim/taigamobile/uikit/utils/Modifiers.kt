package com.grappim.taigamobile.uikit.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Deprecated("check if we really need it")
fun Modifier.clickableUnindicated(enabled: Boolean = true, onClick: () -> Unit) = composed {
    Modifier.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClickLabel = null,
        role = null,
        onClick
    )
}
