package com.grappim.taigamobile.commontask.components

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import com.grappim.taigamobile.commontask.EditActions
import com.grappim.taigamobile.commontask.NavigationActions
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.AppBarWithBackButton
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal

@Composable
fun CommonTaskAppBar(
    toolbarTitle: String,
    toolbarSubtitle: String,
    commonTaskType: CommonTaskType,
    isBlocked: Boolean,
    editActions: EditActions,
    navigationActions: NavigationActions,
    url: String,
    showTaskEditor: () -> Unit,
    showMessage: (message: Int) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboard.current
    AppBarWithBackButton(
        title = {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = toolbarTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = toolbarSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        painter = painterResource(RDrawable.ic_options),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // delete alert dialog
                var isDeleteAlertVisible by remember { mutableStateOf(false) }
                if (isDeleteAlertVisible) {
                    ConfirmActionDialog(
                        title = stringResource(RString.delete_task_title),
                        text = stringResource(RString.delete_task_text),
                        onConfirm = {
                            isDeleteAlertVisible = false
                            editActions.deleteTask.select(Unit)
                        },
                        onDismiss = { isDeleteAlertVisible = false },
                        iconId = RDrawable.ic_delete
                    )
                }

                // promote alert dialog
                var isPromoteAlertVisible by remember { mutableStateOf(false) }
                if (isPromoteAlertVisible) {
                    ConfirmActionDialog(
                        title = stringResource(RString.promote_title),
                        text = stringResource(RString.promote_text),
                        onConfirm = {
                            isPromoteAlertVisible = false
                            editActions.promoteTask.select(Unit)
                        },
                        onDismiss = { isPromoteAlertVisible = false },
                        iconId = RDrawable.ic_arrow_upward
                    )
                }

                // block item dialog
                var isBlockDialogVisible by remember { mutableStateOf(false) }
                if (isBlockDialogVisible) {
                    BlockDialog(
                        onConfirm = {
                            editActions.editBlocked.select(it)
                            isBlockDialogVisible = false
                        },
                        onDismiss = { isBlockDialogVisible = false }
                    )
                }

                DropdownMenu(
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.surfaceColorAtElevationInternal(
                            dialogTonalElevation
                        )
                    ),
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    // Copy link
                    DropdownMenuItem(
                        onClick = {
                            isMenuExpanded = false
                            val clip = ClipData.newPlainText("Copy Link", AnnotatedString(url))
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

                    // edit
                    DropdownMenuItem(
                        onClick = {
                            isMenuExpanded = false
                            showTaskEditor()
                        },
                        text = {
                            Text(
                                text = stringResource(RString.edit),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )

                    // delete
                    DropdownMenuItem(
                        onClick = {
                            isMenuExpanded = false
                            isDeleteAlertVisible = true
                        },
                        text = {
                            Text(
                                text = stringResource(RString.delete),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )

                    // promote
                    if (commonTaskType == CommonTaskType.Task || commonTaskType == CommonTaskType.Issue) {
                        DropdownMenuItem(
                            onClick = {
                                isMenuExpanded = false
                                isPromoteAlertVisible = true
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
                            isMenuExpanded = false
                            if (isBlocked) {
                                editActions.editBlocked.remove(Unit)
                            } else {
                                isBlockDialogVisible = true
                            }
                        },
                        text = {
                            Text(
                                text = stringResource(if (isBlocked) RString.unblock else RString.block),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    )
                }
            }
        },
        navigateBack = navigationActions.navigateBack
    )
}

@Composable
fun BlockDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue()
        )
    }

    AlertDialog(
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
                minHeight = with(LocalDensity.current) { MaterialTheme.typography.bodyLarge.fontSize.toDp() * 4 },
                contentAlignment = Alignment.TopStart
            )
        }
    )
}