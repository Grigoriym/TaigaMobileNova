package com.grappim.taigamobile.uikit.widgets.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.assignee_pattern
import com.grappim.taigamobile.strings.generated.resources.epic
import com.grappim.taigamobile.strings.generated.resources.issue
import com.grappim.taigamobile.strings.generated.resources.task
import com.grappim.taigamobile.strings.generated.resources.unassigned
import com.grappim.taigamobile.strings.generated.resources.userstory
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.widgets.text.CommonTaskTitle
import com.grappim.taigamobile.utils.formatter.datetime.platformFormatMediumDateTime
import com.grappim.taigamobile.utils.ui.formatStringKmp
import com.grappim.taigamobile.utils.ui.toColor
import org.jetbrains.compose.resources.stringResource

/**
 * Single task item
 */
@Deprecated("try to refactor it")
@Composable
fun CommonTaskItem(
    commonTask: WorkItem,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = mainHorizontalScreenPadding,
    verticalPadding: Dp = 8.dp,
    showExtendedInfo: Boolean = false,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit = { _, _, _ -> }
) = Surface(
    modifier = modifier
        .padding(horizontal = horizontalPadding, vertical = verticalPadding),
    shape = MaterialTheme.shapes.medium,
    onClick = { navigateToTask(commonTask.id, commonTask.taskType, commonTask.ref) }
) {
    val formattedDate = remember {
        platformFormatMediumDateTime(commonTask.createdDate)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        if (showExtendedInfo) {
            Text(commonTask.project.name)

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
                text = commonTask.status.name,
                color = commonTask.status.color.toColor(),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = formattedDate,
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
                    .formatStringKmp(it)
            } ?: stringResource(RString.unassigned),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
