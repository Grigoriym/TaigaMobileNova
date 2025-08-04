package com.grappim.taigamobile.feature.workitem.ui.screens.editassignees

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WorkItemEditAssigneeScreen(
    goBack: (Boolean) -> Unit,
    viewModel: EditAssigneeViewModel = hiltViewModel()
) {
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
                            state.setIsDialogVisible(false)
                            goBack(state.wasItemChanged(true))
                        }
                    )
                )
            )
        )
    }

    BackHandler {
        state.setIsDialogVisible(!state.isDialogVisible)
    }

    if (state.isDialogVisible) {
        ConfirmActionDialog(
            description = stringResource(RString.are_you_sure_discarding_changes),
            onConfirm = {
                state.setIsDialogVisible(false)
                goBack(state.wasItemChanged(false))
            },
            onDismiss = {
                state.setIsDialogVisible(false)
            },
            confirmButtonText = NativeText.Resource(RString.discard),
            dismissButtonText = NativeText.Resource(RString.keep_editing)
        )
    }

    EditAssigneeContent(state = state)
}

@Composable
private fun EditAssigneeContent(state: EditAssigneeState) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            itemsIndexed(
                items = state.itemsToShow,
                key = { _, member -> member.id }
            ) { index, member ->
                TeamMemberItem(
                    teamMemberUI = member,
                    isSelected = state.isItemSelected(member.id),
                    onItemClick = {
                        state.onTeamMemberClick(member.id)
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
private fun TeamMemberItem(
    teamMemberUI: TeamMemberUI,
    isSelected: Boolean,
    onItemClick: (TeamMemberUI) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable {
            onItemClick(teamMemberUI)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelected) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "")
                } else {
                    Spacer(modifier = Modifier.size(24.dp))
                }

                AsyncImage(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(RDrawable.default_avatar),
                    error = painterResource(RDrawable.default_avatar),
                    model = teamMemberUI.avatarUrl
                )

                TaigaWidthSpacer(6.dp)

                Text(
                    text = teamMemberUI.name
                )
            }
        }
    )
}
