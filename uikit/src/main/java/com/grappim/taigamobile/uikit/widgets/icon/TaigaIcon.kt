package com.grappim.taigamobile.uikit.widgets.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight

@Composable
fun TaigaIcon(
    painter: Painter,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    tint: Color = LocalContentColor.current
) {
    Icon(
        modifier = modifier
            .testTag(painter.toString()),
        painter = painter,
        contentDescription = contentDescription,
        tint = tint
    )
}

@Composable
fun TaigaIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String = "",
    tint: Color = LocalContentColor.current
) {
    Icon(
        modifier = modifier.testTag(imageVector.name),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint
    )
}

@[Composable PreviewTaigaDarkLight]
private fun PlatoIconPreview() {
    TaigaMobileTheme {
        TaigaIcon(
            imageVector = Icons.Filled.Menu
        )
    }
}
