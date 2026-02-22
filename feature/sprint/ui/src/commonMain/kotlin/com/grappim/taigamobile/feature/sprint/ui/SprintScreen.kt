@file:OptIn(ExperimentalComposeUiApi::class)

package com.grappim.taigamobile.feature.sprint.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.delete
import com.grappim.taigamobile.strings.generated.resources.delete_sprint_text
import com.grappim.taigamobile.strings.generated.resources.delete_sprint_title
import com.grappim.taigamobile.strings.generated.resources.edit
import com.grappim.taigamobile.uikit.generated.resources.ic_options
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.PreviewUtils
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SprintScreen(
    showSnackbar: (NativeText) -> Unit,
    goBack: () -> Unit,
    goToTaskScreen: (Long, CommonTaskType, Long) -> Unit,
    goToCreateTask: (CommonTaskType, Long?, Long) -> Unit,
    updateData: Boolean = false,
    viewModel: SprintViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sprintDialogState by viewModel.sprintDialogState.collectAsStateWithLifecycle()
    val isOffline = LocalOfflineState.current

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
                actions = buildList {
                    if (state.canShowTopBarActions) {
                        add(
                            TopBarActionIconButton(
                                drawable = RDrawable.ic_options,
                                contentDescription = "",
                                onClick = {
                                    state.setIsMenuExpanded(true)
                                }
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    ObserveAsEvents(viewModel.snackBarMessage) { message ->
        if (message.isNotEmpty() && state.sprint != null) {
            showSnackbar(message)
        }
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            state.onRefresh()
        }
    }

    BackHandler {
        goBack()
    }

    ObserveAsEvents(viewModel.deleteResult) {
        goBack()
    }

    SprintDropdownMenuWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state,
        isOffline = isOffline
    )

    ConfirmActionDialog(
        title = stringResource(RString.delete_sprint_title),
        description = stringResource(RString.delete_sprint_text),
        onConfirm = {
            state.setIsDeleteDialogVisible(false)
            state.onDeleteSprint()
        },
        onDismiss = { state.setIsDeleteDialogVisible(false) },
        isVisible = state.isDeleteDialogVisible
    )

    _root_ide_package_.com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.EditSprintDialog(
        state = sprintDialogState,
        onConfirm = state.onEditSprintConfirm
    )

    SprintScreenContent(
        isOffline = isOffline,
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

@Composable
fun SprintScreenContent(
    isOffline: Boolean,
    state: SprintState,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier,
    navigateToCreateTask: (type: CommonTaskType, parentId: Long?) -> Unit = { _, _ -> }
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = state.isLoading,
        onRefresh = state.onRefresh
    ) {
        if (state.error.isNotEmpty() && state.sprint == null) {
            ErrorStateWidget(
                message = state.error,
                onRetry = state.onRefresh
            )
        } else {
            SprintKanbanWidget(
                isOffline = isOffline,
                state = state,
                navigateToTask = navigateToTask,
                navigateToCreateTask = navigateToCreateTask
            )
        }
    }
}

@Composable
private fun SprintDropdownMenuWidget(isOffline: Boolean, state: SprintState, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    dialogTonalElevation
                )
            ),
            expanded = state.isMenuExpanded,
            onDismissRequest = { state.setIsMenuExpanded(false) }
        ) {
            if (state.canEdit) {
                DropdownMenuItem(
                    enabled = !isOffline,
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
            }

            if (state.canDelete) {
                DropdownMenuItem(
                    enabled = !isOffline,
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
}

@PreviewTaigaDarkLight
@Composable
private fun SprintScreenPreview() = TaigaMobilePreviewTheme {
    SprintScreenContent(
        state = SprintState(
            sprint = Sprint(
                id = 1187,
                name = "Lauren Tyler",
                order = 8443,
                start = PreviewUtils.getNowDate(),
                end = PreviewUtils.getNowDate(),
                storiesCount = 8890,
                isClosed = false
            )
        ),
        navigateToTask = { _, _, _ -> },
        isOffline = false
    )
}
