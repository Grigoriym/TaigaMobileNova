package com.grappim.taigamobile.uikit.widgets.button

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable

@Composable
fun TaigaTextButtonWidget(
    text: String,
    isOffline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Int? = null
) {
    FilledTonalButton(modifier = modifier, onClick = onClick, enabled = !isOffline) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TaigaTextButtonWidgetPreview() = TaigaMobilePreviewTheme {
    TaigaTextButtonWidget(
        text = "Button",
        isOffline = false,
        onClick = {}
    )
}

@PreviewTaigaDarkLight
@Composable
private fun TaigaTextButtonWidgetWithIconPreview() = TaigaMobilePreviewTheme {
    TaigaTextButtonWidget(
        text = "Add item",
        isOffline = false,
        onClick = {},
        icon = RDrawable.ic_add
    )
}

@PreviewTaigaDarkLight
@Composable
private fun TaigaTextButtonWidgetOfflinePreview() = TaigaMobilePreviewTheme {
    TaigaTextButtonWidget(
        text = "Button",
        isOffline = true,
        onClick = {}
    )
}
