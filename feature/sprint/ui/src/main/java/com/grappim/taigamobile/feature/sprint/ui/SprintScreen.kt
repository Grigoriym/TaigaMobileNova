package com.grappim.taigamobile.feature.sprint.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.dialog.EditSprintDialog
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import com.grappim.taigamobile.utils.ui.SuccessResult
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

@Composable
fun SprintScreen(
    showMessage: (message: Int) -> Unit,
    goBack: () -> Unit,
    goToTaskScreen: (Long, CommonTaskType, Long) -> Unit,
    goToCreateTask: (CommonTaskType, Long?, Long) -> Unit,
    viewModel: SprintViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    val editResult by viewModel.editResult.collectAsState()
    editResult.SubscribeOnError(showMessage)

    val deleteResult by viewModel.deleteResult.collectAsState()
    deleteResult.SubscribeOnError(showMessage)
    deleteResult.takeIf { it is SuccessResult }?.let {
        LaunchedEffect(Unit) {
            goBack()
        }
    }

    LaunchedEffect(state.sprintToolbarTitle, state.sprintToolbarSubtitle) {
        topBarController.update(
            TopBarConfig(
                title = state.sprintToolbarTitle,
                subtitle = state.sprintToolbarSubtitle,
                navigationIcon = NavigationIconConfig.Back(),
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

    if (state.isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularLoaderWidget()
        }
    }

    if (state.sprint != null) {
        SprintScreenContent(
            state = state,
            isEditLoading = editResult is LoadingResult,
            isDeleteLoading = deleteResult is LoadingResult,
            editSprint = viewModel::editSprint,
            deleteSprint = viewModel::deleteSprint,
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
    isEditLoading: Boolean = false,
    isDeleteLoading: Boolean = false,
    editSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> },
    deleteSprint: () -> Unit = {},
    navigateToCreateTask: (type: CommonTaskType, parentId: Long?) -> Unit = { _, _ -> }
) {
    requireNotNull(state.sprint)
    Box(modifier = modifier.fillMaxSize()) {
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
                    deleteSprint()
                },
                onDismiss = { state.setIsDeleteDialogVisible(false) },
                iconId = RDrawable.ic_delete
            )
        }

        if (state.isEditDialogVisible) {
            EditSprintDialog(
                initialName = state.sprint.name,
                initialStart = state.sprint.start,
                initialEnd = state.sprint.end,
                onConfirm = { name, start, end ->
                    editSprint(name, start, end)
                    state.setIsEditDialogVisible(false)
                },
                onDismiss = { state.setIsEditDialogVisible(false) }
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            when {
                isEditLoading || isDeleteLoading -> LoadingDialog()

                else -> SprintKanbanWidget(
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
                    state.setIsEditDialogVisible(true)
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
