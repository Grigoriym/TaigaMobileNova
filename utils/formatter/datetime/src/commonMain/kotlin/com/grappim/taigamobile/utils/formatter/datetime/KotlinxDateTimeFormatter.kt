package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class KotlinxDateTimeFormatter {
    fun formatIsoDate(date: LocalDate): String = date.toString()

    fun parseIsoDate(text: String): LocalDate = LocalDate.parse(text)

    fun formatMediumDate(date: LocalDate): String = platformFormatMediumDate(date)

    fun formatMediumDateTime(dateTime: LocalDateTime): String = platformFormatMediumDateTime(dateTime)
}

internal expect fun platformFormatMediumDate(date: LocalDate): String

internal expect fun platformFormatMediumDateTime(dateTime: LocalDateTime): String