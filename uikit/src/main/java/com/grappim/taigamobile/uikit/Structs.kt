package com.grappim.taigamobile.uikit

/**
 * Helper structs for CommonTaskScreen
 */

import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.CommentDTO
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.EpicShortInfo
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.core.navigation.NavigateToTask
import java.io.InputStream
import java.time.LocalDate

/**
 * Generic edit action
 */
class EditAction<TItem : Any, TRemove>(
    val items: List<TItem> = emptyList(),
    val itemsLazy: LazyPagingItems<TItem>? = null,
    val searchItems: (query: String) -> Unit = {},
    val select: (item: TItem) -> Unit = {},
    val isLoading: Boolean = false,
    val remove: (item: TRemove) -> Unit = {}
)

/**
 * And some type aliases for certain cases
 */
typealias SimpleEditAction<TItem> = EditAction<TItem, TItem>
typealias EmptyEditAction = EditAction<Unit, Unit>

/**
 * All edit actions
 */
class EditActions(
    val editStatusOld: SimpleEditAction<StatusOld> = SimpleEditAction(),
    val editType: SimpleEditAction<StatusOld> = SimpleEditAction(),
    val editSeverity: SimpleEditAction<StatusOld> = SimpleEditAction(),
    val editPriority: SimpleEditAction<StatusOld> = SimpleEditAction(),
    val editSwimlaneDTO: SimpleEditAction<SwimlaneDTO> = SimpleEditAction(),
    val editSprint: SimpleEditAction<Sprint> = SimpleEditAction(),
    val editEpics: EditAction<CommonTask, EpicShortInfo> = EditAction(),
    val editAttachments: EditAction<Pair<String, InputStream>, AttachmentDTO> = EditAction(),
    val editAssignees: SimpleEditAction<UserDTO> = SimpleEditAction(),
    val editWatchers: SimpleEditAction<UserDTO> = SimpleEditAction(),
    val editComments: EditAction<String, CommentDTO> = EditAction(),
    val editBasicInfo: SimpleEditAction<Pair<String, String>> = SimpleEditAction(),
    val editCustomField: SimpleEditAction<Pair<CustomField, CustomFieldValue?>> =
        SimpleEditAction(),
    val editTags: SimpleEditAction<Tag> = SimpleEditAction(),
    val editDueDate: EditAction<LocalDate, Unit> = EditAction(),
    val editEpicColor: SimpleEditAction<String> = SimpleEditAction(),
    val deleteTask: EmptyEditAction = EmptyEditAction(),
    val promoteTask: EmptyEditAction = EmptyEditAction(),
    val editAssign: EmptyEditAction = EmptyEditAction(),
    val editWatch: EmptyEditAction = EmptyEditAction(),
    val editBlocked: EditAction<String, Unit> = EditAction()
)

/**
 * All navigation actions
 */
class NavigationActions(
    val navigateBack: () -> Unit = {},
    val navigateToCreateTask: () -> Unit = {},
    val navigateToTask: NavigateToTask = { _, _, _ -> }
)
