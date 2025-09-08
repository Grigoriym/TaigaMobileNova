package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.commontask.CommonTaskViewModel
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.EditAction
import com.grappim.taigamobile.uikit.SimpleEditAction
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.editor.SelectorList
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.ui.toColor
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Bunch of common selectors
 */
@Composable
@Deprecated("a god class must be removed")
fun Selectors(
    statusOldEntry: SelectorEntry<StatusOld> = SelectorEntry(),
    typeEntry: SelectorEntry<StatusOld> = SelectorEntry(),
    severityEntry: SelectorEntry<StatusOld> = SelectorEntry(),
    priorityEntry: SelectorEntry<StatusOld> = SelectorEntry(),
    sprintEntry: SelectorEntry<Sprint> = SelectorEntry(),
    epicsEntry: SelectorEntry<CommonTask> = SelectorEntry(),
    assigneesEntry: SelectorEntry<UserDTO> = SelectorEntry(),
    watchersEntry: SelectorEntry<UserDTO> = SelectorEntry(),
    swimlaneDTOEntry: SelectorEntry<SwimlaneDTO> = SelectorEntry()
) {
    // status editor
    SelectorList(
        titleHintId = RString.choose_status,
        items = statusOldEntry.edit.items,
        isVisible = statusOldEntry.isVisible,
        isSearchable = false,
        searchData = statusOldEntry.edit.searchItems,
        navigateBack = statusOldEntry.hide
    ) {
        StatusItem(
            statusOld = it,
            onClick = {
                statusOldEntry.edit.select(it)
                statusOldEntry.hide()
            }
        )
    }

    // type editor
    SelectorList(
        titleHintId = RString.choose_type,
        items = typeEntry.edit.items,
        isVisible = typeEntry.isVisible,
        isSearchable = false,
        searchData = typeEntry.edit.searchItems,
        navigateBack = typeEntry.hide
    ) {
        StatusItem(
            statusOld = it,
            onClick = {
                typeEntry.edit.select(it)
                typeEntry.hide()
            }
        )
    }

    // severity editor
    SelectorList(
        titleHintId = RString.choose_severity,
        items = severityEntry.edit.items,
        isVisible = severityEntry.isVisible,
        isSearchable = false,
        searchData = severityEntry.edit.searchItems,
        navigateBack = severityEntry.hide
    ) {
        StatusItem(
            statusOld = it,
            onClick = {
                severityEntry.edit.select(it)
                severityEntry.hide()
            }
        )
    }

    // priority editor
    SelectorList(
        titleHintId = RString.choose_priority,
        items = priorityEntry.edit.items,
        isVisible = priorityEntry.isVisible,
        isSearchable = false,
        searchData = priorityEntry.edit.searchItems,
        navigateBack = priorityEntry.hide
    ) {
        StatusItem(
            statusOld = it,
            onClick = {
                priorityEntry.edit.select(it)
                priorityEntry.hide()
            }
        )
    }

    // sprint editor
    SelectorList(
        titleHintId = RString.choose_sprint,
        itemsLazy = sprintEntry.edit.itemsLazy,
        isVisible = sprintEntry.isVisible,
        isSearchable = false,
        navigateBack = sprintEntry.hide
    ) {
        SprintItem(
            sprint = it,
            onClick = {
                sprintEntry.edit.select(it)
                sprintEntry.hide()
            }
        )
    }

    // epics editor
    SelectorList(
        titleHintId = RString.search_epics,
        itemsLazy = epicsEntry.edit.itemsLazy,
        isVisible = epicsEntry.isVisible,
        searchData = epicsEntry.edit.searchItems,
        navigateBack = epicsEntry.hide
    ) {
        EpicItem(
            epic = it,
            onClick = {
                epicsEntry.edit.select(it)
                epicsEntry.hide()
            }
        )
    }

    // assignees editor
    SelectorList(
        titleHintId = RString.search_members,
        items = assigneesEntry.edit.items,
        isVisible = assigneesEntry.isVisible,
        searchData = assigneesEntry.edit.searchItems,
        navigateBack = assigneesEntry.hide
    ) {
        MemberItem(
            member = it,
            onClick = {
                assigneesEntry.edit.select(it)
                assigneesEntry.hide()
            }
        )
    }

    // watchers editor
    SelectorList(
        titleHintId = RString.search_members,
        items = watchersEntry.edit.items,
        isVisible = watchersEntry.isVisible,
        searchData = watchersEntry.edit.searchItems,
        navigateBack = watchersEntry.hide
    ) {
        MemberItem(
            member = it,
            onClick = {
                watchersEntry.edit.select(it)
                watchersEntry.hide()
            }
        )
    }

    // swimlane editor
    SelectorList(
        titleHintId = RString.choose_swimlane,
        items = swimlaneDTOEntry.edit.items,
        isVisible = swimlaneDTOEntry.isVisible,
        isSearchable = false,
        navigateBack = swimlaneDTOEntry.hide
    ) {
        SwimlaneItem(
            swimlaneDTO = it,
            onClick = {
                swimlaneDTOEntry.edit.select(it)
                swimlaneDTOEntry.hide()
            }
        )
    }
}

class SelectorEntry<TItem : Any>(
    val edit: EditAction<TItem, *> = SimpleEditAction(),
    val isVisible: Boolean = false,
    val hide: () -> Unit = {}
)

@Composable
private fun StatusItem(statusOld: StatusOld, onClick: () -> Unit) = ContainerBoxWidget(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    Text(
        text = statusOld.name,
        color = statusOld.color.toColor()
    )
}

@Composable
private fun SprintItem(sprint: Sprint?, onClick: () -> Unit) = ContainerBoxWidget(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    sprint.takeIf { it != CommonTaskViewModel.SPRINT_HEADER }?.also {
        Surface(
            contentColor = if (it.isClosed) {
                MaterialTheme.colorScheme.outline
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ) {
            Column {
                Text(
                    if (it.isClosed) {
                        stringResource(RString.closed_sprint_name_template).format(it.name)
                    } else {
                        it.name
                    }
                )

                Text(
                    text = stringResource(RString.sprint_dates_template).format(
                        it.start.format(dateFormatter),
                        it.end.format(dateFormatter)
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } ?: run {
        Text(
            text = stringResource(RString.move_to_backlog),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MemberItem(member: UserDTO, onClick: () -> Unit) = ContainerBoxWidget(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    UserItem(member)
}

@Composable
private fun EpicItem(epic: CommonTask, onClick: () -> Unit) = ContainerBoxWidget(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    CommonTaskTitle(
        ref = epic.ref,
        title = epic.title,
        indicatorColorsHex = epic.colors,
        isInactive = epic.isClosed
    )
}

@Composable
private fun SwimlaneItem(swimlaneDTO: SwimlaneDTO, onClick: () -> Unit) = ContainerBoxWidget(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    val swimlaneNullable = swimlaneDTO.takeIf { it != CommonTaskViewModel.SWIMLANE_DTO_HEADER }

    Text(
        text = swimlaneNullable?.name ?: stringResource(RString.unclassifed),
        style = MaterialTheme.typography.bodyLarge,
        color = swimlaneNullable?.let { MaterialTheme.colorScheme.onSurface }
            ?: MaterialTheme.colorScheme.primary
    )
}
