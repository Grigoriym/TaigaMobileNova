package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle

private val mediumFormatter = NSDateFormatter().apply {
    dateStyle = NSDateFormatterMediumStyle
    timeStyle = NSDateFormatterNoStyle
}

actual fun platformFormatMediumDate(date: LocalDate): String =
    mediumFormatter.stringFromDate(date.atStartOfDayIn(TimeZone.currentSystemDefault()).toNSDate())

actual fun platformFormatMediumDateTime(dateTime: LocalDateTime): String =
    mediumFormatter.stringFromDate(dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault()).toNSDate())
