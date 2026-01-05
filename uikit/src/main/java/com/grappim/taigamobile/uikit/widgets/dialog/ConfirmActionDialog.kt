package com.grappim.taigamobile.uikit.widgets.dialog

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString

/**
 * Standard confirmation alert with "yes" "no" buttons, title and text
 */
@Composable
fun ConfirmActionDialog(
    isVisible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String? = null,
    description: String? = null,
    confirmButtonText: NativeText = NativeText.Resource(RString.yes),
    dismissButtonText: NativeText = NativeText.Resource(RString.no),
    @DrawableRes iconId: Int? = null
) {
    if (isVisible) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text(
                        text = confirmButtonText.asString(context),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = dismissButtonText.asString(context),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            title = if (title != null) {
                {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                null
            },
            text = if (description != null) {
                { Text(description) }
            } else {
                null
            },
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
    }
}
