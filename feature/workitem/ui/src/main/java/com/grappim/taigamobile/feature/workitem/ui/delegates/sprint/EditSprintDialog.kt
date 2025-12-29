package com.grappim.taigamobile.feature.workitem.ui.delegates.sprint

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.DatePickerDialogWidget
import com.grappim.taigamobile.uikit.widgets.editor.HintTextField
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString

@Composable
fun EditSprintDialog(state: SprintDialogState, onConfirm: () -> Unit) {
    val context = LocalContext.current
    if (state.isSprintDialogVisible) {
        DatePickerDialogWidget(
            isVisible = state.isStartDateDialogVisible,
            onDismissRequest = {
                state.onStartDateDismissRequest()
            },
            onDismissButonClick = {
                state.onStartDateDismissButonClick()
            },
            onConfirmButtonClick = { newDate: Long? ->
                state.onStartDateConfirmButtonClick(newDate)
            },
            initialDate = state.startDate
        )

        DatePickerDialogWidget(
            isVisible = state.isEndDateDialogVisible,
            onDismissRequest = {
                state.onEndDateDismissRequest()
            },
            onDismissButonClick = {
                state.onEndDateDismissButonClick()
            },
            onConfirmButtonClick = { newDate: Long? ->
                state.onEndDateConfirmButtonClick(newDate)
            },
            initialDate = state.endDate
        )

        AlertDialog(
            onDismissRequest = state.onDismiss,
            dismissButton = {
                TextButton(onClick = state.onDismiss) {
                    Text(
                        text = stringResource(RString.cancel),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm
                ) {
                    Text(
                        text = stringResource(RString.ok),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            title = {
                Column {
                    if (state.dialogError.isNotEmpty()) {
                        Text(
                            text = state.dialogError.asString(context),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    HintTextField(
                        value = state.sprintNameValue,
                        onValueChange = state.onSetSprintNameValue,
                        hint = NativeText.Resource(RString.sprint_name_hint),
                        error = state.sprintNameError
                    )
                }
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = {
                            state.setIsStartDateDialogVisible(true)
                        }
                    ) {
                        Text(
                            text = state.startDateToDisplay,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(
                        Modifier
                            .width(16.dp)
                            .height(1.5.dp)
                            .background(MaterialTheme.colorScheme.onSurface)
                    )

                    TextButton(
                        onClick = {
                            state.setIsEndDateDialogVisible(true)
                        }
                    ) {
                        Text(
                            text = state.endDateToDisplay,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}
