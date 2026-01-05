package com.grappim.taigamobile.feature.workitem.ui.widgets

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal

@Composable
fun WorkItemDropdownMenuWidget(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    showSnackbar: (NativeText) -> Unit,
    url: String,
    isBlocked: Boolean,
    setBlockDialogVisible: (Boolean) -> Unit,
    doOnUnblock: () -> Unit,
    modifier: Modifier = Modifier,
    setDeleteAlertVisible: ((Boolean) -> Unit)? = null,
    onPromoteClick: (() -> Unit)? = null,
    canDelete: Boolean = false,
    canModify: Boolean = false
) {
    val clipboardManager = LocalClipboard.current
    Box(modifier = modifier) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevationInternal(
                    dialogTonalElevation
                )
            ),
            expanded = isExpanded,
            onDismissRequest = onDismissRequest
        ) {
            DropdownMenuItem(
                onClick = {
                    onDismissRequest()
                    val clip = ClipData.newPlainText(
                        "Copy Link",
                        AnnotatedString(url)
                    )
                    clipboardManager.nativeClipboard.setPrimaryClip(clip)
                    showSnackbar(NativeText.Resource(RString.copy_link_successfully))
                },
                text = {
                    Text(
                        text = stringResource(RString.copy_link),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )

            if (setDeleteAlertVisible != null && canDelete) {
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        setDeleteAlertVisible(true)
                    },
                    text = {
                        Text(
                            text = stringResource(RString.delete),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }

            if (onPromoteClick != null && canModify) {
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        onPromoteClick()
                    },
                    text = {
                        Text(
                            text = stringResource(
                                RString.promote_to_user_story
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }

            if (canModify) {
                DropdownMenuItem(
                    onClick = {
                        onDismissRequest()
                        if (isBlocked) {
                            doOnUnblock()
                        } else {
                            setBlockDialogVisible(true)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(
                                if (isBlocked) RString.unblock else RString.block
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }
        }
    }
}
