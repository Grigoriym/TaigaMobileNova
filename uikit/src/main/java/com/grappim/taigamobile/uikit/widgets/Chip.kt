package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.utils.ui.textColor

/**
 * Material chip component (rounded rectangle)
 */
@Composable
fun Chip(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.outline,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize.provides(Dp.Unspecified)
    ) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(50),
            color = color,
            contentColor = color.textColor(),
            shadowElevation = 1.dp
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (onClick != null) {
                            Modifier.clickable(
                                indication = ripple(),
                                onClick = onClick,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                        } else {
                            Modifier
                        }
                    )
                    .padding(vertical = 4.dp, horizontal = 10.dp)
            ) {
                content()
            }
        }
    }
}

@Preview
@Composable
private fun ChipPreview() = TaigaMobilePreviewTheme {
    Box(modifier = Modifier.padding(10.dp)) {
        Chip {
            Text("Testing chip")
        }
    }
}
