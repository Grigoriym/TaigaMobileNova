package com.grappim.taigamobile.commontask

/**
 * Helper structs for CommonTaskScreen
 */

import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.core.nav.NavigateToTask
import com.grappim.taigamobile.domain.entities.Attachment
import com.grappim.taigamobile.domain.entities.Comment
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CustomField
import com.grappim.taigamobile.domain.entities.CustomFieldValue
import com.grappim.taigamobile.domain.entities.EpicShortInfo
import com.grappim.taigamobile.domain.entities.Sprint
import com.grappim.taigamobile.domain.entities.Status
import com.grappim.taigamobile.domain.entities.Swimlane
import com.grappim.taigamobile.domain.entities.Tag
import com.grappim.taigamobile.domain.entities.User
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
    val editStatus: SimpleEditAction<Status> = SimpleEditAction(),
    val editType: SimpleEditAction<Status> = SimpleEditAction(),
    val editSeverity: SimpleEditAction<Status> = SimpleEditAction(),
    val editPriority: SimpleEditAction<Status> = SimpleEditAction(),
    val editSwimlane: SimpleEditAction<Swimlane> = SimpleEditAction(),
    val editSprint: SimpleEditAction<Sprint> = SimpleEditAction(),
    val editEpics: EditAction<CommonTask, EpicShortInfo> = EditAction(),
    val editAttachments: EditAction<Pair<String, InputStream>, Attachment> = EditAction(),
    val editAssignees: SimpleEditAction<User> = SimpleEditAction(),
    val editWatchers: SimpleEditAction<User> = SimpleEditAction(),
    val editComments: EditAction<String, Comment> = EditAction(),
    val editBasicInfo: SimpleEditAction<Pair<String, String>> = SimpleEditAction(),
    val editCustomField: SimpleEditAction<Pair<CustomField, CustomFieldValue?>> = SimpleEditAction(),
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
    val navigateToTask: NavigateToTask = { _, _, _ -> },
)
