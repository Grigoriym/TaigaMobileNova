package com.grappim.taigamobile.feature.epics.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitleExpandable
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.utils.ui.textColor
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WorkItemsSectionWidget(
    workItemUIS: ImmutableList<WorkItemUI>,
    workItemsType: CommonTaskType,
    areWorkItemsExpanded: Boolean,
    setAreWorkItemsExpanded: (Boolean) -> Unit,
    goToWorkItem: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        SectionTitleExpandable(
            text = stringResource(
                when (workItemsType) {
                    CommonTaskType.UserStory -> RString.userstories
                    CommonTaskType.Task -> RString.tasks
                    CommonTaskType.Epic -> RString.epics
                    CommonTaskType.Issue -> RString.issues
                }
            ),
            isExpanded = areWorkItemsExpanded,
            onExpandClick = {
                setAreWorkItemsExpanded(!areWorkItemsExpanded)
            }
        )

        if (areWorkItemsExpanded) {
            Spacer(Modifier.height(10.dp))

            workItemUIS.forEachIndexed { index, item ->
                WorkItemItemWidget(
                    workItemUI = item,
                    goToWorkItem = goToWorkItem
                )

                if (index < workItemUIS.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkItemItemWidget(
    workItemUI: WorkItemUI,
    goToWorkItem: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    ContainerBoxWidget(
        modifier = modifier,
        onClick = {
            goToWorkItem(workItemUI.id, workItemUI.taskType, workItemUI.ref)
        }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = workItemUI.status.title.asString(context),
                    color = workItemUI.status.color.asColor(),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = workItemUI.createdDate,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = buildAnnotatedString {
                    if (workItemUI.isClosed) {
                        pushStyle(
                            SpanStyle(
                                color = MaterialTheme.colorScheme.outline,
                                textDecoration = TextDecoration.LineThrough
                            )
                        )
                    }
                    append(stringResource(RString.title_with_ref_pattern).format(workItemUI.ref, workItemUI.title))
                    if (workItemUI.isClosed) pop()

                    append(" ")

                    workItemUI.colors.forEach {
                        pushStyle(SpanStyle(color = it.toColor()))
                        append("⬤")
                        pop()
                    }
                },
                color = if (workItemUI.isBlocked) taigaRed else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )

            if (workItemUI.tags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    workItemUI.tags.forEach {
                        val bgColor = it.color

                        Chip(color = bgColor) {
                            Text(
                                text = it.name,
                                color = bgColor.textColor(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Text(
                text = workItemUI.assignee?.fullName?.let {
                    stringResource(RString.assignee_pattern)
                        .format(it)
                } ?: stringResource(RString.unassigned),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
