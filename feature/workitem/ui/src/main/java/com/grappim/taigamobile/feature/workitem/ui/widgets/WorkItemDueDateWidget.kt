package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.PreviewMulti
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.utils.ui.textColor
import java.time.LocalDate

@Composable
fun WorkItemDueDateWidget(
    dueDateText: NativeText,
    dueDateStatus: DueDateStatus?,
    isLoading: Boolean,
    dueDate: LocalDate?,
    setIsDueDatePickerVisible: (Boolean) -> Unit,
    setDueDate: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
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
                        }.takeUnless { isLoading } ?: MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            ) {
                if (isLoading) {
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
                    text = dueDateText.asString(context),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable {
                        setIsDueDatePickerVisible(true)
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

@Composable
@PreviewMulti
private fun WorkItemDueDateWidgetPreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateText = NativeText.Simple("27.02.0255"),
            dueDateStatus = DueDateStatus.DueSoon,
            isLoading = false,
            dueDate = LocalDate.now(),
            setIsDueDatePickerVisible = {},
            setDueDate = {}
        )
    }
}

@Composable
@PreviewMulti
private fun WorkItemDueDateWidgetPastDuePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateText = NativeText.Simple("27.02.0255"),
            dueDateStatus = DueDateStatus.PastDue,
            isLoading = false,
            dueDate = LocalDate.now(),
            setIsDueDatePickerVisible = {},
            setDueDate = {}
        )
    }
}

@Composable
@PreviewMulti
private fun WorkItemDueDateWidgetNoLongerApplicablePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateText = NativeText.Simple("27.02.0255"),
            dueDateStatus = DueDateStatus.NoLongerApplicable,
            isLoading = false,
            dueDate = LocalDate.now(),
            setIsDueDatePickerVisible = {},
            setDueDate = {}
        )
    }
}

@Composable
@PreviewMulti
private fun WorkItemDueDateWidgetNoDueDatePreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateText = NativeText.Simple("no date"),
            dueDateStatus = null,
            isLoading = false,
            dueDate = null,
            setIsDueDatePickerVisible = {},
            setDueDate = {}
        )
    }
}

@Composable
@PreviewMulti
private fun WorkItemDueDateWidgetLoadingPreview() {
    TaigaMobileTheme {
        WorkItemDueDateWidget(
            dueDateText = NativeText.Simple("24.25.2626"),
            dueDateStatus = DueDateStatus.NotSet,
            isLoading = true,
            dueDate = LocalDate.now(),
            setIsDueDatePickerVisible = {},
            setDueDate = {}
        )
    }
}
