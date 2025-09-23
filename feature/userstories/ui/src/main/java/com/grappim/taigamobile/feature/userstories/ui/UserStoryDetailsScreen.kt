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
import com.grappim.taigamobile.feature.workitem.ui.widgets.CommentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.CreatedByWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WatchersWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemBlockedBannerWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDescriptionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDropdownMenuWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDueDateWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemTitleWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemsBottomSheet
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
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents

@Composable
fun UserStoryDetailsScreen(
    goBack: () -> Unit,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToProfile: (Long) -> Unit,
    showSnackbar: (message: NativeText) -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit,
    viewModel: UserStoryDetailsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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

    LaunchedEffect(state.patchableFieldError) {
        if (state.patchableFieldError !is NativeText.Empty) {
            showSnackbar(state.patchableFieldError)
        }
    }

    BackHandler {
        goBack()
    }

    TaigaLoadingDialog(isVisible = state.isLoading)

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
                state.setIsBlockDialogVisible(it)
            },
            doOnUnblock = {
                state.onBlockToggle(false, null)
            }
        )

        UserStoryDetailsScreenContent(
            state = state,
            goToEditDescription = goToEditDescription,
            goToEditTags = goToEditTags,
            goToProfile = goToProfile,
            goToEditAssignee = goToEditAssignee,
            goToEditWatchers = goToEditWatchers
        )
    }
}

@Composable
private fun UserStoryDetailsScreenContent(
    state: UserStoryDetailsState,
    goToEditDescription: (String) -> Unit,
    goToEditTags: () -> Unit,
    goToProfile: (Long) -> Unit,
    goToEditAssignee: () -> Unit,
    goToEditWatchers: () -> Unit
) {
    requireNotNull(state.currentUserStory)
    requireNotNull(state.originalUserStory)

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
                    currentTitle = state.currentUserStory.title,
                    originalTitle = state.originalUserStory.title,
                    onTitleChange = {
                        state.onFieldChanged(PatchableField.TITLE, it)
                    },
                    isClosed = state.currentUserStory.isClosed,
                    onTitleSave = {
                        state.onSaveField(PatchableField.TITLE)
                    },
                    isLoading = PatchableField.TITLE in state.savingFields,
                    isEditable = PatchableField.TITLE in state.editableFields,
                    setIsEditable = { isEditable ->
                        state.onFieldSetIsEditable(PatchableField.TITLE, isEditable)
                    }
                )

                WorkItemBlockedBannerWidget(blockedNote = state.currentUserStory.blockedNote)

                WorkItemBadgesWidget(
                    updatingBadges = state.updatingBadges,
                    items = state.workItemBadges,
                    onWorkingItemBadgeClick = state.onWorkingItemBadgeClick
                )

                WorkItemDescriptionWidget(
                    currentDescription = state.currentUserStory.description,
                    onDescriptionClick = {
                        goToEditDescription(state.currentUserStory.description)
                    }
                )

                WorkItemTagsWidget(
                    tags = state.tags,
                    onTagRemoveClick = state.onTagRemove,
                    areTagsLoading = state.areTagsLoading,
                    goToEditTags = {
                        state.onGoingToEditTags()
                        goToEditTags()
                    }
                )

                WorkItemDueDateWidget(
                    dueDateText = state.dueDateText,
                    dueDateStatus = state.currentUserStory.dueDateStatus,
                    isLoading = state.isDueDateLoading,
                    dueDate = state.currentUserStory.dueDate,
                    setIsDueDatePickerVisible = { value ->
                        state.setIsDueDatePickerVisible(value)
                    },
                    setDueDate = { value ->
                        state.setDueDate(value)
                    }
                )

                CreatedByWidget(
                    goToProfile = goToProfile,
                    creator = state.creator,
                    createdDateTime = state.currentUserStory.createdDateTime
                )

                AssignedToWidget(
                    goToProfile = goToProfile,
                    assignees = state.assignees,
                    isAssigneesLoading = state.isAssigneesLoading,
                    onRemoveAssigneeClick = { user ->
                        state.onRemoveAssigneeClick(user)
                    },
                    isAssignedToMe = state.isAssignedToMe,
                    onAssignToMe = state.onAssignToMe,
                    isPlural = true,
                    onAddAssigneeClick = {
                        state.onGoingToEditAssignees()
                        goToEditAssignee()
                    }
                )

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

                CommentsSectionWidget(
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
