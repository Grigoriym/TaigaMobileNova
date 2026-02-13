package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.dragdrop.DragDropColumn
import com.grappim.taigamobile.uikit.dragdrop.DragDropContainer
import com.grappim.taigamobile.uikit.dragdrop.DraggableItem
import com.grappim.taigamobile.uikit.dragdrop.DropIndicator
import com.grappim.taigamobile.uikit.dragdrop.MultiColumnDragDropState
import com.grappim.taigamobile.uikit.dragdrop.rememberMultiColumnDragDropState
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.cardShadowElevation
import com.grappim.taigamobile.uikit.theme.kanbanBoardTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.DropdownSelector
import com.grappim.taigamobile.uikit.widgets.button.PlusButtonWidget
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.roundToInt

private val cellOuterPadding = 8.dp
private val cellPadding = 8.dp
private val cellWidth = 280.dp

@Composable
fun KanbanBoardWidget(
    state: KanbanState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    navigateToStory: (id: Long, ref: Long) -> Unit = { _, _ -> },
    navigateToCreateTask: (statusId: Long, swimlaneId: Long?) -> Unit = { _, _ -> },
    onMoveStory: (
        storyId: Long,
        newStatusId: Long,
        swimlaneId: Long?,
        beforeStoryId: Long?,
        afterStoryId: Long?
    ) -> Unit = { _, _, _, _, _ -> }
) {
    val dragDropState =
        rememberMultiColumnDragDropState<KanbanUserStory> { item, targetColumnId, beforeItemKey, afterItemKey ->
            onMoveStory(
                item.userStory.id,
                targetColumnId as Long,
                state.selectedSwimlane?.id,
                beforeItemKey as? Long,
                afterItemKey as? Long
            )
        }

    val backgroundCellColor = MaterialTheme.colorScheme.surfaceColorAtElevation(kanbanBoardTonalElevation)

    DragDropContainer(
        state = dragDropState,
        modifier = modifier,
        dragOverlay = { item, offset ->
            DraggedStoryOverlay(
                story = item,
                offset = offset,
                cellWidth = cellWidth
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {
            if (state.swimlanes.isNotEmpty()) {
                SwimlaneSelector(
                    swimlanes = state.swimlanes,
                    selectedSwimlane = state.selectedSwimlane,
                    onSelectSwimlane = state.onSelectSwimlane,
                    isOffline = isOffline
                )
            }

            Row(
                Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.width(cellPadding))

                state.statuses.forEach { status ->
                    val statusStories = state.storiesByStatus[status].orEmpty().toImmutableList()

                    KanbanColumn(
                        isOffline = isOffline,
                        state = dragDropState,
                        status = status,
                        stories = statusStories,
                        cellWidth = cellWidth,
                        cellOuterPadding = cellOuterPadding,
                        cellPadding = cellPadding,
                        backgroundCellColor = backgroundCellColor,
                        canAddUserStory = state.canAddUserStory,
                        onAddClick = { navigateToCreateTask(status.id, state.selectedSwimlane?.id) },
                        onStoryClick = { story -> navigateToStory(story.userStory.id, story.userStory.ref) }
                    )
                }
            }
        }
    }
}

@Composable
private fun swimlaneDisplayName(swimlane: Swimlane?): String = when {
    swimlane == null -> stringResource(RString.no_name)
    swimlane.isUnclassified -> stringResource(RString.unclassifed)
    else -> swimlane.name
}

@Composable
private fun SwimlaneSelector(
    swimlanes: ImmutableList<Swimlane>,
    selectedSwimlane: Swimlane?,
    onSelectSwimlane: (Swimlane?) -> Unit,
    isOffline: Boolean
) {
    Row(
        modifier = Modifier.padding(cellOuterPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(RString.swimlane_title),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.width(8.dp))

        DropdownSelector(
            canModify = true,
            isOffline = isOffline,
            items = swimlanes,
            selectedItem = selectedSwimlane,
            onItemSelect = onSelectSwimlane,
            itemContent = {
                Text(
                    text = swimlaneDisplayName(it),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (it != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            },
            selectedItemContent = {
                Text(
                    text = swimlaneDisplayName(it),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

@Composable
private fun KanbanColumn(
    state: MultiColumnDragDropState<KanbanUserStory>,
    status: Statuses,
    isOffline: Boolean,
    stories: ImmutableList<KanbanUserStory>,
    cellWidth: Dp,
    cellOuterPadding: Dp,
    cellPadding: Dp,
    backgroundCellColor: Color,
    canAddUserStory: Boolean,
    onAddClick: () -> Unit,
    onStoryClick: (KanbanUserStory) -> Unit
) {
    val isTargetColumn = state.isTargetColumn(status.id)
    val targetIndex = state.getTargetIndexForColumn(status.id)

    DragDropColumn(
        state = state,
        columnId = status.id,
        itemCount = stories.size
    ) {
        Column {
            Header(
                text = status.name,
                storiesCount = stories.size,
                cellWidth = cellWidth,
                cellOuterPadding = cellOuterPadding,
                stripeColor = status.color.toColor(),
                backgroundColor = backgroundCellColor,
                canAddUserStory = canAddUserStory,
                isDropTarget = isTargetColumn,
                onAddClick = onAddClick,
                isOffline = isOffline
            )

            LazyColumn(
                Modifier
                    .fillMaxHeight()
                    .width(cellWidth)
                    .background(
                        if (isTargetColumn) {
                            backgroundCellColor.copy(alpha = 0.8f)
                        } else {
                            backgroundCellColor
                        }
                    )
                    .padding(cellPadding)
            ) {
                itemsIndexed(
                    items = stories,
                    key = { _, story -> story.userStory.id }
                ) { index, kanbanStory ->
                    DropIndicator(visible = targetIndex == index) {
                        DefaultDropIndicator()
                    }

                    DraggableItem(
                        state = state,
                        item = kanbanStory,
                        itemKey = kanbanStory.userStory.id,
                        columnId = status.id,
                        index = index,
                        enabled = !isOffline
                    ) { isDragging ->
                        StoryItemContent(
                            kanbanUserStory = kanbanStory,
                            onClick = { onStoryClick(kanbanStory) },
                            enabled = !isDragging
                        )
                    }
                }

                if (targetIndex == stories.size) {
                    item {
                        DefaultDropIndicator()
                    }
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

@Composable
private fun DefaultDropIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.small
            )
    )
}

@Composable
private fun DraggedStoryOverlay(story: KanbanUserStory, offset: Offset, cellWidth: Dp) {
    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .width(cellWidth - 16.dp)
            .shadow(8.dp, MaterialTheme.shapes.small)
            .graphicsLayer {
                alpha = 0.9f
                rotationZ = 2f
            }
    ) {
        StoryItemContent(
            kanbanUserStory = story,
            onClick = {},
            enabled = false
        )
    }
}

@Composable
private fun Header(
    text: String,
    isOffline: Boolean,
    storiesCount: Int,
    cellWidth: Dp,
    cellOuterPadding: Dp,
    stripeColor: Color,
    backgroundColor: Color,
    canAddUserStory: Boolean,
    isDropTarget: Boolean,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(end = cellOuterPadding, bottom = cellOuterPadding)
            .width(cellWidth)
            .background(
                color = if (isDropTarget) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    backgroundColor
                },
                shape = MaterialTheme.shapes.small.copy(
                    bottomStart = CornerSize(0.dp),
                    bottomEnd = CornerSize(0.dp)
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val textStyle = MaterialTheme.typography.titleMedium

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.8f, fill = false)
        ) {
            Spacer(
                Modifier
                    .padding(start = 10.dp)
                    .size(
                        width = 10.dp,
                        height = with(LocalDensity.current) { textStyle.fontSize.toDp() + 2.dp }
                    )
                    .background(stripeColor)
            )

            Text(
                text = stringResource(RString.status_with_number_template).format(
                    text.uppercase(),
                    storiesCount
                ),
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }

        if (canAddUserStory) {
            PlusButtonWidget(
                isOffline = isOffline,
                tint = MaterialTheme.colorScheme.outline,
                onClick = onAddClick,
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}

@Composable
private fun StoryItemContent(kanbanUserStory: KanbanUserStory, onClick: () -> Unit, enabled: Boolean) {
    val story = kanbanUserStory.userStory
    val assignees = kanbanUserStory.assignees

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.small,
        shadowElevation = cardShadowElevation,
        onClick = onClick,
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            story.userStoryEpics.forEach {
                val textStyle = MaterialTheme.typography.bodySmall
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(
                        Modifier
                            .size(with(LocalDensity.current) { textStyle.fontSize.toDp() })
                            .background(it.color.toColor(), CircleShape)
                    )

                    Spacer(Modifier.width(4.dp))

                    Text(
                        text = it.title,
                        style = textStyle
                    )
                }

                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(4.dp))

            CommonTaskTitle(
                ref = story.ref,
                title = story.title,
                isInactive = story.isClosed,
                tags = story.tags,
                isBlocked = story.blockedNote != null
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                assignees.forEach {
                    AsyncImage(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .weight(0.2f, fill = false),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(RDrawable.default_avatar),
                        error = painterResource(RDrawable.default_avatar),
                        model = it.avatarUrl
                    )
                }
            }
        }
    }
}

private fun previewUserStory(
    id: Long = 1L,
    ref: Long = 100L,
    title: String = "Implement login feature",
    isClosed: Boolean = false,
    swimlane: Long? = null,
    epics: ImmutableList<UserStoryEpic> = persistentListOf(),
    tags: ImmutableList<Tag> = persistentListOf()
) = UserStory(
    id = id,
    version = 1L,
    createdDateTime = java.time.LocalDateTime.now(),
    title = title,
    ref = ref,
    status = Status(color = "#70728F", id = 1L, name = "New"),
    project = ProjectExtraInfo(id = 1L, name = "Project", slug = "project", logoSmallUrl = null),
    isClosed = isClosed,
    description = "",
    milestone = null,
    creatorId = 1L,
    assignedUserIds = emptyList(),
    watcherUserIds = emptyList(),
    tags = tags,
    dueDate = null,
    dueDateStatus = null,
    copyLinkUrl = "",
    userStoryEpics = epics,
    swimlane = swimlane,
    kanbanOrder = 1L
)

private fun previewKanbanUserStory(
    id: Long = 1L,
    ref: Long = 100L,
    title: String = "Implement login feature",
    assignees: ImmutableList<TeamMember> = persistentListOf()
) = KanbanUserStory(
    userStory = previewUserStory(id = id, ref = ref, title = title),
    assignees = assignees
)

private val previewNewStatus: Statuses = Status(color = "#70728F", id = 1L, name = "New")
private val previewReadyStatus: Statuses = Status(color = "#E47C40", id = 2L, name = "Ready")
private val previewInProgressStatus: Statuses = Status(color = "#E4CE40", id = 3L, name = "In Progress")
private val previewDoneStatus: Statuses = Status(color = "#A8E440", id = 4L, name = "Done")

private fun previewStatuses(): ImmutableList<Statuses> = persistentListOf(
    previewNewStatus,
    previewReadyStatus,
    previewInProgressStatus,
    previewDoneStatus
)

private fun previewStoriesByStatus(): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> = persistentMapOf(
    previewNewStatus to persistentListOf(
        previewKanbanUserStory(id = 1L, ref = 101L, title = "Setup project structure"),
        previewKanbanUserStory(id = 2L, ref = 102L, title = "Configure CI/CD pipeline")
    ),
    previewReadyStatus to persistentListOf(
        previewKanbanUserStory(id = 3L, ref = 103L, title = "Design database schema")
    ),
    previewInProgressStatus to persistentListOf(
        previewKanbanUserStory(id = 4L, ref = 104L, title = "Implement user authentication"),
        previewKanbanUserStory(id = 5L, ref = 105L, title = "Create REST API endpoints"),
        previewKanbanUserStory(id = 6L, ref = 106L, title = "Build dashboard UI")
    ),
    previewDoneStatus to persistentListOf(
        previewKanbanUserStory(id = 7L, ref = 107L, title = "Write documentation")
    )
)

private fun previewKanbanState() = KanbanState(
    statuses = previewStatuses(),
    swimlanes = persistentListOf(),
    storiesByStatus = previewStoriesByStatus(),
    onRefresh = {},
    onSelectSwimlane = {},
    canAddUserStory = true
)

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun KanbanBoardWidgetPreview() {
    TaigaMobileTheme {
        KanbanBoardWidget(state = previewKanbanState(), isOffline = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun StoryItemContentPreview() {
    TaigaMobileTheme {
        StoryItemContent(
            kanbanUserStory = KanbanUserStory(
                userStory = previewUserStory(
                    title = "Implement user authentication with OAuth2",
                    epics = persistentListOf(
                        UserStoryEpic(id = 1L, title = "Security", ref = 10L, color = "#E44057")
                    ),
                    tags = persistentListOf(
                        Tag(color = "#4285f4", name = "backend"),
                        Tag(color = "#34a853", name = "priority")
                    )
                ),
                assignees = persistentListOf(
                    TeamMember(id = 1L, avatarUrl = null, name = "John", role = "Dev", username = "john"),
                    TeamMember(id = 2L, avatarUrl = null, name = "Jane", role = "Dev", username = "jane")
                )
            ),
            onClick = {},
            enabled = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {
    TaigaMobileTheme {
        Header(
            text = "In Progress",
            storiesCount = 5,
            cellWidth = 280.dp,
            cellOuterPadding = 8.dp,
            stripeColor = Color(0xFFE4CE40),
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(kanbanBoardTonalElevation),
            canAddUserStory = true,
            isDropTarget = false,
            onAddClick = {},
            isOffline = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HeaderDropTargetPreview() {
    TaigaMobileTheme {
        Header(
            text = "Ready",
            storiesCount = 3,
            cellWidth = 280.dp,
            cellOuterPadding = 8.dp,
            stripeColor = Color(0xFFE47C40),
            backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(kanbanBoardTonalElevation),
            canAddUserStory = true,
            isDropTarget = true,
            onAddClick = {},
            isOffline = false

        )
    }
}

@Preview(showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun KanbanBoardWidgetWithSwimlanesPreview() {
    TaigaMobileTheme {
        KanbanBoardWidget(
            state = previewKanbanState().copy(
                swimlanes = persistentListOf(
                    Swimlane(id = 1L, name = "Backend", order = 1L),
                    Swimlane(id = 2L, name = "Frontend", order = 2L),
                    Swimlane(id = 3L, name = "Mobile", order = 3L)
                ),
                selectedSwimlane = Swimlane(id = 1L, name = "Backend", order = 1L)
            ),
            isOffline = false
        )
    }
}
