package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.utils.ui.textColor
import java.time.LocalDate

@Composable
fun WorkItemDueDateWidget(
    dueDateState: WorkItemDueDateState,
    dueDateStatus: DueDateStatus?,
    dueDate: LocalDate?,
    setDueDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(RString.due_date),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(8.dp)

        Box(
            modifier = Modifier
                .height(42.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = when (dueDateStatus) {
                                DueDateStatus.NotSet, DueDateStatus.NoLongerApplicable, null ->
                                    dueDate?.let { MaterialTheme.colorScheme.surface }
                                        ?: MaterialTheme.colorScheme.primary

                                DueDateStatus.Set -> taigaGreenPositive
                                DueDateStatus.DueSoon -> taigaOrange
                                DueDateStatus.PastDue -> taigaRed
                            }.takeUnless { dueDateState.isDueDateLoading } ?: MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(4.dp)
                ) {
                    if (dueDateState.isDueDateLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(2.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            painter = painterResource(RDrawable.ic_clock),
                            contentDescription = null,
                            tint = dueDate?.let { MaterialTheme.colorScheme.onSurface }
                                ?: MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small.copy(
                                topStart = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp)
                            )
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = dueDateState.dueDateText.asString(context),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.clickable {
                            dueDateState.setDueDateDatePickerVisibility(true)
                        },
                        color = (
                            dueDate?.let { MaterialTheme.colorScheme.onSurface }
                                ?: MaterialTheme.colorScheme.onPrimary
                            ).textColor()
                    )

                    if (dueDate != null) {
                        Spacer(Modifier.width(4.dp))

                        IconButton(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape),
                            onClick = { setDueDate(null) }
                        ) {
                            Icon(
                                painter = painterResource(RDrawable.ic_remove),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@PreviewDarkLight
private fun WorkItemDueDateWidgetPreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(),
            dueDateStatus = DueDateStatus.DueSoon,
            dueDate = LocalDate.now(),
            setDueDate = {}
        )
    }
}

@Composable
@PreviewDarkLight
private fun WorkItemDueDateWidgetPastDuePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(),
            dueDateStatus = DueDateStatus.PastDue,
            dueDate = LocalDate.now(),
            setDueDate = {}
        )
    }
}

@Composable
@PreviewDarkLight
private fun WorkItemDueDateWidgetNoLongerApplicablePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(),
            dueDateStatus = DueDateStatus.NoLongerApplicable,
            dueDate = LocalDate.now(),
            setDueDate = {}
        )
    }
}

@Composable
@PreviewDarkLight
private fun WorkItemDueDateWidgetNoDueDatePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(),
            dueDateStatus = null,
            dueDate = null,
            setDueDate = {}
        )
    }
}

@Composable
@PreviewDarkLight
private fun WorkItemDueDateWidgetLoadingPreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(),
            dueDateStatus = DueDateStatus.NotSet,
            dueDate = LocalDate.now(),
            setDueDate = {}
        )
    }
}
