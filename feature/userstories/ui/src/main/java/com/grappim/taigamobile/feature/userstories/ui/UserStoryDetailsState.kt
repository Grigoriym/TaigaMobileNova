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
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class UserStoryDetailsState(
    val toolbarTitle: NativeText = NativeText.Empty,
    val isLoading: Boolean = false,
    val retryLoadUserStory: () -> Unit = {},
    val initialLoadError: NativeText = NativeText.Empty,
    val sprint: Sprint? = null,
    val filtersData: FiltersData? = null,

    val onTitleSave: () -> Unit = {},
    val onBadgeSave: (SelectableWorkItemBadgeState, StatusUI) -> Unit = { _, _ -> },

    val error: NativeText = NativeText.Empty,

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit = {},

    val currentUserStory: UserStory? = null,
    val originalUserStory: UserStory? = null,

    val onTagRemove: (TagUI) -> Unit = {},

    val isDueDateDatePickerVisible: Boolean = false,
    val setIsDueDatePickerVisible: (Boolean) -> Unit = {},
    val setDueDate: (Long?) -> Unit = {},
    val isDueDateLoading: Boolean = false,
    val dueDateText: NativeText = NativeText.Empty,

    val creator: User? = null,

    val removeWatcher: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val assignees: PersistentList<User> = persistentListOf(),
    val isAssignedToMe: Boolean = false,
    val onAssignToMe: () -> Unit = {},
    val onGoingToEditAssignees: () -> Unit = {},
    val removeAssignee: () -> Unit = {},
    val isAssigneesLoading: Boolean = false,
    val isRemoveAssigneeDialogVisible: Boolean = false,
    val setIsRemoveAssigneeDialogVisible: (Boolean) -> Unit = {},
    val onRemoveAssigneeClick: (User) -> Unit = {},
    val assigneeToRemove: User? = null,

    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},

    val onCommentRemove: (Comment) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit = { _, _ -> },
    val isBlockDialogVisible: Boolean = false,
    val setIsBlockDialogVisible: (Boolean) -> Unit = {},

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {}
)
