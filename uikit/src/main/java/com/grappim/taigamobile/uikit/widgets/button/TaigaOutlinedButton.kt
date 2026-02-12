package com.grappim.taigamobile.uikit.widgets.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.icon.TaigaIcon

@Composable
fun TaigaOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, painter: Painter? = null) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = { onClick() }
    ) {
        if (painter != null) {
            TaigaIcon(
                modifier = Modifier.size(26.dp),
                painter = painter
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
fun TaigaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageVector: ImageVector? = null
) {
    OutlinedButton(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = { onClick() }
    ) {
        if (imageVector != null) {
            TaigaIcon(
                modifier = Modifier.size(26.dp),
                imageVector = imageVector
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@[Composable PreviewTaigaDarkLight]
private fun PlatoOutlinedButtonImageVector() {
    TaigaMobilePreviewTheme {
        TaigaOutlinedButton(
            text = "Login",
            onClick = {},
            imageVector = Icons.Default.Clear
        )
    }
}
