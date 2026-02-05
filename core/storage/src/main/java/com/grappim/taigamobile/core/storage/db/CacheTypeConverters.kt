package com.grappim.taigamobile.core.storage.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.grappim.taigamobile.core.domain.CommonTaskType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Type converters for cached entities.
 */
@[ProvidedTypeConverter Singleton]
class CacheTypeConverters @Inject constructor(private val json: Json) {

    // LocalDate converters (stored as epoch day)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    // LocalDateTime converters (stored as epoch millis UTC)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? = dateTime?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(epochMillis: Long?): LocalDateTime? = epochMillis?.let {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
    }

    // CommonTaskType converters (stored as string name)
    @TypeConverter
    fun fromCommonTaskType(taskType: CommonTaskType): String = taskType.name

    @TypeConverter
    fun toCommonTaskType(name: String): CommonTaskType = CommonTaskType.valueOf(name)

    // List<String> converters for tags (stored as JSON)
    @TypeConverter
    fun fromStringList(list: List<String>): String = json.encodeToString(list)

    @TypeConverter
    fun toStringList(value: String): List<String> = json.decodeFromString(value)
}
