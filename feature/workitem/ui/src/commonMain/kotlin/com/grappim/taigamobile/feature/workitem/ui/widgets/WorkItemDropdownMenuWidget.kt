package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.block
import com.grappim.taigamobile.strings.generated.resources.copy_link
import com.grappim.taigamobile.strings.generated.resources.copy_link_successfully
import com.grappim.taigamobile.strings.generated.resources.delete
import com.grappim.taigamobile.strings.generated.resources.promote_to_user_story
import com.grappim.taigamobile.strings.generated.resources.unblock
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.toClipEntry
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WorkItemDropdownMenuWidget(
    isExpanded: Boolean,
    isOffline: Boolean,
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
    val scope = rememberCoroutineScope()

    Box(modifier = modifier) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    dialogTonalElevation
                )
            ),
            expanded = isExpanded,
            onDismissRequest = onDismissRequest
        ) {
            DropdownMenuItem(
                onClick = {
                    scope.launch {
                        onDismissRequest()
                        clipboardManager.setClipEntry(url.toClipEntry())
                        showSnackbar(NativeText.Resource(RString.copy_link_successfully))
                    }
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
                    enabled = !isOffline,
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
                    enabled = !isOffline,
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
                    enabled = !isOffline,
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
