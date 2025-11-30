package com.grappim.taigamobile.feature.workitem.ui.utils

import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import java.time.LocalDate

fun DateTimeUtils.getDueDateText(dueDate: LocalDate?): NativeText = if (dueDate == null) {
    NativeText.Resource(id = RString.no_due_date)
} else {
    NativeText.Simple(formatToMediumFormat(dueDate))
}
