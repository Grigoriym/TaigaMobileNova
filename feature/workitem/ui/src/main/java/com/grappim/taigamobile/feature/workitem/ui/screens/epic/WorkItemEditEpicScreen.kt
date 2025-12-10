package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WorkItemEditEpicScreen(goBack: () -> Unit, viewModel: EditEpicViewModel = hiltViewModel()) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_epic),
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = { state.setIsDialogVisible(!state.isDialogVisible) }
                ),
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        onClick = {
                            state.shouldGoBackWithCurrentValue(true)
                        }
                    )
                )
            )
        )
    }

    BackHandler {
        state.setIsDialogVisible(!state.isDialogVisible)
    }

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
        dismissButtonText = NativeText.Resource(
            RString.keep_editing
        )
    )

    ObserveAsEvents(viewModel.onBackAction, isImmediate = false) {
        goBack()
    }

    EditEpicContent(state = state)
}

@Composable
private fun EditEpicContent(state: EditEpicState) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(
                items = state.itemsToShow,
                key = { _, epic -> epic.id }
            ) { index, epic ->
                EpicItem(
                    epic = epic,
                    isSelected = state.isItemSelected(epic.id),
                    onItemClick = {
                        state.onEpicClick(epic.id)
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
private fun EpicItem(epic: Epic, isSelected: Boolean, onItemClick: (Epic) -> Unit) {
    ListItem(
        modifier = Modifier.clickable {
            onItemClick(epic)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Column {
                Text(text = epic.title)
                Text(
                    text = "#${epic.ref}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "")
            }
        }
    )
}
