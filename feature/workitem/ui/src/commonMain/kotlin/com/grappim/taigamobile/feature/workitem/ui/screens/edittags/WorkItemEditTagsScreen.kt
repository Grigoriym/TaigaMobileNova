package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog.TagEditDialog
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.are_you_sure_discarding_tags_changes
import com.grappim.taigamobile.strings.generated.resources.discard
import com.grappim.taigamobile.strings.generated.resources.edit_tags
import com.grappim.taigamobile.strings.generated.resources.keep_editing
import com.grappim.taigamobile.uikit.generated.resources.ic_options
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkItemEditTagsScreen(
    showSnackbar: (NativeText) -> Unit,
    goBack: () -> Unit,
    viewModel: WorkItemEditTagsViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tagEditDialogState by viewModel.tagEditDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_tags),
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = state.onBackClick
                ),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "Issue options",
                        onClick = {
                            state.setDropdownMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    ConfirmActionDialog(
        isVisible = state.isDialogVisible,
        description = stringResource(RString.are_you_sure_discarding_tags_changes),
        onConfirm = {
            state.shouldGoBackWithCurrentValue(false)
        },
        onDismiss = {
            state.setIsDialogVisible(false)
        },
        confirmButtonText = NativeText.Resource(RString.discard),
        dismissButtonText = NativeText.Resource(RString.keep_editing)
    )

    ObserveAsEvents(viewModel.snackBarMessage) { message ->
        if (message.isNotEmpty()) {
            showSnackbar(message)
        }
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = {
            state.onBackClick()
        }
    )

    ObserveAsEvents(viewModel.onBackAction, isImmediate = false) {
        goBack()
    }

    EditTagsDropdownMenu(state)

    TagEditDialog(
        state = tagEditDialogState,
        onSaveClick = { name, color ->
            state.onSaveClick(name, color)
        }
    )

    EditTagsContent(state)
}

@Composable
private fun EditTagsContent(state: EditTagsState) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(
                items = state.tags,
                key = { _, tag -> tag.name }
            ) { index, tag ->
                TagItem(
                    tag = tag,
                    onItemClick = {
                        state.onTagClick(it)
                    }
                )

                if (index < state.tags.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun TagItem(tag: SelectableTagUI, onItemClick: (SelectableTagUI) -> Unit) {
    ListItem(
        modifier = Modifier
            .clickable {
                onItemClick(tag)
            },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Row {
                if (tag.isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "")
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                TaigaWidthSpacer(6.dp)

                Text(
                    text = tag.name
                )
            }
        },
        trailingContent = {
            Spacer(
                Modifier
                    .size(32.dp)
                    .background(color = tag.color, shape = MaterialTheme.shapes.small)
                    .clickable { }
            )
        }
    )
}
