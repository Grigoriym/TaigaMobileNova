@file:OptIn(ExperimentalLayoutApi::class)

package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.core.domain.EpicShortInfo
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.SwimlaneDTO
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.cardShadowElevation
import com.grappim.taigamobile.uikit.theme.kanbanBoardTonalElevation
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.DropdownSelector
import com.grappim.taigamobile.uikit.widgets.button.PlusButton
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal
import com.grappim.taigamobile.utils.ui.toColor
import java.time.LocalDateTime

@Composable
fun KanbanBoardWidget(
    statusOlds: List<StatusOld>,
    swimlaneDTOS: List<SwimlaneDTO?>,
    modifier: Modifier = Modifier,
    stories: List<CommonTaskExtended> = emptyList(),
    team: List<UserDTO> = emptyList(),
    selectSwimlane: (SwimlaneDTO?) -> Unit = {},
    selectedSwimlaneDTO: SwimlaneDTO? = null,
    navigateToStory: (id: Long, ref: Int) -> Unit = { _, _ -> },
    navigateToCreateTask: (statusId: Long, swimlaneId: Long?) -> Unit = { _, _ -> }
) {
    val cellOuterPadding = 8.dp
    val cellPadding = 8.dp
    val cellWidth = 280.dp
    val backgroundCellColor =
        MaterialTheme.colorScheme.surfaceColorAtElevationInternal(kanbanBoardTonalElevation)

    swimlaneDTOS.takeIf { it.isNotEmpty() }?.let {
        Row(
            modifier = modifier.padding(cellOuterPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(RString.swimlane_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.width(8.dp))

            DropdownSelector(
                items = swimlaneDTOS,
                selectedItem = selectedSwimlaneDTO,
                onItemSelect = selectSwimlane,
                itemContent = {
                    Text(
                        text = it?.name ?: stringResource(RString.unclassifed),
                        style = MaterialTheme.typography.bodyLarge,
                        color = it?.let { MaterialTheme.colorScheme.onSurface }
                            ?: MaterialTheme.colorScheme.primary
                    )
                },
                selectedItemContent = {
                    Text(
                        text = it?.name ?: stringResource(RString.unclassifed),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }
    }

    val storiesToDisplay = stories.filter { it.swimlaneDTO?.id == selectedSwimlaneDTO?.id }

    Row(
        Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.width(cellPadding))

        statusOlds.forEach { status ->
            val statusStories = storiesToDisplay.filter { it.statusOld == status }

            Column {
                Header(
                    text = status.name,
                    storiesCount = statusStories.size,
                    cellWidth = cellWidth,
                    cellOuterPadding = cellOuterPadding,
                    stripeColor = status.color.toColor(),
                    backgroundColor = backgroundCellColor,
                    onAddClick = { navigateToCreateTask(status.id, selectedSwimlaneDTO?.id) }
                )

                LazyColumn(
                    Modifier
                        .fillMaxHeight()
                        .width(cellWidth)
                        .background(backgroundCellColor)
                        .padding(cellPadding)
                ) {
                    items(statusStories) {
                        StoryItem(
                            story = it,
                            assignees = it.assignedIds.mapNotNull { id ->
                                team.find { it.actualId == id }
                            },
                            onTaskClick = { navigateToStory(it.id, it.ref) }
                        )
                    }

                    item {
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(
    text: String,
    storiesCount: Int,
    cellWidth: Dp,
    cellOuterPadding: Dp,
    stripeColor: Color,
    backgroundColor: Color,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(end = cellOuterPadding, bottom = cellOuterPadding)
            .width(cellWidth)
            .background(
                color = backgroundColor,
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

        PlusButton(
            tint = MaterialTheme.colorScheme.outline,
            onClick = onAddClick,
            modifier = Modifier.weight(0.2f)
        )
    }
}

@ExperimentalLayoutApi
@Composable
private fun StoryItem(
    story: CommonTaskExtended,
    assignees: List<UserDTO>,
    onTaskClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = MaterialTheme.shapes.small,
        shadowElevation = cardShadowElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onTaskClick,
                    indication = ripple(),
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(12.dp)
        ) {
            story.epicsShortInfo.forEach {
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

@Preview(showBackground = true)
@Composable
private fun KanbanBoardPreview() {
    TaigaMobileTheme {
        KanbanBoardWidget(
            swimlaneDTOS = listOf(
                SwimlaneDTO(0, "Name", 0),
                SwimlaneDTO(0, "Another name", 1)
            ),
            statusOlds = listOf(
                StatusOld(
                    id = 0,
                    name = "New",
                    color = "#70728F",
                    type = StatusType.Status
                ),
                StatusOld(
                    id = 1,
                    name = "In progress",
                    color = "#E47C40",
                    type = StatusType.Status
                ),
                StatusOld(
                    id = 1,
                    name = "Done",
                    color = "#A8E440",
                    type = StatusType.Status
                ),
                StatusOld(
                    id = 1,
                    name = "Archived",
                    color = "#A9AABC",
                    type = StatusType.Status
                )
            ),
            stories = List(5) {
                CommonTaskExtended(
                    id = 0,
                    statusOld = StatusOld(
                        id = 1,
                        name = "In progress",
                        color = "#E47C40",
                        type = StatusType.Status
                    ),
                    createdDateTime = LocalDateTime.now(),
                    sprint = null,
                    assignedIds = List(10) { it.toLong() },
                    watcherIds = emptyList(),
                    creatorId = 0,
                    ref = 1,
                    title = "Sample title",
                    isClosed = false,
                    description = "",
                    epicsShortInfo = List(3) { EpicShortInfo(0, "Some title", 1, "#A8E440") },
                    projectSlug = "",
                    userStoryShortInfo = null,
                    version = 0,
                    color = null,
                    type = null,
                    priority = null,
                    severity = null,
                    taskType = CommonTaskType.UserStory,
                    swimlaneDTO = null,
                    dueDate = null,
                    dueDateStatusDTO = DueDateStatusDTO.NotSet,
                    url = ""
                )
            },
            team = List(10) {
                UserDTO(
                    id = it.toLong(),
                    fullName = "Name Name",
                    photo = "https://avatars.githubusercontent.com/u/36568187?v=4",
                    bigPhoto = null,
                    username = "username"
                )
            }
        )
    }
}
