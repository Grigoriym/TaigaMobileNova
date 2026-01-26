package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MergeType
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog.TagEditDialog
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.dialog.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.StaticStringColor
import com.grappim.taigamobile.utils.ui.asColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun TagsScreen(showSnackbar: (NativeText) -> Unit, viewModel: TagsScreenViewModel = hiltViewModel()) {
    val topBarController: TopBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tagEditDialogState by viewModel.tagEditDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.tags_title),
                navigationIcon = NavigationIconConfig.Back(),
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

    ObserveAsEvents(viewModel.snackBarMessage) { message ->
        if (message.isNotEmpty()) {
            showSnackbar(message)
        }
    }

    ConfirmActionDialog(
        isVisible = state.isDeleteTagDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.deleteTag()
        },
        onDismiss = { state.closeDeleteDialog() }
    )

    TagItemsDropdownMenu(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd)
    )

    TaigaLoadingDialog(isVisible = state.isOperationLoading)

    TagEditDialog(
        state = tagEditDialogState,
        onSaveClick = { name, color ->
            state.onSaveClick(name, color)
        }
    )

    TagsScreenContent(state = state)
}

@Composable
private fun TagsScreenContent(state: TagsScreenState) {
    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        onRefresh = state.refresh,
        isRefreshing = state.isLoading
    ) {
        Column {
            if (state.isMergeMode) {
                MergePreviewCard(state = state)
            }
            LazyColumn {
                items(state.tags) { tag ->
                    if (state.isMergeMode) {
                        MergeTagItemWidget(state = state, tag = tag)
                    } else {
                        TagItemWidget(state = state, tag = tag)
                    }
                }
            }
        }
    }
}

@Composable
private fun MergePreviewCard(state: TagsScreenState) {
    val mainTag = state.tags.find { it.name == state.mainTagName }
    val mergeTags = state.tags.filter { state.tagsToMerge.contains(it.name) }
    val canMerge = mainTag != null && mergeTags.isNotEmpty()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(RString.merge_tags_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = state.onCancelMerge) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel"
                    )
                }
            }

            TaigaHeightSpacer(4.dp)

            Text(
                text = stringResource(RString.merge_tags_instruction),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (canMerge) {
                TaigaHeightSpacer(8.dp)
                Text(
                    text = "${mergeTags.joinToString { it.name }} → ${mainTag.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                TaigaHeightSpacer(8.dp)
                ExtendedFloatingActionButton(
                    onClick = state.onConfirmMerge,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Filled.MergeType,
                            contentDescription = null
                        )
                    },
                    text = { Text(stringResource(RString.merge_tags_button, mergeTags.size)) },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun MergeTagItemWidget(state: TagsScreenState, tag: TagUI) {
    val isMain = state.mainTagName == tag.name
    val isToMerge = state.tagsToMerge.contains(tag.name)

    val containerColor = if (isMain) {
        MaterialTheme.colorScheme.primaryContainer
    } else if (isToMerge) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ListItem(
        colors = ListItemDefaults.colors(containerColor = containerColor),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = isMain,
                    onClick = { state.onMainTagSelect(tag) }
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(tag.color.asColor())
                )
            }
        },
        headlineContent = {
            Text(
                text = tag.name,
                textDecoration = if (isToMerge) TextDecoration.LineThrough else null,
                fontWeight = if (isMain) FontWeight.Bold else null
            )
        },
        trailingContent = {
            if (isMain) {
                Text(
                    text = stringResource(RString.merge_main_tag_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Checkbox(
                    checked = isToMerge,
                    onCheckedChange = { state.onTagToMergeToggle(tag) }
                )
            }
        }
    )
}

@Composable
private fun TagItemWidget(state: TagsScreenState, tag: TagUI) {
    ListItem(
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(tag.color.asColor())
            )
        },
        headlineContent = { Text(tag.name) },
        trailingContent = {
            Row {
                IconButton(onClick = {
                    state.onTagEditClick(tag)
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = {
                    state.onTagDeleteClick(tag)
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun TagItemsDropdownMenu(state: TagsScreenState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        DropdownMenu(
            modifier = Modifier.background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(
                    dialogTonalElevation
                )
            ),
            expanded = state.isDropdownMenuExpanded,
            onDismissRequest = {
                state.setDropdownMenuExpanded(false)
            }
        ) {
            DropdownMenuItem(
                onClick = state.onAddTagClick,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "")
                },
                text = {
                    Text(text = stringResource(RString.add_tag))
                }
            )

            DropdownMenuItem(
                onClick = state.onMergeTagsClick,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Merge, contentDescription = "")
                },
                text = {
                    Text(text = stringResource(RString.merge_tags_title))
                }
            )
        }
    }
}

@[Composable PreviewTaigaDarkLight]
private fun TagsScreenContentPreview() {
    TaigaMobileTheme {
        TagsScreenContent(
            state = TagsScreenState(
                tags = persistentListOf(
                    TagUI(
                        name = "Bug",
                        color = StaticStringColor("#E57373")
                    ),
                    TagUI(
                        name = "Info",
                        color = StaticColor(Color.Green)
                    )
                )
            )
        )
    }
}

@[Composable PreviewTaigaDarkLight]
private fun TagsScreenMergeModePreview() {
    TaigaMobileTheme {
        TagsScreenContent(
            state = TagsScreenState(
                tags = persistentListOf(
                    TagUI(name = "Bug", color = StaticColor(Color(0xFFE57373))),
                    TagUI(name = "Feature", color = StaticColor(Color(0xFF81C784))),
                    TagUI(name = "Enhancement", color = StaticColor(Color(0xFF64B5F6))),
                    TagUI(name = "Documentation", color = StaticColor(Color(0xFFFFD54F)))
                ),
                isMergeMode = true,
                mainTagName = "Bug",
                tagsToMerge = persistentSetOf("Feature", "Enhancement")
            )
        )
    }
}
