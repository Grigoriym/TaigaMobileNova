package com.grappim.taigamobile.feature.workitem.ui.screens.teammembers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import coil3.compose.AsyncImage
import com.grappim.taigamobile.feature.workitem.ui.models.TeamMemberUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.are_you_sure_discarding_changes
import com.grappim.taigamobile.strings.generated.resources.discard
import com.grappim.taigamobile.strings.generated.resources.edit_team_members
import com.grappim.taigamobile.strings.generated.resources.keep_editing
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.uikit.generated.resources.default_avatar
import com.grappim.taigamobile.uikit.generated.resources.ic_check
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun WorkItemEditTeamMemberScreen(
    route: WorkItemEditTeamMemberNavDestination,
    goBack: () -> Unit,
    viewModel: EditTeamMemberViewModel = koinViewModel { parametersOf(route) }
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_team_members),
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

    EditAssigneeContent(state = state)
}

@Composable
private fun EditAssigneeContent(state: EditTeamMemberState) {
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
private fun TeamMemberItem(teamMemberUI: TeamMemberUI, isSelected: Boolean, onItemClick: (TeamMemberUI) -> Unit) {
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
                    Icon(painter = painterResource(RDrawable.ic_check), contentDescription = "")
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

@PreviewTaigaDarkLight
@Composable
private fun EditAssigneeContentPreview() {
    TaigaMobilePreviewTheme {
        EditAssigneeContent(
            state = EditTeamMemberState(
                itemsToShow = persistentListOf(
                    TeamMemberUI(id = 1L, name = "Alice Johnson", avatarUrl = null),
                    TeamMemberUI(id = 2L, name = "Bob Smith", avatarUrl = null),
                    TeamMemberUI(id = 3L, name = "Charlie Brown", avatarUrl = null)
                ),
                isItemSelected = { id -> id == 1L },
                selectedItems = persistentListOf(1L),
                originalSelectedItems = persistentListOf(1L),
                onTeamMemberClick = {},
                isDialogVisible = false,
                setIsDialogVisible = {},
                shouldGoBackWithCurrentValue = {}
            )
        )
    }
}
