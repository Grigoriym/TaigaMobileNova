package com.grappim.taigamobile.utils.formatter.datetime

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DateTimeUtilsImpl @Inject constructor(
    @IsoLocalDateFormatter private val isoDateFormatter: DateTimeFormatter,
    @LocalDateUIMedium private val localDateUiMediumFormatter: DateTimeFormatter
) : DateTimeUtils {

    override fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long =
        localDate.atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    override fun fromMillisToLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis)
        .atOffset(ZoneOffset.UTC)
        .toLocalDate()

    override fun formatLocalDateUiMedium(localDate: LocalDate): String =
        localDateUiMediumFormatter.format(localDate)

    override fun parseLocalDateToString(localDate: LocalDate): String =
        isoDateFormatter.format(localDate)
}
