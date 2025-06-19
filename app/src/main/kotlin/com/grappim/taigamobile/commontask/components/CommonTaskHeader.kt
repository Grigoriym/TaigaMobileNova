@file:OptIn(ExperimentalLayoutApi::class)

package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.commontask.EditActions
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.badge.ClickableBadge
import com.grappim.taigamobile.uikit.widgets.picker.ColorPicker
import com.grappim.taigamobile.utils.ui.toColor
import com.grappim.taigamobile.utils.ui.toHex

@Suppress("FunctionName")
fun LazyListScope.CommonTaskHeader(
    commonTask: CommonTaskExtended,
    editActions: EditActions,
    showStatusSelector: () -> Unit,
    showSprintSelector: () -> Unit,
    showTypeSelector: () -> Unit,
    showSeveritySelector: () -> Unit,
    showPrioritySelector: () -> Unit,
    showSwimlaneSelector: () -> Unit
) {
    val badgesPadding = 8.dp

    item {
        commonTask.blockedNote?.trim()?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(taigaRed, MaterialTheme.shapes.medium)
                    .padding(8.dp)
            ) {
                val space = 4.dp

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(RDrawable.ic_lock),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.width(space))

                    Text(stringResource(RString.blocked))
                }

                if (it.isNotEmpty()) {
                    Spacer(Modifier.width(space))

                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(badgesPadding))
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(badgesPadding),
            verticalArrangement = Arrangement.spacedBy(badgesPadding)
//            crossAxisAlignment = FlowCrossAxisAlignment.Center,
        ) {
            // epic color
            if (commonTask.taskType == CommonTaskType.Epic) {
                ColorPicker(
                    size = 32.dp,
                    color = commonTask.color.orEmpty().toColor(),
                    onColorPick = { editActions.editEpicColor.select(it.toHex()) }
                )
            }

            // status
            ClickableBadge(
                text = commonTask.status.name,
                colorHex = commonTask.status.color,
                onClick = { showStatusSelector() },
                isLoading = editActions.editStatus.isLoading
            )

            // sprint
            if (commonTask.taskType != CommonTaskType.Epic) {
                ClickableBadge(
                    text = commonTask.sprint?.name ?: stringResource(RString.no_sprint),
                    color = commonTask.sprint?.let { MaterialTheme.colorScheme.primary }
                        ?: MaterialTheme.colorScheme.outline,
                    onClick = { showSprintSelector() },
                    isLoading = editActions.editSprint.isLoading,
                    isClickable = commonTask.taskType != CommonTaskType.Task
                )
            }

            // swimlane
            if (commonTask.taskType == CommonTaskType.UserStory) {
                ClickableBadge(
                    text = commonTask.swimlane?.name ?: stringResource(RString.unclassifed),
                    color = commonTask.swimlane?.let { MaterialTheme.colorScheme.primary }
                        ?: MaterialTheme.colorScheme.outline,
                    isLoading = editActions.editSwimlane.isLoading,
                    onClick = { showSwimlaneSelector() }
                )
            }

            if (commonTask.taskType == CommonTaskType.Issue) {
                // type
                ClickableBadge(
                    text = commonTask.type!!.name,
                    colorHex = commonTask.type!!.color,
                    onClick = { showTypeSelector() },
                    isLoading = editActions.editType.isLoading
                )

                // severity
                ClickableBadge(
                    text = commonTask.severity!!.name,
                    colorHex = commonTask.severity!!.color,
                    onClick = { showSeveritySelector() },
                    isLoading = editActions.editSeverity.isLoading
                )

                // priority
                ClickableBadge(
                    text = commonTask.priority!!.name,
                    colorHex = commonTask.priority!!.color,
                    onClick = { showPrioritySelector() },
                    isLoading = editActions.editPriority.isLoading
                )
            }
        }

        // title
        Text(
            text = commonTask.title,
            style = MaterialTheme.typography.headlineSmall.let {
                if (commonTask.isClosed) {
                    it.merge(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.outline,
                            textDecoration = TextDecoration.LineThrough
                        )
                    )
                } else {
                    it
                }
            }
        )

        Spacer(Modifier.height(4.dp))
    }
}
