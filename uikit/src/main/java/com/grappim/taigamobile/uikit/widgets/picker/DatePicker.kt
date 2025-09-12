package com.grappim.taigamobile.uikit.widgets.picker

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.google.android.material.datepicker.MaterialDatePicker
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.R
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.ui.activity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * Date picker with material dialog. Null passed to onDatePicked() means selection was cleared
 */
@Composable
@Deprecated("Use composable DatePicker instead")
fun DatePicker(
    date: LocalDate?,
    onDatePick: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes hintId: Int = RString.date_hint,
    showClearButton: Boolean = true,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    onClose: () -> Unit = {},
    onOpen: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context.activity
    val fragmentManager = activity.supportFragmentManager

    Box(modifier = modifier) {
        val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

        val dialog = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(RString.select_date)
            .setTheme(R.style.DatePicker)
            .setSelection(
                date?.atStartOfDay(ZoneOffset.UTC)
                    ?.toInstant()
                    ?.toEpochMilli()
            )
            .build()
            .apply {
                addOnDismissListener { onClose() }
                addOnPositiveButtonClickListener {
                    onDatePick(
                        Instant.ofEpochMilli(it)
                            .atOffset(ZoneOffset.UTC)
                            .toLocalDate()
                    )
                }
            }

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date?.format(dateFormatter) ?: stringResource(hintId),
                style = style,
                modifier = Modifier.clickable {
                    onOpen()
                    dialog.show(fragmentManager, dialog.toString())
                },
                color = date?.let { MaterialTheme.colorScheme.onSurface }
                    ?: MaterialTheme.colorScheme.outline
            )

            // do not show clear button if there is no date (sounds right to me)
            if (showClearButton && date != null) {
                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = { onDatePick(null) },
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_remove),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
