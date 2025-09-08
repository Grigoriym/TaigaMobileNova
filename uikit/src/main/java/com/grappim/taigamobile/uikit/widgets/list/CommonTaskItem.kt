package com.grappim.taigamobile.uikit.widgets.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.navigation.NavigateToTask
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.ui.toColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Single task item
 */
@Composable
fun CommonTaskItem(
    commonTask: CommonTask,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = mainHorizontalScreenPadding,
    verticalPadding: Dp = 8.dp,
    showExtendedInfo: Boolean = false,
    navigateToTask: NavigateToTask = { _, _, _ -> }
) = ContainerBoxWidget(
    modifier = modifier,
    horizontalPadding = horizontalPadding,
    verticalPadding = verticalPadding,
    onClick = { navigateToTask(commonTask.id, commonTask.taskType, commonTask.ref) }
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (showExtendedInfo) {
            Text(commonTask.projectDTOInfo.name)

            Text(
                text = stringResource(
                    when (commonTask.taskType) {
                        CommonTaskType.UserStory -> RString.userstory
                        CommonTaskType.Task -> RString.task
                        CommonTaskType.Epic -> RString.epic
                        CommonTaskType.Issue -> RString.issue
                    }
                ).uppercase(),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = commonTask.statusOld.name,
                color = commonTask.statusOld.color.toColor(),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = commonTask.createdDate.format(dateTimeFormatter),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        CommonTaskTitle(
            ref = commonTask.ref,
            title = commonTask.title,
            indicatorColorsHex = commonTask.colors,
            isInactive = commonTask.isClosed,
            tags = commonTask.tags,
            isBlocked = commonTask.blockedNote != null
        )

        Text(
            text = commonTask.assignee?.fullName?.let {
                stringResource(RString.assignee_pattern)
                    .format(it)
            } ?: stringResource(RString.unassigned),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommonTaskItemPreview() = TaigaMobileTheme {
    CommonTaskItem(
        CommonTask(
            id = 0L,
            createdDate = LocalDateTime.now(),
            title = "Very cool story",
            ref = 100,
            statusOld = StatusOld(
                id = 0L,
                name = "In progress",
                color = "#729fcf",
                type = StatusType.Status
            ),
            assignee = null,
            projectDTOInfo = ProjectDTO(0, "Name", "slug"),
            taskType = CommonTaskType.UserStory,
            isClosed = false,
            blockedNote = "Block reason"
        ),
        showExtendedInfo = true
    )
}
