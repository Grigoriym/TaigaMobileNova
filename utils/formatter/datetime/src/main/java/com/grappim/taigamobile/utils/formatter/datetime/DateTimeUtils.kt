package com.grappim.taigamobile.utils.formatter.datetime

import java.time.LocalDate
import java.time.LocalDateTime

interface DateTimeUtils {
    fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long
    fun fromMillisToLocalDate(millis: Long): LocalDate
    fun parseLocalDateToString(localDate: LocalDate): String
    fun formatToMediumFormat(localDate: LocalDate): String

    fun formatToMediumFormat(localDateTime: LocalDateTime): String
}
