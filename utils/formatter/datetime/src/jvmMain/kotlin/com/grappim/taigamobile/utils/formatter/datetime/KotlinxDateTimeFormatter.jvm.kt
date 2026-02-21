package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDateTime

internal actual fun platformFormatMediumDate(date: kotlinx.datetime.LocalDate): String {
    return date.toString()
}

internal actual fun platformFormatMediumDateTime(dateTime: LocalDateTime): String {
    return dateTime.toString()
}
