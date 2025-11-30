@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.epics.ui.details

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
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.epics.ui.widgets.EpicColorWidget
import com.grappim.taigamobile.feature.epics.ui.widgets.WorkItemsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeState
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleState
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersState
import com.grappim.taigamobile.feature.workitem.ui.widgets.AssignedToWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.AttachmentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.BlockDialog
import com.grappim.taigamobile.feature.workitem.ui.widgets.CommentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.CreatedByWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WatchersWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBadgesBottomSheet
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBlockedBannerWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDescriptionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDropdownMenuWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemTitleWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.WorkItemBadgesWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.WorkItemTagsWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.CreateCommentBar
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun EpicDetailsScreen(
    showSnackbar: (message: NativeText) -> Unit,
    goToProfile: (userId: Long) -> Unit,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goBack: () -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit,
    goToUserStory: (id: Long, type: CommonTaskType, ref: Int) -> Unit,
    viewModel: EpicDetailsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val titleState by viewModel.titleState.collectAsStateWithLifecycle()
    val badgeState by viewModel.badgeState.collectAsStateWithLifecycle()
    val deleteTrigger by viewModel.deleteTrigger.collectAsStateWithLifecycle(false)
    val blockState by viewModel.blockState.collectAsStateWithLifecycle()
    val watchersState by viewModel.watchersState.collectAsStateWithLifecycle()
    val assigneesState by viewModel.singleAssigneeState.collectAsStateWithLifecycle()
    val tagsState by viewModel.tagsState.collectAsStateWithLifecycle()
    val commentsState by viewModel.commentsState.collectAsStateWithLifecycle()
    val attachmentState by viewModel.attachmentsState.collectAsStateWithLifecycle()
    val customFieldsState by viewModel.customFieldsState.collectAsStateWithLifecycle()
    val descriptionState by viewModel.descriptionState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                showBackButton = true,
                overrideBackHandlerAction = {
                    goBack()
                },
                actions = listOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "Task options",
                        onClick = {
                            state.setDropdownMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    BackHandler {
        goBack()
    }

    LaunchedEffect(state.error) {
        if (state.error !is NativeText.Empty) {
            showSnackbar(state.error)
        }
    }

    LaunchedEffect(deleteTrigger) {
        if (deleteTrigger) {
            goBack()
        }
    }

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

    ConfirmActionDialog(
        isVisible = assigneesState.isRemoveAssigneeDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.removeAssignee()
            assigneesState.setIsRemoveAssigneeDialogVisible(false)
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

    TaigaLoadingDialog(isVisible = state.isLoading)

    if (state.initialLoadError !is NativeText.Empty) {
        ErrorStateWidget(
            modifier = Modifier.fillMaxSize(),
            message = state.initialLoadError,
            onRetry = {
                state.retryLoadEpic()
            }
        )
    } else if (state.currentEpic != null) {
        WorkItemDropdownMenuWidget(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd),
            isExpanded = state.isDropdownMenuExpanded,
            onDismissRequest = { state.setDropdownMenuExpanded(false) },
            showSnackbar = showSnackbar,
            url = state.currentEpic?.copyLinkUrl ?: "",
            setDeleteAlertVisible = {
                state.setIsDeleteDialogVisible(it)
            },
            isBlocked = state.currentEpic?.blockedNote != null,
            setBlockDialogVisible = {
                blockState.setIsBlockDialogVisible(it)
            },
            doOnUnblock = {
                state.onBlockToggle(false, null)
            }
        )

        EpicDetailsScreenContent(
            state = state,
            titleState = titleState,
            badgeState = badgeState,
            tagsState = tagsState,
            commentsState = commentsState,
            attachmentsState = attachmentState,
            watchersState = watchersState,
            customFieldsState = customFieldsState,
            assigneesState = assigneesState,
            descriptionState = descriptionState,
            goToEditTags = goToEditTags,
            goToEditDescription = goToEditDescription,
            goToProfile = goToProfile,
            goToEditAssignee = goToEditAssignee,
            goToEditWatchers = goToEditWatchers,
            goToUserStory = goToUserStory
        )
    }
}

@Composable
private fun EpicDetailsScreenContent(
    state: EpicDetailsState,
    titleState: WorkItemTitleState,
    badgeState: WorkItemBadgeState,
    tagsState: WorkItemTagsState,
    commentsState: WorkItemCommentsState,
    attachmentsState: WorkItemAttachmentsState,
    watchersState: WorkItemWatchersState,
    customFieldsState: WorkItemCustomFieldsState,
    assigneesState: WorkItemSingleAssigneeState,
    descriptionState: WorkItemDescriptionState,
    goToProfile: (Long) -> Unit,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit,
    goToUserStory: (id: Long, type: CommonTaskType, ref: Int) -> Unit
) {
    requireNotNull(state.currentEpic)

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    currentTitle = titleState.currentTitle,
                    onTitleChange = titleState.onTitleChange,
                    onTitleSave = state.onTitleSave,
                    isLoading = titleState.isTitleLoading,
                    isEditable = titleState.isTitleEditable,
                    setIsEditable = titleState.setIsTitleEditable,
                    onCancelClick = titleState.onCancelClick
                )
                WorkItemBlockedBannerWidget(blockedNote = state.currentEpic.blockedNote)

                WorkItemBadgesWidget(
                    updatingBadges = badgeState.updatingBadges,
                    items = badgeState.workItemBadges,
                    onBadgeClick = badgeState.onBadgeClick
                )

                EpicColorWidget(
                    isEpicColorLoading = state.isEpicColorLoading,
                    epicColor = state.currentEpic.epicColor,
                    onColorPick = state.onEpicColorPick
                )

                WorkItemDescriptionWidget(
                    currentDescription = state.currentEpic.description,
                    onDescriptionClick = {
                        goToEditDescription(state.currentEpic.description)
                    },
                    isLoading = descriptionState.isDescriptionLoading
                )

                WorkItemTagsWidget(
                    tags = tagsState.tags,
                    onTagRemoveClick = state.onTagRemove,
                    areTagsLoading = tagsState.areTagsLoading,
                    goToEditTags = {
                        tagsState.onGoingToEditTags()
                        goToEditTags()
                    }
                )

                CreatedByWidget(
                    goToProfile = goToProfile,
                    creator = state.creator,
                    createdDateTime = state.currentEpic.createdDateTime
                )

                AssignedToWidget(
                    goToProfile = goToProfile,
                    assignees = assigneesState.assignees,
                    isAssigneesLoading = assigneesState.isAssigneesLoading,
                    onRemoveAssigneeClick = {
                        assigneesState.onRemoveAssigneeClick()
                    },
                    isAssignedToMe = assigneesState.isAssignedToMe,
                    onUnassign = state.onUnassign,
                    onAssignToMe = state.onAssignToMe,
                    onAddAssigneeClick = {
                        assigneesState.onGoingToEditAssignee()
                        goToEditAssignee()
                    }
                )

                WatchersWidget(
                    goToProfile = goToProfile,
                    watchers = watchersState.watchers,
                    onRemoveWatcherClick = { watcherId ->
                        watchersState.onRemoveWatcherClick(watcherId)
                    },
                    isWatchersLoading = watchersState.areWatchersLoading,
                    onAddWatcherClick = {
                        watchersState.onGoingToEditWatchers()
                        goToEditWatchers()
                    },
                    isWatchedByMe = watchersState.isWatchedByMe,
                    onAddMeToWatchersClick = state.onAddMeToWatchersClick,
                    onRemoveMeFromWatchersClick = state.onRemoveMeFromWatchersClick
                )

                CustomFieldsSectionWidget(
                    customFieldStateItems = customFieldsState.customFieldStateItems,
                    isCustomFieldsLoading = customFieldsState.isCustomFieldsLoading,
                    isCustomFieldsWidgetExpanded = customFieldsState.isCustomFieldsWidgetExpanded,
                    setIsCustomFieldsWidgetExpanded = customFieldsState.setIsCustomFieldsWidgetExpanded,
                    onCustomFieldChange = customFieldsState.onCustomFieldChange,
                    onCustomFieldSave = state.onCustomFieldSave,
                    onCustomFieldEditToggle = customFieldsState.onCustomFieldEditToggle,
                    editingItemIds = customFieldsState.editingItemIds
                )

                WorkItemsSectionWidget(
                    workItemUIS = state.userStories,
                    workItemsType = CommonTaskType.UserStory,
                    areWorkItemsExpanded = state.areWorkItemsExpanded,
                    setAreWorkItemsExpanded = state.setAreWorkItemsExpanded,
                    goToWorkItem = goToUserStory
                )

                AttachmentsSectionWidget(
                    attachments = attachmentsState.attachments,
                    isAttachmentsLoading = attachmentsState.areAttachmentsLoading,
                    onAttachmentAdd = { uri ->
                        state.onAttachmentAdd(uri)
                    },
                    areAttachmentsExpanded = attachmentsState.areAttachmentsExpanded,
                    setAreAttachmentsExpanded = attachmentsState.setAreAttachmentsExpanded,
                    onAttachmentRemove = {
                        state.onAttachmentRemove(it)
                    }
                )

                CommentsSectionWidget(
                    comments = commentsState.comments,
                    goToProfile = goToProfile,
                    isCommentsWidgetExpanded = commentsState.isCommentsWidgetExpanded,
                    setIsCommentsWidgetExpanded = { value ->
                        commentsState.setIsCommentsWidgetExpanded(value)
                    },
                    areCommentsLoading = commentsState.areCommentsLoading,
                    onCommentRemove = { value ->
                        state.onCommentRemove(value)
                    }
                )
            }
        }
        CreateCommentBar(onButtonClick = state.onCreateCommentClick)
    }
}
