package com.grappim.taigamobile.utils.formatter.datetime

import android.annotation.SuppressLint
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@SuppressLint("NewApi")
private val mediumFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

actual fun platformFormatMediumDate(date: LocalDate): String =
    mediumFormatter.format(date.toJavaLocalDate())

actual fun platformFormatMediumDateTime(dateTime: LocalDateTime): String =
    mediumFormatter.format(dateTime.toJavaLocalDateTime())
