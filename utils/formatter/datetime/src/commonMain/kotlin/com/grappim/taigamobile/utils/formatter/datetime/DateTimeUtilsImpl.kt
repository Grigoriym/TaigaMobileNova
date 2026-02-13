package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.Instant

class DateTimeUtilsImpl(
    private val formatter: KotlinxDateTimeFormatter
) : DateTimeUtils {

    override fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long =
        localDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    override fun fromMillisToLocalDate(millis: Long): LocalDate =
        Instant.fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.UTC)
            .date

    override fun formatToMediumFormat(localDate: LocalDate): String =
        formatter.formatMediumDate(localDate)

    override fun formatToMediumFormat(localDateTime: LocalDateTime): String =
        formatter.formatMediumDateTime(localDateTime)

    override fun parseLocalDateToString(localDate: LocalDate): String =
        formatter.formatIsoDate(localDate)

    override fun parseToLocalDate(text: String): LocalDate =
        formatter.parseIsoDate(text)

    override fun getLocalDateNow(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}