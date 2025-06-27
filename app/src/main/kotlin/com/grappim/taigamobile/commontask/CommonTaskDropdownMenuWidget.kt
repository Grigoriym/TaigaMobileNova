package com.grappim.taigamobile.commontask

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
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal

@Composable
fun CommonTaskDropdownMenuWidget(
    state: CommonTaskState,
    url: String,
    showMessage: (message: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboard.current
    Box(modifier = modifier) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevationInternal(
                    dialogTonalElevation
                )
            ),
            expanded = state.isDropdownMenuExpanded,
            onDismissRequest = { state.setDropdownMenuExpanded(false) }
        ) {
            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    val clip = ClipData.newPlainText(
                        "Copy Link",
                        AnnotatedString(url)
                    )
                    clipboardManager.nativeClipboard.setPrimaryClip(clip)
                    showMessage(RString.copy_link_successfully)
                },
                text = {
                    Text(
                        text = stringResource(RString.copy_link),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )

            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    state.setTaskEditorVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(RString.edit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )

            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    state.setDeleteAlertVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(RString.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )

            if (state.commonTaskType == CommonTaskType.Task ||
                state.commonTaskType == CommonTaskType.Issue
            ) {
                DropdownMenuItem(
                    onClick = {
                        state.setDropdownMenuExpanded(false)
                        state.setPromoteAlertVisible(true)
                    },
                    text = {
                        Text(
                            text = stringResource(RString.promote_to_user_story),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                )
            }

            DropdownMenuItem(
                onClick = {
                    state.setDropdownMenuExpanded(false)
                    if (state.isBlocked) {
                        state.editActions.editBlocked.remove(Unit)
                    } else {
                        state.setBlockDialogVisible(true)
                    }
                },
                text = {
                    Text(
                        text = stringResource(
                            if (state.isBlocked) RString.unblock else RString.block
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
