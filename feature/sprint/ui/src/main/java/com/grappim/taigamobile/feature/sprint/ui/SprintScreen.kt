package com.grappim.taigamobile.feature.sprint.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.EditSprintDialog
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.dialog.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal
import kotlinx.collections.immutable.persistentListOf

@Composable
fun SprintScreen(
    showSnackbar: (NativeText) -> Unit,
    goBack: () -> Unit,
    goToTaskScreen: (Long, CommonTaskType, Long) -> Unit,
    goToCreateTask: (CommonTaskType, Long?, Long) -> Unit,
    viewModel: SprintViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sprintDialogState by viewModel.sprintDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(state.sprintToolbarTitle, state.sprintToolbarSubtitle) {
        topBarController.update(
            TopBarConfig(
                title = state.sprintToolbarTitle,
                subtitle = state.sprintToolbarSubtitle,
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = {
                        goBack()
                    }
                ),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "",
                        onClick = {
                            state.setIsMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    BackHandler {
        goBack()
    }

    ObserveAsEvents(viewModel.deleteResult) {
        goBack()
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    SprintDropdownMenuWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state
    )

    if (state.isDeleteDialogVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.delete_sprint_title),
            description = stringResource(RString.delete_sprint_text),
            onConfirm = {
                state.setIsDeleteDialogVisible(false)
                state.onDeleteSprint()
            },
            onDismiss = { state.setIsDeleteDialogVisible(false) }
        )
    }

    EditSprintDialog(
        state = sprintDialogState,
        onConfirm = state.onEditSprintConfirm
    )

    TaigaLoadingDialog(state.isLoading)

    if (state.sprint != null) {
        SprintScreenContent(
            state = state,
            navigateToTask = { id, type, ref ->
                goToTaskScreen(id, type, ref)
            },
            navigateToCreateTask = { type, parentId ->
                goToCreateTask(
                    type,
                    parentId,
                    state.sprint!!.id
                )
            }
        )
    }
}

@Composable
fun SprintScreenContent(
    state: SprintState,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier,
    navigateToCreateTask: (type: CommonTaskType, parentId: Long?) -> Unit = { _, _ -> }
) {
    requireNotNull(state.sprint)
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            SprintKanbanWidget(
                statuses = state.statuses,
                storiesWithTasks = state.storiesWithTasks,
                storylessTasks = state.storylessTasks,
                issues = state.issues,
                navigateToTask = navigateToTask,
                navigateToCreateTask = navigateToCreateTask
            )
        }
    }
}

@Composable
private fun SprintDropdownMenuWidget(state: SprintState, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevationInternal(
                    dialogTonalElevation
                )
            ),
            expanded = state.isMenuExpanded,
            onDismissRequest = { state.setIsMenuExpanded(false) }
        ) {
            DropdownMenuItem(
                onClick = {
                    state.setIsMenuExpanded(false)
                    state.onEditSprintClick()
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
                    state.setIsMenuExpanded(false)
                    state.setIsDeleteDialogVisible(true)
                },
                text = {
                    Text(
                        text = stringResource(RString.delete),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SprintScreenPreview() = TaigaMobileTheme {
    SprintScreenContent(
        state = SprintState(),
        navigateToTask = { _, _, _ -> }
    )
}
