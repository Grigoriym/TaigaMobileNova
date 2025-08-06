@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.issues.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.patch.PatchableField
import com.grappim.taigamobile.feature.workitem.ui.widgets.AssignedToWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.AttachmentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.BlockDialog
import com.grappim.taigamobile.feature.workitem.ui.widgets.CreatedByWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WatchersWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBlockedBannerWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDescriptionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDropdownMenuWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDueDateWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemTitleWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemsBottomSheet
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.WorkItemBadgesWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.commentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.WorkItemTagsWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.CreateCommentBar
import com.grappim.taigamobile.uikit.widgets.DatePickerDialogWidget
import com.grappim.taigamobile.uikit.widgets.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun IssueDetailsScreen(
    showSnackbar: (message: NativeText) -> Unit,
    goToProfile: (userId: Long) -> Unit,
    goToEditDescription: (String) -> Unit,
    editedDescriptionValue: String?,
    goToEditTags: () -> Unit,
    wereTagsChanged: Boolean,
    wereUsersChanged: Boolean,
    goBack: (updateData: Boolean) -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit,
    viewModel: IssueDetailsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val deleteTrigger by viewModel.deleteTrigger.collectAsStateWithLifecycle(false)

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                showBackButton = true,
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

    /**
     * That means we updated the description and we need to update the value here
     */
    LaunchedEffect(editedDescriptionValue) {
        if (editedDescriptionValue != null) {
            state.onNewDescriptionUpdate(editedDescriptionValue)
        }
    }

    LaunchedEffect(wereTagsChanged) {
        if (wereTagsChanged) {
            state.onTagsUpdate()
        }
    }

    LaunchedEffect(wereUsersChanged) {
        if (wereUsersChanged) {
            state.onUsersUpdate()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error !is NativeText.Empty) {
            showSnackbar(state.error)
        }
    }

    LaunchedEffect(state.patchableFieldError) {
        if (state.patchableFieldError !is NativeText.Empty) {
            showSnackbar(state.patchableFieldError)
        }
    }

    LaunchedEffect(deleteTrigger) {
        if (deleteTrigger) {
            goBack(true)
        }
    }

    WorkItemsBottomSheet(
        activeBadge = state.activeBadge,
        bottomSheetState = bottomSheetState,
        onDismiss = {
            state.onBadgeSheetDismiss()
        },
        onBottomSheetItemSelect = { badgeState, option ->
            state.onBadgeSheetDismiss()
            state.onBadgeSheetItemClick(badgeState, option)
        }
    )

    WorkItemDropdownMenuWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        isExpanded = state.isDropdownMenuExpanded,
        onDismissRequest = { state.setDropdownMenuExpanded(false) },
        showSnackbar = showSnackbar,
        url = state.currentIssue?.copyLinkUrl ?: "",
        setDeleteAlertVisible = {
            state.setIsDeleteDialogVisible(it)
        },
        isBlocked = state.currentIssue?.blockedNote != null,
        setBlockDialogVisible = {
            state.setIsBlockDialogVisible(it)
        },
        doOnUnblock = {
            state.onBlockToggle(false, null)
        }
    )

    TaigaLoadingDialog(isVisible = state.isLoading)

    BlockDialog(
        isVisible = state.isBlockDialogVisible,
        onConfirm = { blockNote ->
            state.onBlockToggle(true, blockNote)
            state.setIsBlockDialogVisible(false)
        },
        onDismiss = {
            state.setIsBlockDialogVisible(false)
        }
    )

    DatePickerDialogWidget(
        isVisible = state.isDueDateDatePickerVisible,
        onDismissRequest = { state.setIsDueDatePickerVisible(false) },
        onDismissButonClick = { state.setIsDueDatePickerVisible(false) },
        onConfirmButtonClick = { dateMillis ->
            state.setDueDate(dateMillis)
            state.setIsDueDatePickerVisible(false)
        }
    )

    ConfirmActionDialog(
        isVisible = state.isRemoveAssigneeDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.removeAssignee()
            state.setIsRemoveAssigneeDialogVisible(false)
        },
        onDismiss = { state.setIsRemoveAssigneeDialogVisible(false) }
    )

    ConfirmActionDialog(
        isVisible = state.isRemoveWatcherDialogVisible,
        title = stringResource(RString.remove_user_title),
        description = stringResource(RString.remove_user_text),
        onConfirm = {
            state.removeWatcher()
            state.setIsRemoveWatcherDialogVisible(false)
        },
        onDismiss = { state.setIsRemoveWatcherDialogVisible(false) }
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

    if (state.currentIssue != null) {
        IssueDetailsScreenContent(
            state = state,
            goToProfile = goToProfile,
            goToEditDescription = goToEditDescription,
            goToEditTags = goToEditTags,
            goToEditAssignee = goToEditAssignee,
            goToEditWatchers = goToEditWatchers
        )
    }
}

@Composable
private fun IssueDetailsScreenContent(
    state: IssueDetailsState,
    goToProfile: (Long) -> Unit,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit
) {
    requireNotNull(state.currentIssue)

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    WorkItemTitleWidget(
                        currentTitle = state.currentIssue.title,
                        originalTitle = state.originalIssue?.title ?: "",
                        // todo maybe remove it, on front they don't do strikethrough
                        // todo if we try to change the status rn, this one won't be changed
                        isClosed = state.currentIssue.isClosed,
                        onTitleChange = {
                            state.onFieldChanged(PatchableField.TITLE, it)
                        },
                        onTitleSave = {
                            state.onSaveField(PatchableField.TITLE)
                        },
                        isLoading = PatchableField.TITLE in state.savingFields,
                        isEditable = PatchableField.TITLE in state.editableFields,
                        setIsEditable = { isEditable ->
                            state.onFieldSetIsEditable(PatchableField.TITLE, isEditable)
                        }
                    )
                }

                item {
                    WorkItemBlockedBannerWidget(blockedNote = state.currentIssue.blockedNote)
                }

                item {
                    WorkItemBadgesWidget(
                        updatingBadges = state.updatingBadges,
                        items = state.workItemBadges,
                        onWorkingItemBadgeClick = state.onWorkingItemBadgeClick
                    )
                }

                item {
                    WorkItemDescriptionWidget(
                        currentDescription = state.currentIssue.description,
                        onDescriptionClick = {
                            goToEditDescription(state.currentIssue.description)
                        }
                    )
                }

                item {
                    WorkItemTagsWidget(
                        tags = state.tags,
                        onTagRemoveClick = state.onTagRemove,
                        areTagsLoading = state.areTagsLoading,
                        goToEditTags = {
                            state.onGoingToEditTags()
                            goToEditTags()
                        }
                    )
                }

                item {
                    WorkItemDueDateWidget(
                        dueDateText = state.dueDateText,
                        dueDateStatus = state.currentIssue.dueDateStatus,
                        isLoading = state.isDueDateLoading,
                        dueDate = state.currentIssue.dueDate,
                        setIsDueDatePickerVisible = { value ->
                            state.setIsDueDatePickerVisible(value)
                        },
                        setDueDate = { value ->
                            state.setDueDate(value)
                        }
                    )
                }

                item {
                    CreatedByWidget(
                        goToProfile = goToProfile,
                        creator = state.creator,
                        createdDateTime = state.currentIssue.createdDateTime
                    )
                }

                item {
                    AssignedToWidget(
                        goToProfile = goToProfile,
                        assignees = state.assignees,
                        isAssigneesLoading = state.isAssigneesLoading,
                        onRemoveAssigneeClick = {
                            state.onRemoveAssigneeClick()
                        },
                        isAssignedToMe = state.isAssignedToMe,
                        onUnassign = state.onUnassign,
                        onAssignToMe = state.onAssignToMe,
                        onAddAssigneeClick = {
                            state.onGoingToEditAssignee()
                            goToEditAssignee()
                        }
                    )
                }

                item {
                    WatchersWidget(
                        goToProfile = goToProfile,
                        watchers = state.watchers,
                        onRemoveWatcherClick = { watcherId ->
                            state.onRemoveWatcherClick(watcherId)
                        },
                        isWatchersLoading = state.isWatchersLoading,
                        onAddWatcherClick = {
                            state.onGoingToEditWatchers()
                            goToEditWatchers()
                        },
                        isWatchedByMe = state.isWatchedByMe,
                        onAddMeToWatchersClick = state.onAddMeToWatchersClick,
                        onRemoveMeFromWatchersClick = state.onRemoveMeFromWatchersClick
                    )
                }

                item {
                    CustomFieldsSectionWidget(
                        customFieldStateItems = state.customFieldStateItems,
                        isCustomFieldsLoading = state.isCustomFieldsLoading,
                        isCustomFieldsWidgetExpanded = state.isCustomFieldsWidgetExpanded,
                        setIsCustomFieldsWidgetExpanded = state.setIsCustomFieldsWidgetExpanded,
                        onCustomFieldChange = state.onCustomFieldChange,
                        onCustomFieldSave = state.onCustomFieldSave,
                        onCustomFieldEditToggle = state.onCustomFieldEditToggle,
                        editingItemIds = state.editingItemIds
                    )
                }

                item {
                    AttachmentsSectionWidget(
                        attachments = state.attachments,
                        isAttachmentsLoading = state.isAttachmentsLoading,
                        onAttachmentAdd = { uri ->
                            state.onAttachmentAdd(uri)
                        },
                        areAttachmentsExpanded = state.areAttachmentsExpanded,
                        setAreAttachmentsExpanded = state.setAreAttachmentsExpanded,
                        onAttachmentRemove = {
                            state.onAttachmentRemove(it)
                        }
                    )
                }

                commentsSectionWidget(
                    comments = state.comments,
                    goToProfile = goToProfile,
                    isCommentsWidgetExpanded = state.isCommentsWidgetExpanded,
                    setIsCommentsWidgetExpanded = { value ->
                        state.setIsCommentsWidgetExpanded(value)
                    },
                    isCommentsLoading = state.isCommentsLoading,
                    onCommentRemove = { value ->
                        state.onCommentRemove(value)
                    }
                )
            }
        }
        CreateCommentBar(onButtonClick = state.onCreateCommentClick)
    }
}
