package com.grappim.taigamobile.uikit.widgets.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.uikit.widgets.picker.DatePicker
import java.time.LocalDate

@Composable
fun EditSprintDialog(
    onDismiss: () -> Unit,
    initialName: String = "",
    initialStart: LocalDate? = null,
    initialEnd: LocalDate? = null,
    onConfirm: (name: String, start: LocalDate, end: LocalDate) -> Unit
) {
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(
                initialName
            )
        )
    }
    var start by remember { mutableStateOf(initialStart ?: LocalDate.now()) }
    var end by remember { mutableStateOf(initialEnd ?: LocalDate.now().plusDays(14)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(RString.cancel),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    name.text.trim()
                        .takeIf { it.isNotEmpty() }
                        ?.let { onConfirm(it, start, end) }
                }
            ) {
                Text(
                    text = stringResource(RString.ok),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        title = {
            TextFieldWithHint(
                hintId = RString.sprint_name_hint,
                value = name,
                onValueChange = { name = it },
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            val pickerStyle =
                MaterialTheme.typography.titleMedium.merge(
                    TextStyle(fontWeight = FontWeight.Normal)
                )
            val pickerModifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.small
                )
                .padding(6.dp)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                DatePicker(
                    date = start,
                    onDatePick = { start = it!! },
                    showClearButton = false,
                    style = pickerStyle,
                    modifier = pickerModifier
                )

                Spacer(
                    Modifier
                        .width(16.dp)
                        .height(1.5.dp)
                        .background(MaterialTheme.colorScheme.onSurface)
                )

                DatePicker(
                    date = end,
                    onDatePick = { end = it!! },
                    showClearButton = false,
                    style = pickerStyle,
                    modifier = pickerModifier
                )
            }
        }
    )
}
