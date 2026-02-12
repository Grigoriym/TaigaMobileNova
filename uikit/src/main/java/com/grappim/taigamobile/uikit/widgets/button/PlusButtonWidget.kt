package com.grappim.taigamobile.uikit.widgets.button

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable

@Composable
fun PlusButtonWidget(
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {}
) {
    IconButton(
        enabled = !isOffline,
        onClick = onClick,
        modifier = modifier
            .padding(top = 2.dp)
            .size(32.dp)
            .clip(CircleShape)
    ) {
        Icon(
            painter = painterResource(RDrawable.ic_add),
            contentDescription = "Add",
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun PlusButtonWidgetPreview() {
    TaigaMobilePreviewTheme {
        PlusButtonWidget(isOffline = false)
    }
}
