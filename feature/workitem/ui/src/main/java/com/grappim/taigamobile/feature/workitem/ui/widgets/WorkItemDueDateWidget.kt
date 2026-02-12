package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import java.time.LocalDate

@Composable
fun WorkItemDueDateWidget(
    canModify: Boolean,
    isOffline: Boolean,
    dueDateState: WorkItemDueDateState,
    setDueDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = if (dueDateState.isDueDateLoading) {
        MaterialTheme.colorScheme.primary
    } else {
        dueDateState.backgroundColor.asColor()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(RString.due_date),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(4.dp)

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canModify && !dueDateState.isDueDateLoading && !isOffline) {
                        Modifier.clickable {
                            dueDateState.setDueDateDatePickerVisibility(true)
                        }
                    } else {
                        Modifier
                    }
                ),
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (dueDateState.isDueDateLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = accentColor
                        )
                    } else {
                        Icon(
                            painter = painterResource(RDrawable.ic_clock),
                            contentDescription = null,
                            tint = accentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = dueDateState.dueDateText.asString(context),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (dueDateState.dueDate != null && canModify) {
                    IconButton(
                        enabled = !isOffline,
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

@Composable
@PreviewTaigaDarkLight
private fun WorkItemDueDateWidgetPreview() {
    TaigaMobilePreviewTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(
                dueDateStatus = DueDateStatus.DueSoon,
                dueDate = LocalDate.now(),
                dueDateText = NativeText.Simple("24.01.23"),
                backgroundColor = StaticColor(
                    taigaOrange
                )
            ),
            setDueDate = {},
            isOffline = false,
            canModify = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemDueDateWidgetPastDuePreview() {
    TaigaMobilePreviewTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(
                dueDateStatus = DueDateStatus.PastDue,
                dueDate = LocalDate.now(),
                dueDateText = NativeText.Simple("24.01.23")
            ),
            setDueDate = {},
            isOffline = false,
            canModify = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemDueDateWidgetNoLongerApplicablePreview() {
    TaigaMobilePreviewTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(
                dueDateStatus = DueDateStatus.NoLongerApplicable,
                dueDate = LocalDate.now(),
                dueDateText = NativeText.Simple("24.01.23"),
                backgroundColor = StaticColor(
                    taigaRed
                )
            ),
            setDueDate = {},
            isOffline = false,
            canModify = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemDueDateWidgetNoDueDatePreview() {
    TaigaMobilePreviewTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(
                dueDateStatus = null,
                dueDate = null
            ),
            setDueDate = {},
            isOffline = false,
            canModify = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemDueDateWidgetLoadingPreview() {
    TaigaMobilePreviewTheme {
        WorkItemDueDateWidget(
            dueDateState = WorkItemDueDateState(
                dueDateStatus = DueDateStatus.NotSet,
                dueDate = LocalDate.now(),
                dueDateText = NativeText.Simple("24.01.23")
            ),
            setDueDate = {},
            isOffline = false,
            canModify = true
        )
    }
}
