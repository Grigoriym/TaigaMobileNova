package com.grappim.taigamobile.testing.utils

import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class FakeDateTimeUtils : DateTimeUtils {
    var fixedDate: LocalDate = LocalDate(2024, 1, 15)

    override fun getLocalDateNow(): LocalDate = fixedDate

    override fun retrieveEpochMillisAtStartOfDay(localDate: LocalDate): Long = error("not used in this test")
    override fun fromMillisToLocalDate(millis: Long): LocalDate = error("not used in this test")
    override fun parseLocalDateToString(localDate: LocalDate): String = localDate.toString()
    override fun formatToMediumFormat(localDate: LocalDate): String = localDate.toString()
    override fun formatToMediumFormat(localDateTime: LocalDateTime): String = localDateTime.date.toString()
    override fun parseToLocalDate(text: String): LocalDate = error("not used in this test")
}