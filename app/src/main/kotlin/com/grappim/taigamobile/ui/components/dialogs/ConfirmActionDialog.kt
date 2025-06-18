package com.grappim.taigamobile.ui.components.dialogs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.R

/**
 * Standard confirmation alert with "yes" "no" buttons, title and text
 */
@Composable
fun ConfirmActionDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    @DrawableRes iconId: Int? = null
) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(
                text = stringResource(R.string.yes),
                style = MaterialTheme.typography.titleMedium
            )
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = stringResource(R.string.no),
                style = MaterialTheme.typography.titleMedium
            )
        }
    },
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
    },
    text = { Text(text) },
    icon = iconId?.let {
        {
            Icon(
                modifier = Modifier.size(26.dp),
                painter = painterResource(it),
                contentDescription = null
            )
        }
    }
)
