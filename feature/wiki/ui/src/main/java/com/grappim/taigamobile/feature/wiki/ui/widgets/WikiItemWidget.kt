package com.grappim.taigamobile.feature.wiki.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight

@Composable
fun WikiItemWidget(
    title: String,
    onClick: () -> Unit,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    canDeleteItem: Boolean = false,
    onDeleteItemClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier.clickable {
            onClick()
        },
        headlineContent = {
            Text(
                text = title
            )
        },
        trailingContent = {
            if (onDeleteItemClick != null && canDeleteItem) {
                IconButton(
                    enabled = !isOffline,
                    onClick = onDeleteItemClick
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete item")
                }
            }
        }
    )
}

@PreviewTaigaDarkLight
@Composable
private fun WikiItemWidgetPreview() {
    TaigaMobileTheme {
        WikiItemWidget(
            title = "Getting Started Guide",
            onClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WikiItemWidgetWithDeletePreview() {
    TaigaMobileTheme {
        WikiItemWidget(
            title = "API Documentation",
            onClick = {},
            isOffline = false,
            canDeleteItem = true,
            onDeleteItemClick = {}
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WikiItemWidgetOfflinePreview() {
    TaigaMobileTheme {
        WikiItemWidget(
            title = "Offline Wiki Page",
            onClick = {},
            isOffline = true,
            canDeleteItem = true,
            onDeleteItemClick = {}
        )
    }
}
