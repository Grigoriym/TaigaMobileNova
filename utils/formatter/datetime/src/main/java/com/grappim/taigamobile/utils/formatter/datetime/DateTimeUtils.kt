package com.grappim.taigamobile.utils.formatter.datetime

import java.time.LocalDate

interface DateTimeUtils {
    fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long
    fun fromMillisToLocalDate(millis: Long): LocalDate
    fun parseLocalDateToString(localDate: LocalDate): String
    fun formatLocalDateUiMedium(localDate: LocalDate): String
}
