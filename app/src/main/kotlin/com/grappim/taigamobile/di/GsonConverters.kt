package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.api.toLocalDate
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class LocalDateTypeAdapter @Inject constructor(private val dateTimeUtils: DateTimeUtils) {
    @ToJson
    fun toJson(value: LocalDate): String = dateTimeUtils.parseLocalDateToString(value)

    @FromJson
    fun fromJson(input: String): LocalDate = input.toLocalDate()
}

class LocalDateTimeTypeAdapter {
    @ToJson
    fun toJson(value: LocalDateTime): String = value.atZone(ZoneId.systemDefault())
        .toInstant()
        .toString()

    @FromJson
    fun fromJson(input: String): LocalDateTime = Instant.parse(input)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
