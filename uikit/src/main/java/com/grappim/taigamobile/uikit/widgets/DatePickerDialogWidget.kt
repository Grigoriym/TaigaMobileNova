package com.grappim.taigamobile.uikit.widgets

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.strings.RString
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogWidget(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    onDismissButonClick: () -> Unit,
    onConfirmButtonClick: (Long?) -> Unit,
    initialDate: LocalDate? = null
) {
    if (isVisible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()
                ?.toEpochMilli()
        )
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }

        DatePickerDialog(
            onDismissRequest = onDismissRequest,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmButtonClick(datePickerState.selectedDateMillis)
                    },
                    enabled = confirmEnabled
                ) {
                    Text(text = stringResource(RString.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissButonClick) {
                    Text(text = stringResource(RString.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
