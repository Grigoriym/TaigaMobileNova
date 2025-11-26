package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WorkItemEditTagsScreen(goBack: () -> Unit, viewModel: WorkItemEditTagsViewModel = hiltViewModel()) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_tags),
                showBackButton = true,
                overrideBackHandlerAction = {
                    state.setIsDialogVisible(!state.isDialogVisible)
                },
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        contentDescription = "",
                        onClick = {
                            state.shouldGoBackWithCurrentValue(true)
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

    BackHandler {
        state.setIsDialogVisible(!state.isDialogVisible)
    }

    ObserveAsEvents(viewModel.onBackAction, isImmediate = false) {
        goBack()
    }

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
private fun TagItem(tag: TagUI, onItemClick: (TagUI) -> Unit) {
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
