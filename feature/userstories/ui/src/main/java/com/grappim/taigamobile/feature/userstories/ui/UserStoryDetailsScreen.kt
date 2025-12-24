@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.userstories.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple.WorkItemMultipleAssigneesState
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateState
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleState
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersState
import com.grappim.taigamobile.feature.workitem.ui.widgets.AttachmentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.BlockDialog
import com.grappim.taigamobile.feature.workitem.ui.widgets.CommentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.CreatedByWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.MultipleAssignedToWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WatchersWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBadgesBottomSheet
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBlockedBannerWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDescriptionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDropdownMenuWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDueDateWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemEpicInfoWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemTitleWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.WorkItemBadgesWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.WorkItemTagsWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.CreateCommentBar
import com.grappim.taigamobile.uikit.widgets.DatePickerDialogWidget
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.dialog.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun UserStoryDetailsScreen(
    goBack: () -> Unit,
    goToEditDescription: (description: String, id: Long) -> Unit,
    goToEditTags: (id: Long) -> Unit,
    goToProfile: (Long) -> Unit,
    showSnackbar: (message: NativeText) -> Unit,
    goToEditAssignee: (id: Long) -> Unit,
    goToEditWatchers: (id: Long) -> Unit,
    goToEpic: (epicId: Long, ref: Long) -> Unit,
    goToEditEpics: (userStoryId: Long) -> Unit,
    viewModel: UserStoryDetailsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val titleState by viewModel.titleState.collectAsStateWithLifecycle()
    val badgeState by viewModel.badgeState.collectAsStateWithLifecycle()
    val tagsState by viewModel.tagsState.collectAsStateWithLifecycle()
    val commentsState by viewModel.commentsState.collectAsStateWithLifecycle()
    val attachmentsState by viewModel.attachmentsState.collectAsStateWithLifecycle()
    val watchersState by viewModel.watchersState.collectAsStateWithLifecycle()
    val customFieldsState by viewModel.customFieldsState.collectAsStateWithLifecycle()
    val dueDateState by viewModel.dueDateState.collectAsStateWithLifecycle()
    val blockState by viewModel.blockState.collectAsStateWithLifecycle()
    val assigneesState by viewModel.multipleAssigneesState.collectAsStateWithLifecycle()
    val descriptionState by viewModel.descriptionState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = { goBack() }
                ),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "User Story options",
                        onClick = {
                            state.setDropdownMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.deleteTrigger) { isDelete ->
        if (isDelete) {
            goBack()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error !is NativeText.Empty) {
            showSnackbar(state.error)
        }
    }

    LaunchedEffect(titleState.titleError) {
        if (titleState.titleError !is NativeText.Empty) {
            showSnackbar(titleState.titleError)
        }
    }

    BackHandler {
        goBack()
    }

    TaigaLoadingDialog(isVisible = state.isLoading)

    WorkItemBadgesBottomSheet(
        activeBadge = badgeState.activeBadge,
        bottomSheetState = bottomSheetState,
        onDismiss = {
            badgeState.onBadgeSheetDismiss()
        },
        onBottomSheetItemSelect = { badgeState, option ->
            state.onBadgeSave(badgeState, option)
        }
    )

    DatePickerDialogWidget(
        isVisible = dueDateState.isDueDateDatePickerVisible,
        onDismissRequest = { dueDateState.setDueDateDatePickerVisibility(false) },
        onDismissButonClick = { dueDateState.setDueDateDatePickerVisibility(false) },
        onConfirmButtonClick = { dateMillis ->
            state.setDueDate(dateMillis)
            dueDateState.setDueDateDatePickerVisibility(false)
        }
    )

    ConfirmActionDialog(
        isVisible = assigneesState.isRemoveAssigneeDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.removeAssignee()
        },
        onDismiss = { assigneesState.setIsRemoveAssigneeDialogVisible(false) }
    )

    ConfirmActionDialog(
        isVisible = watchersState.isRemoveWatcherDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.removeWatcher()
            watchersState.setIsRemoveWatcherDialogVisible(false)
        },
        onDismiss = { watchersState.setIsRemoveWatcherDialogVisible(false) }
    )

    BlockDialog(
        isVisible = blockState.isBlockDialogVisible,
        onConfirm = { blockNote ->
            state.onBlockToggle(true, blockNote)
            blockState.setIsBlockDialogVisible(false)
        },
        onDismiss = {
            blockState.setIsBlockDialogVisible(false)
        }
    )

    ConfirmActionDialog(
        isVisible = state.isDeleteDialogVisible,
        title = stringResource(RString.delete_task_title),
        description = stringResource(RString.delete_task_text),
        onConfirm = {
            state.setIsDeleteDialogVisible(false)
            state.onDelete()
        },
        onDismiss = { state.setIsDeleteDialogVisible(false) }
    )

    if (state.initialLoadError !is NativeText.Empty) {
        ErrorStateWidget(
            modifier = Modifier.fillMaxSize(),
            message = state.initialLoadError,
            onRetry = {
                state.retryLoadUserStory()
            }
        )
    } else if (state.currentUserStory != null) {
        WorkItemDropdownMenuWidget(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd),
            isExpanded = state.isDropdownMenuExpanded,
            onDismissRequest = { state.setDropdownMenuExpanded(false) },
            showSnackbar = showSnackbar,
            url = state.currentUserStory?.copyLinkUrl ?: "",
            isBlocked = state.currentUserStory?.blockedNote != null,
            setDeleteAlertVisible = {
                state.setIsDeleteDialogVisible(it)
            },
            setBlockDialogVisible = {
                blockState.setIsBlockDialogVisible(it)
            },
            doOnUnblock = {
                state.onBlockToggle(false, null)
            }
        )

        UserStoryDetailsScreenContent(
            state = state,
            titleState = titleState,
            badgeState = badgeState,
            tagsState = tagsState,
            commentsState = commentsState,
            attachmentsState = attachmentsState,
            watchersState = watchersState,
            customFieldsState = customFieldsState,
            dueDateState = dueDateState,
            assigneesState = assigneesState,
            descriptionState = descriptionState,
            goToEditDescription = goToEditDescription,
            goToEditTags = goToEditTags,
            goToProfile = goToProfile,
            goToEditAssignee = goToEditAssignee,
            goToEditWatchers = goToEditWatchers,
            goToEpic = goToEpic,
            goToEditEpics = goToEditEpics
        )
    }
}

@Composable
private fun UserStoryDetailsScreenContent(
    state: UserStoryDetailsState,
    titleState: WorkItemTitleState,
    badgeState: WorkItemBadgeState,
    tagsState: WorkItemTagsState,
    commentsState: WorkItemCommentsState,
    attachmentsState: WorkItemAttachmentsState,
    customFieldsState: WorkItemCustomFieldsState,
    watchersState: WorkItemWatchersState,
    dueDateState: WorkItemDueDateState,
    assigneesState: WorkItemMultipleAssigneesState,
    descriptionState: WorkItemDescriptionState,
    goToEditDescription: (description: String, id: Long) -> Unit,
    goToEditTags: (id: Long) -> Unit,
    goToProfile: (Long) -> Unit,
    goToEditAssignee: (id: Long) -> Unit,
    goToEditWatchers: (id: Long) -> Unit,
    goToEpic: (epicId: Long, ref: Long) -> Unit,
    goToEditEpics: (userStoryId: Long) -> Unit
) {
    requireNotNull(state.currentUserStory)

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                WorkItemTitleWidget(
                    titleState = titleState,
                    onTitleSave = state.onTitleSave
                )

                WorkItemBlockedBannerWidget(blockedNote = state.currentUserStory.blockedNote)

                WorkItemBadgesWidget(badgeState = badgeState)

                WorkItemEpicInfoWidget(
                    areUserStoryEpicsLoading = state.areUserStoryEpicsLoading,
                    userStoryEpics = state.currentUserStory.userStoryEpics,
                    onEpicClick = { epic ->
                        goToEpic(epic.id, epic.ref)
                    },
                    onEpicRemoveClick = { epic ->
                        state.onEpicRemoveClick(epic.id)
                    },
                    onLinkToEpicClick = {
                        state.onGoingToEditEpics()
                        goToEditEpics(state.currentUserStory.id)
                    }
                )

                if (state.currentUserStory.wasPromotedFromTask) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = stringResource(RString.user_story_was_task),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                WorkItemDescriptionWidget(
                    currentDescription = state.currentUserStory.description,
                    onDescriptionClick = {
                        goToEditDescription(
                            state.currentUserStory.description,
                            state.currentUserStory.id
                        )
                    },
                    descriptionState = descriptionState
                )

                WorkItemTagsWidget(
                    tagsState = tagsState,
                    onTagRemoveClick = state.onTagRemove,
                    goToEditTags = {
                        state.onEditTags()
                        goToEditTags(state.currentUserStory.id)
                    }
                )

                WorkItemDueDateWidget(
                    dueDateState = dueDateState,
                    dueDateStatus = state.currentUserStory.dueDateStatus,
                    dueDate = state.currentUserStory.dueDate,
                    setDueDate = { value ->
                        state.setDueDate(value)
                    }
                )

                CreatedByWidget(
                    goToProfile = goToProfile,
                    creator = state.creator,
                    createdDateTime = state.currentUserStory.createdDateTime
                )

                MultipleAssignedToWidget(
                    assigneesState = assigneesState,
                    goToProfile = goToProfile,
                    onAssignToMe = state.onAssignToMe,
                    onAddAssigneeClick = {
                        state.onGoingToEditAssignees()
                        goToEditAssignee(state.currentUserStory.id)
                    }
                )

                WatchersWidget(
                    goToProfile = goToProfile,
                    watchersState = watchersState,
                    onAddWatcherClick = {
                        state.onGoingToEditWatchers()
                        goToEditWatchers(state.currentUserStory.id)
                    },
                    onAddMeToWatchersClick = state.onAddMeToWatchersClick,
                    onRemoveMeFromWatchersClick = state.onRemoveMeFromWatchersClick
                )

                CustomFieldsSectionWidget(
                    customFieldsState = customFieldsState,
                    onCustomFieldSave = state.onCustomFieldSave
                )

                AttachmentsSectionWidget(
                    attachmentsState = attachmentsState,
                    onAttachmentAdd = { uri ->
                        state.onAttachmentAdd(uri)
                    },
                    onAttachmentRemove = {
                        state.onAttachmentRemove(it)
                    }
                )

                CommentsSectionWidget(
                    commentsState = commentsState,
                    goToProfile = goToProfile,
                    onCommentRemove = { value ->
                        state.onCommentRemove(value)
                    }
                )
            }
        }
        CreateCommentBar(onButtonClick = state.onCreateCommentClick)
    }
}
