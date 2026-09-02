package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.are_you_sure_discarding_changes
import com.grappim.taigamobile.strings.generated.resources.discard
import com.grappim.taigamobile.strings.generated.resources.edit_sprint
import com.grappim.taigamobile.strings.generated.resources.keep_editing
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.uikit.generated.resources.ic_check
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.PreviewUtils
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkItemEditSprintScreen(
    route: WorkItemEditSprintNavDestination,
    goBack: () -> Unit,
    viewModel: EditSprintViewModel = koinViewModel { parametersOf(route) }
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.canModify) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_sprint),
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = {
                        if (state.canModify) {
                            state.setIsDialogVisible(!state.isDialogVisible)
                        } else {
                            state.shouldGoBackWithCurrentValue(false)
                        }
                    }
                ),
                actions = buildList {
                    if (state.canModify) {
                        add(
                            TopBarActionTextButton(
                                text = NativeText.Resource(RString.save),
                                onClick = {
                                    state.shouldGoBackWithCurrentValue(true)
                                }
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = {
            state.setIsDialogVisible(!state.isDialogVisible)
        }
    )

    ConfirmActionDialog(
        isVisible = state.isDialogVisible,
        description = stringResource(RString.are_you_sure_discarding_changes),
        onConfirm = {
            state.shouldGoBackWithCurrentValue(false)
        },
        onDismiss = {
            state.setIsDialogVisible(false)
        },
        confirmButtonText = NativeText.Resource(RString.discard),
        dismissButtonText = NativeText.Resource(RString.keep_editing)
    )

    ObserveAsEvents(viewModel.onBackAction, isImmediate = false) {
        goBack()
    }

    EditSprintContent(state = state)
}

@Composable
private fun EditSprintContent(state: EditSprintState) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(
                items = state.itemsToShow,
                key = { _, sprint -> sprint.id }
            ) { index, sprint ->
                SprintItem(
                    sprint = sprint,
                    canModify = state.canModify,
                    isSelected = state.isItemSelected(sprint.id),
                    onItemClick = {
                        state.onSprintClick(sprint.id)
                    }
                )

                if (index < state.itemsToShow.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun SprintItem(sprint: Sprint, isSelected: Boolean, canModify: Boolean, onItemClick: (Sprint) -> Unit) {
    ListItem(
        modifier = Modifier.then(
            if (canModify) {
                Modifier.clickable {
                    onItemClick(sprint)
                }
            } else {
                Modifier
            }
        ),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Column {
                Text(text = sprint.name)
                Text(
                    text = "${sprint.start} - ${sprint.end}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(painter = painterResource(RDrawable.ic_check), contentDescription = "")
            }
        }
    )
}

@PreviewTaigaDarkLight
@Composable
private fun EditSprintContentPreview() {
    TaigaMobilePreviewTheme {
        EditSprintContent(
            state = EditSprintState(
                itemsToShow = persistentListOf(
                    Sprint(
                        id = 1L,
                        name = "Sprint 1",
                        order = 1,
                        start = PreviewUtils.getNowDate(),
                        end = PreviewUtils.getNowDate(),
                        storiesCount = 5,
                        isClosed = false
                    ),
                    Sprint(
                        id = 2L,
                        name = "Sprint 2",
                        order = 2,
                        start = PreviewUtils.getNowDate(),
                        end = PreviewUtils.getNowDate(),
                        storiesCount = 3,
                        isClosed = true
                    )
                ),
                isItemSelected = { id -> id == 1L },
                selectedItem = 1L,
                originalSelectedItem = 1L,
                canModify = true,
                onSprintClick = {},
                isDialogVisible = false,
                setIsDialogVisible = {},
                shouldGoBackWithCurrentValue = {}
            )
        )
    }
}
