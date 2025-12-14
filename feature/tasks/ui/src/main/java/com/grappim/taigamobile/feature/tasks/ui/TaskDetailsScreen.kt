@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.tasks.ui

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
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple.WorkItemMultipleAssigneesState
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeState
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateState
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
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDueDateWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemTitleWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.WorkItemBadgesWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.WorkItemTagsWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.CreateCommentBar
import com.grappim.taigamobile.uikit.widgets.DatePickerDialogWidget
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun TaskDetailsScreen(
    goBack: () -> Unit,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToProfile: (Long) -> Unit,
    showSnackbar: (message: NativeText) -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit,
    viewModel: TaskDetailsViewModel = hiltViewModel()
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
    val assigneesState by viewModel.singleAssigneeState.collectAsStateWithLifecycle()
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
                        contentDescription = "Task options",
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
                state.retryLoadTask()
            }
        )
    } else if (state.currentTask != null) {
        WorkItemDropdownMenuWidget(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd),
            isExpanded = state.isDropdownMenuExpanded,
            onDismissRequest = { state.setDropdownMenuExpanded(false) },
            showSnackbar = showSnackbar,
            url = state.currentTask?.copyLinkUrl ?: "",
            isBlocked = state.currentTask?.blockedNote != null,
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

        TaskDetailsScreenContent(
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
            goToEditWatchers = goToEditWatchers
        )
    }
}

@Composable
private fun TaskDetailsScreenContent(
    state: TaskDetailsState,
    titleState: WorkItemTitleState,
    badgeState: WorkItemBadgeState,
    tagsState: WorkItemTagsState,
    commentsState: WorkItemCommentsState,
    attachmentsState: WorkItemAttachmentsState,
    customFieldsState: WorkItemCustomFieldsState,
    watchersState: WorkItemWatchersState,
    dueDateState: WorkItemDueDateState,
    assigneesState: WorkItemSingleAssigneeState,
    descriptionState: WorkItemDescriptionState,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToProfile: (Long) -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit
) {
    requireNotNull(state.currentTask)

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
                    currentTitle = titleState.currentTitle,
                    onTitleChange = titleState.onTitleChange,
                    onTitleSave = state.onTitleSave,
                    isLoading = titleState.isTitleLoading,
                    isEditable = titleState.isTitleEditable,
                    setIsEditable = titleState.setIsTitleEditable,
                    onCancelClick = titleState.onCancelClick
                )

                WorkItemBlockedBannerWidget(blockedNote = state.currentTask.blockedNote)

                WorkItemBadgesWidget(
                    updatingBadges = badgeState.updatingBadges,
                    items = badgeState.workItemBadges,
                    onBadgeClick = badgeState.onBadgeClick
                )

                WorkItemDescriptionWidget(
                    currentDescription = state.currentTask.description,
                    onDescriptionClick = {
                        goToEditDescription(state.currentTask.description)
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

                WorkItemDueDateWidget(
                    dueDateText = dueDateState.dueDateText,
                    dueDateStatus = state.currentTask.dueDateStatus,
                    isLoading = dueDateState.isDueDateLoading,
                    dueDate = state.currentTask.dueDate,
                    setIsDueDatePickerVisible = { value ->
                        dueDateState.setDueDateDatePickerVisibility(value)
                    },
                    setDueDate = { value ->
                        state.setDueDate(value)
                    }
                )

                CreatedByWidget(
                    goToProfile = goToProfile,
                    creator = state.creator,
                    createdDateTime = state.currentTask.createdDateTime
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
