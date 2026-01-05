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

@Composable
fun WikiItemWidget(
    title: String,
    onClick: () -> Unit,
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
                    onClick = onDeleteItemClick
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete item")
                }
            }
        }
    )
}
