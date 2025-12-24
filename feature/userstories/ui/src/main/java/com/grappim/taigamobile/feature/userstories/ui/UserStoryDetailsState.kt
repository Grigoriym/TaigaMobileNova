package com.grappim.taigamobile.feature.userstories.ui

import android.net.Uri
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.utils.ui.NativeText

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

    val setDueDate: (Long?) -> Unit = {},

    val creator: User? = null,

    val removeWatcher: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val onAssignToMe: () -> Unit = {},
    val removeAssignee: () -> Unit = {},

    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},

    val onCommentRemove: (Comment) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit = { _, _ -> },

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {},
    val customFieldsVersion: Long = 0,

    val areUserStoryEpicsLoading: Boolean = false,
    val onEpicRemoveClick: (Long) -> Unit = {},
    val onGoingToEditEpics: () -> Unit = {},

    val onEditTags: () -> Unit = {},
    val onGoingToEditAssignees: () -> Unit = {},
    val onGoingToEditWatchers: () -> Unit = {}
)
