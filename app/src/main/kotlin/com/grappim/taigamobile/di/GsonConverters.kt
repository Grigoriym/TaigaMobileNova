package com.grappim.taigamobile.di

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LocalDateTypeAdapter {
    @ToJson
    fun toJson(value: LocalDate): String = DateTimeFormatter.ISO_LOCAL_DATE.format(value)

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

// used in TaskRepository
fun String.toLocalDate(): LocalDate = LocalDate.parse(this)
