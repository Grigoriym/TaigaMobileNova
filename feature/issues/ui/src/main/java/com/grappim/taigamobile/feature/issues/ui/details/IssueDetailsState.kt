package com.grappim.taigamobile.feature.issues.ui.details

import android.net.Uri
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.issues.ui.model.IssueUI
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.utils.ui.NativeText

data class IssueDetailsState(
    val isLoading: Boolean = false,
    /**
     * We use it on first load, so that we could reload whole issue
     */
    val initialLoadError: NativeText = NativeText.Empty,
    val loadIssue: () -> Unit = {},

    val error: NativeText = NativeText.Empty,

    val currentIssue: IssueUI? = null,
    val originalIssue: IssueUI? = null,

    val onTitleSave: () -> Unit = {},
    val onBadgeSave: (SelectableWorkItemBadgeState, StatusUI) -> Unit = { _, _ -> },

    val sprint: Sprint? = null,
    val onGoingToEditSprint: () -> Unit = {},
    val isSprintLoading: Boolean = false,

    val filtersData: FiltersData? = null,

    val onCommentRemove: (Comment) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},

    val toolbarTitle: NativeText,
    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val setDueDate: (Long?) -> Unit = {},

    val creator: User? = null,

    val onUnassign: () -> Unit = {},
    val onAssignToMe: () -> Unit = {},
    val removeAssignee: () -> Unit = {},

    val removeWatcher: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val onTagRemove: (TagUI) -> Unit,

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit,

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {},
    val customFieldsVersion: Long = 0,

    val onPromoteClick: () -> Unit = {}
)
