package com.grappim.taigamobile.feature.sprint.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.navigation.NavigateToTask
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.cardShadowElevation
import com.grappim.taigamobile.uikit.theme.kanbanBoardTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.utils.clickableUnindicated
import com.grappim.taigamobile.uikit.widgets.button.PlusButton
import com.grappim.taigamobile.uikit.widgets.list.CommonTaskItem
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal
import com.grappim.taigamobile.utils.ui.toColor
import java.time.LocalDateTime

@Composable
internal fun SprintKanbanWidget(
    statuses: List<Status>,
    storiesWithTasks: Map<CommonTask, List<CommonTask>>,
    modifier: Modifier = Modifier,
    storylessTasks: List<CommonTask> = emptyList(),
    issues: List<CommonTask> = emptyList(),
    navigateToTask: NavigateToTask = { _, _, _ -> },
    navigateToCreateTask: (type: CommonTaskType, parentId: Long?) -> Unit = { _, _ -> }
) {
    Column(
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        val cellOuterPadding = 8.dp
        val cellPadding = 8.dp
        val cellWidth = 280.dp
        val userStoryHeadingWidth = cellWidth - 20.dp
        val minCellHeight = 80.dp
        val backgroundCellColor =
            MaterialTheme.colorScheme.surfaceColorAtElevationInternal(kanbanBoardTonalElevation)
        val screenWidth = LocalConfiguration.current.screenWidthDp.dp
        val totalWidth =
            cellWidth * statuses.size + userStoryHeadingWidth + cellPadding * statuses.size

        Row(Modifier.padding(start = cellPadding, top = cellPadding)) {
            Header(
                text = stringResource(RString.userstory),
                cellWidth = userStoryHeadingWidth,
                cellPadding = cellPadding,
                stripeColor = backgroundCellColor,
                backgroundColor = Color.Transparent
            )

            statuses.forEach {
                Header(
                    text = it.name,
                    cellWidth = cellWidth,
                    cellPadding = cellPadding,
                    stripeColor = it.color.toColor(),
                    backgroundColor = backgroundCellColor
                )
            }
        }

        LazyColumn {
            // stories with tasks
            storiesWithTasks.forEach { (story, tasks) ->
                item {
                    Row(
                        Modifier
                            .height(IntrinsicSize.Max)
                            .padding(start = cellPadding)
                    ) {
                        UserStoryItem(
                            cellPadding = cellPadding,
                            cellWidth = userStoryHeadingWidth,
                            minCellHeight = minCellHeight,
                            userStory = story,
                            onAddClick = { navigateToCreateTask(CommonTaskType.Task, story.id) },
                            onUserStoryClick = {
                                navigateToTask(
                                    story.id,
                                    story.taskType,
                                    story.ref
                                )
                            }
                        )

                        statuses.forEach { status ->
                            Cell(
                                cellWidth = cellWidth,
                                cellOuterPadding = cellOuterPadding,
                                cellPadding = cellPadding,
                                backgroundCellColor = backgroundCellColor
                            ) {
                                tasks.filter { it.status == status }.forEach {
                                    TaskItem(
                                        task = it,
                                        onTaskClick = { navigateToTask(it.id, it.taskType, it.ref) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // storyless tasks
            item {
                Row(
                    Modifier
                        .height(IntrinsicSize.Max)
                        .padding(start = cellPadding)
                ) {
                    CategoryItem(
                        titleId = RString.tasks_without_story,
                        cellPadding = cellPadding,
                        cellWidth = userStoryHeadingWidth,
                        minCellHeight = minCellHeight,
                        onAddClick = { navigateToCreateTask(CommonTaskType.Task, null) }
                    )

                    statuses.forEach { status ->
                        Cell(
                            cellWidth = cellWidth,
                            cellOuterPadding = cellOuterPadding,
                            cellPadding = cellPadding,
                            backgroundCellColor = backgroundCellColor
                        ) {
                            storylessTasks.filter { it.status == status }.forEach {
                                TaskItem(
                                    task = it,
                                    onTaskClick = { navigateToTask(it.id, it.taskType, it.ref) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(
                    Modifier
                        .height(4.dp)
                        .padding(start = cellPadding)
                        .width(totalWidth)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                )
            }

            // issues
            item {
                IssueHeader(
                    width = screenWidth,
                    padding = cellPadding,
                    backgroundColor = backgroundCellColor,
                    onAddClick = { navigateToCreateTask(CommonTaskType.Issue, null) }
                )
            }

            items(issues) {
                Row(Modifier.width(totalWidth)) {
                    Row(
                        Modifier
                            .width(screenWidth)
                            .padding(vertical = 4.dp)
                            .background(backgroundCellColor)
                    ) {
                        CommonTaskItem(
                            commonTask = it,
                            navigateToTask = navigateToTask
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun Header(
    text: String,
    cellWidth: Dp,
    cellPadding: Dp,
    stripeColor: Color,
    backgroundColor: Color
) = Column(
    modifier = Modifier
        .padding(end = cellPadding, bottom = cellPadding)
        .width(cellWidth)
        .background(
            color = backgroundColor,
            shape = MaterialTheme.shapes.small.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            )
        ),
    horizontalAlignment = Alignment.Start
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(8.dp)
    )

    Spacer(
        Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(stripeColor)
    )
}

@Composable
private fun IssueHeader(width: Dp, padding: Dp, backgroundColor: Color, onAddClick: () -> Unit) =
    Row(
        modifier = Modifier
            .width(width)
            .padding(padding)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(RString.sprint_issues).uppercase(),
            modifier = Modifier.weight(0.8f, fill = false)
        )

        PlusButton(
            tint = MaterialTheme.colorScheme.outline,
            onClick = onAddClick,
            modifier = Modifier.weight(0.2f)
        )
    }

@Composable
private fun UserStoryItem(
    cellPadding: Dp,
    cellWidth: Dp,
    minCellHeight: Dp,
    userStory: CommonTask,
    onAddClick: () -> Unit,
    onUserStoryClick: () -> Unit
) = Row(
    modifier = Modifier
        .padding(end = cellPadding, bottom = cellPadding)
        .width(cellWidth)
        .heightIn(min = minCellHeight),
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(0.8f, fill = false)
    ) {
        CommonTaskTitle(
            ref = userStory.ref,
            title = userStory.title,
            indicatorColorsHex = userStory.colors,
            isInactive = userStory.isClosed,
            tags = userStory.tags,
            isBlocked = userStory.blockedNote != null,
            modifier = Modifier
                .padding(top = 4.dp)
                .clickableUnindicated(onClick = onUserStoryClick)
        )

        Text(
            text = userStory.status.name,
            color = userStory.status.color.toColor(),
            style = MaterialTheme.typography.bodyMedium
        )
    }

    PlusButton(
        tint = MaterialTheme.colorScheme.outline,
        onClick = onAddClick,
        modifier = Modifier.weight(0.2f)
    )
}

@Composable
private fun CategoryItem(
    @StringRes titleId: Int,
    cellPadding: Dp,
    cellWidth: Dp,
    minCellHeight: Dp,
    onAddClick: () -> Unit
) = Column(
    modifier = Modifier
        .padding(end = cellPadding, bottom = cellPadding)
        .width(cellWidth)
        .heightIn(min = minCellHeight)
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(titleId),
            modifier = Modifier
                .weight(0.8f, fill = false)
                .padding(top = 4.dp)
        )

        PlusButton(
            tint = MaterialTheme.colorScheme.outline,
            onClick = onAddClick,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@Composable
private fun Cell(
    cellWidth: Dp,
    cellOuterPadding: Dp,
    cellPadding: Dp,
    backgroundCellColor: Color,
    content: @Composable ColumnScope.() -> Unit
) = Column(
    modifier = Modifier
        .fillMaxHeight()
        .padding(end = cellOuterPadding, bottom = cellOuterPadding)
        .width(cellWidth)
        .background(backgroundCellColor)
        .padding(cellPadding),
    content = content
)

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun TaskItem(task: CommonTask, onTaskClick: () -> Unit) = Surface(
    modifier = Modifier
        .fillMaxWidth()
        .padding(4.dp),
    shape = MaterialTheme.shapes.small,
    shadowElevation = cardShadowElevation
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onTaskClick,
                indication = ripple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(12.dp)
    ) {
        Column(Modifier.weight(0.8f, fill = false)) {
            CommonTaskTitle(
                ref = task.ref,
                title = task.title,
                indicatorColorsHex = task.colors,
                isInactive = task.isClosed,
                tags = task.tags,
                isBlocked = task.blockedNote != null
            )

            Text(
                text = task.assignee?.fullName?.let {
                    stringResource(RString.assignee_pattern).format(it)
                } ?: stringResource(RString.unassigned),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        task.assignee?.let {
            AsyncImage(
                modifier = Modifier
                    .size(32.dp)
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

@Preview(showBackground = true)
@Composable
private fun SprintKanbanPreview() = TaigaMobileTheme {
    SprintKanbanWidget(
        statuses = listOf(
            Status(
                id = 0,
                name = "New",
                color = "#70728F",
                type = StatusType.Status
            ),
            Status(
                id = 1,
                name = "In progress",
                color = "#E47C40",
                type = StatusType.Status
            ),
            Status(
                id = 1,
                name = "Done",
                color = "#A8E440",
                type = StatusType.Status
            ),
            Status(
                id = 1,
                name = "Archived",
                color = "#A9AABC",
                type = StatusType.Status
            )
        ),
        storiesWithTasks = List(5) {
            CommonTask(
                id = it.toLong(),
                createdDate = LocalDateTime.now(),
                title = "Very cool story",
                ref = 100,
                status = Status(
                    id = 1,
                    name = "In progress",
                    color = "#E47C40",
                    type = StatusType.Status
                ),
                assignee = User(
                    id = it.toLong(),
                    fullName = "Name Name",
                    photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                    bigPhoto = null,
                    username = "username"
                ),
                projectInfo = Project(0, "", ""),
                taskType = CommonTaskType.UserStory,
                isClosed = false
            ) to listOf(
                CommonTask(
                    id = it.toLong(),
                    createdDate = LocalDateTime.now(),
                    title = "Very cool story Very cool story Very cool story",
                    ref = 100,
                    status = Status(
                        id = 1,
                        name = "In progress",
                        color = "#E47C40",
                        type = StatusType.Status
                    ),
                    assignee = User(
                        id = it.toLong(),
                        fullName = "Name Name",
                        photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                        bigPhoto = null,
                        username = "username"
                    ),
                    projectInfo = Project(0, "", ""),
                    taskType = CommonTaskType.Task,
                    isClosed = false
                ),
                CommonTask(
                    id = it.toLong() + 2,
                    createdDate = LocalDateTime.now(),
                    title = "Very cool story",
                    ref = 100,
                    status = Status(
                        id = 1,
                        name = "In progress",
                        color = "#E47C40",
                        type = StatusType.Status
                    ),
                    assignee = User(
                        id = it.toLong(),
                        fullName = "Name Name",
                        photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                        bigPhoto = null,
                        username = "username"
                    ),
                    projectInfo = Project(0, "", ""),
                    taskType = CommonTaskType.Task,
                    isClosed = false
                ),
                CommonTask(
                    id = it.toLong() + 2,
                    createdDate = LocalDateTime.now(),
                    title = "Very cool story",
                    ref = 100,
                    status = Status(
                        id = 0,
                        name = "New",
                        color = "#70728F",
                        type = StatusType.Status
                    ),
                    assignee = User(
                        id = it.toLong(),
                        fullName = "Name Name",
                        photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                        bigPhoto = null,
                        username = "username"
                    ),
                    projectInfo = Project(0, "", ""),
                    taskType = CommonTaskType.Task,
                    isClosed = false
                )
            )
        }.toMap(),
        issues = List(10) {
            CommonTask(
                id = it.toLong() + 1,
                createdDate = LocalDateTime.now(),
                title = "Very cool story",
                ref = 100,
                status = Status(
                    id = 0,
                    name = "New",
                    color = "#70728F",
                    type = StatusType.Status
                ),
                assignee = User(
                    id = it.toLong(),
                    fullName = "Name Name",
                    photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                    bigPhoto = null,
                    username = "username"
                ),
                projectInfo = Project(0, "", ""),
                taskType = CommonTaskType.Issue,
                isClosed = false
            )
        }
    )
}
