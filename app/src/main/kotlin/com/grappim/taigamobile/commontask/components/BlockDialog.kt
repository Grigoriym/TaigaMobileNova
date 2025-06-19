package com.grappim.taigamobile.commontask.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint

@Composable
fun BlockDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var reason by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue()
        )
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(RString.cancel),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason.text) }) {
                Text(
                    text = stringResource(RString.ok),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        title = {
            Text(
                text = stringResource(RString.block),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            TextFieldWithHint(
                hintId = RString.block_reason,
                value = reason,
                onValueChange = { reason = it },
                minHeight = with(LocalDensity.current) {
                    MaterialTheme.typography.bodyLarge.fontSize.toDp() *
                        4
                },
                contentAlignment = Alignment.TopStart
            )
        }
    )
}
