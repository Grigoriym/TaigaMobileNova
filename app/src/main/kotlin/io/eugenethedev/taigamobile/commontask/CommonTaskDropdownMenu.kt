package io.eugenethedev.taigamobile.commontask

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
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.ui.theme.dialogTonalElevation
import io.eugenethedev.taigamobile.ui.utils.surfaceColorAtElevationInternal

@Composable
fun CommonTaskDropdownMenu(
    modifier: Modifier = Modifier,
    state: CommonTaskState,
    url: String,
    showMessage: (message: Int) -> Unit
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
                    showMessage(R.string.copy_link_successfully)
                },
                text = {
                    Text(
                        text = stringResource(R.string.copy_link),
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
                        text = stringResource(R.string.edit),
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
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )

            if (state.commonTaskType == CommonTaskType.Task || state.commonTaskType == CommonTaskType.Issue) {
                DropdownMenuItem(
                    onClick = {
                        state.setDropdownMenuExpanded(false)
                        state.setPromoteAlertVisible(true)
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.promote_to_user_story),
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
                        text = stringResource(if (state.isBlocked) R.string.unblock else R.string.block),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
