package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

actual fun platformFormatMediumDate(date: LocalDate): String = date.toString()

actual fun platformFormatMediumDateTime(dateTime: LocalDateTime): String = dateTime.toString()
