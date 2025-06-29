package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.EditActions
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.picker.DatePicker
import com.grappim.taigamobile.utils.ui.surfaceColorAtElevationInternal

@Suppress("FunctionName")
fun LazyListScope.CommonTaskDueDate(commonTask: CommonTaskExtended, editActions: EditActions) {
    item {
        val background = MaterialTheme.colorScheme.surfaceColorAtElevationInternal(2.dp)
        val defaultIconBackground = MaterialTheme.colorScheme.surfaceColorAtElevationInternal(8.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .background(background, MaterialTheme.shapes.small)

        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .background(
                        color = when (commonTask.dueDateStatus) {
                            DueDateStatus.NotSet, DueDateStatus.NoLongerApplicable, null ->
                                defaultIconBackground

                            DueDateStatus.Set -> taigaGreenPositive
                            DueDateStatus.DueSoon -> taigaOrange
                            DueDateStatus.PastDue -> taigaRed
                        }.takeUnless { editActions.editDueDate.isLoading } ?: defaultIconBackground,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(4.dp)
            ) {
                if (editActions.editDueDate.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        painter = painterResource(RDrawable.ic_clock),
                        contentDescription = null,
                        tint = commonTask.dueDate?.let { MaterialTheme.colorScheme.onSurface }
                            ?: MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            DatePicker(
                date = commonTask.dueDate,
                onDatePick = {
                    editActions.editDueDate.apply {
                        it?.let { select(it) } ?: remove(Unit)
                    }
                },
                hintId = RString.no_due_date,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
