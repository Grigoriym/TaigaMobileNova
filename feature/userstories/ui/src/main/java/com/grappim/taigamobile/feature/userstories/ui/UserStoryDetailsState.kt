package com.grappim.taigamobile.feature.userstories.ui

import android.net.Uri
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class UserStoryDetailsState(
    val toolbarTitle: NativeText = NativeText.Empty,
    val isLoading: Boolean = false,
    val retryLoadUserStory: () -> Unit = {},
    val initialLoadError: NativeText = NativeText.Empty,
    val sprint: Sprint? = null,
    val filtersData: FiltersData? = null,

    val onTitleSave: () -> Unit = {},

    val error: NativeText = NativeText.Empty,

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit = {},

    val currentUserStory: UserStory? = null,
    val originalUserStory: UserStory? = null,

    val activeBadge: SelectableWorkItemBadgeState? = null,
    val workItemBadges: ImmutableSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onWorkingItemBadgeClick: (SelectableWorkItemBadgeState) -> Unit = {},
    val updatingBadges: PersistentSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onBadgeSheetDismiss: () -> Unit = {},
    val onBadgeSheetItemClick: (SelectableWorkItemBadgeState, StatusUI) -> Unit = { _, _ -> },

    val tags: PersistentList<TagUI> = persistentListOf(),
    val onTagRemove: (TagUI) -> Unit = {},
    val onGoingToEditTags: () -> Unit = {},
    val areTagsLoading: Boolean = false,

    val isDueDateDatePickerVisible: Boolean = false,
    val setIsDueDatePickerVisible: (Boolean) -> Unit = {},
    val setDueDate: (Long?) -> Unit = {},
    val isDueDateLoading: Boolean = false,
    val dueDateText: NativeText = NativeText.Empty,

    val creator: User? = null,

    val watchers: PersistentList<User> = persistentListOf(),
    val isWatchedByMe: Boolean = false,
    val isWatchersLoading: Boolean = false,
    val isRemoveWatcherDialogVisible: Boolean = false,
    val watcherIdToRemove: Long? = null,
    val removeWatcher: () -> Unit = {},
    val setIsRemoveWatcherDialogVisible: (Boolean) -> Unit = {},
    val onRemoveWatcherClick: (Long) -> Unit = {},
    val onGoingToEditWatchers: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val assignees: PersistentList<User> = persistentListOf(),
    val isAssignedToMe: Boolean = false,
    val onAssignToMe: () -> Unit = {},
    val onGoingToEditAssignees: () -> Unit = {},
    val removeAssignee: () -> Unit = {},
    val isAssigneesLoading: Boolean = false,
    val isRemoveAssigneeDialogVisible: Boolean = false,
    @Deprecated("change it")
    val setIsRemoveAssigneeDialogVisible: (Boolean) -> Unit = {},
    val onRemoveAssigneeClick: (User) -> Unit = {},
    val assigneeToRemove: User? = null,

    val isCustomFieldsLoading: Boolean = false,
    val customFieldStateItems: ImmutableList<CustomFieldItemState> = persistentListOf(),
    val onCustomFieldChange: (CustomFieldItemState) -> Unit = {},
    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},
    val customFieldsVersion: Long = 0,
    val isCustomFieldsWidgetExpanded: Boolean = false,
    val setIsCustomFieldsWidgetExpanded: (Boolean) -> Unit = {},
    val onCustomFieldEditToggle: (CustomFieldItemState) -> Unit = {},
    val editingItemIds: ImmutableSet<Long> = persistentSetOf(),

    val attachments: PersistentList<Attachment> = persistentListOf(),
    val isAttachmentsLoading: Boolean = false,
    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},
    val areAttachmentsExpanded: Boolean = false,
    val setAreAttachmentsExpanded: (Boolean) -> Unit = {},

    val comments: PersistentList<Comment> = persistentListOf(),
    val isCommentsLoading: Boolean = false,
    val onCommentRemove: (Comment) -> Unit = {},
    val isCommentsWidgetExpanded: Boolean = false,
    val setIsCommentsWidgetExpanded: (Boolean) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit = { _, _ -> },
    val isBlockDialogVisible: Boolean = false,
    val setIsBlockDialogVisible: (Boolean) -> Unit = {},

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {}
)
