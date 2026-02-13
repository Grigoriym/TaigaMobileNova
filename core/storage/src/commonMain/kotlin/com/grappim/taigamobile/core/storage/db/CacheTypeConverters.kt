package com.grappim.taigamobile.core.storage.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.di.DbJsonQualifier
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Instant

/**
 * Type converters for cached entities.
 */
@[ProvidedTypeConverter Single]
class CacheTypeConverters(@param:DbJsonQualifier private val json: Json) {

    // LocalDate converters (stored as epoch day)
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): Long? = date?.toEpochDays()

    @TypeConverter
    fun toLocalDate(epochDay: Long?): LocalDate? =
        epochDay?.let { LocalDate.Companion.fromEpochDays(it.toInt()) }

    // LocalDateTime converters (stored as epoch millis UTC)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): Long? =
        dateTime?.toInstant(TimeZone.UTC)?.toEpochMilliseconds()

    @TypeConverter
    fun toLocalDateTime(epochMillis: Long?): LocalDateTime? = epochMillis?.let {
        Instant.Companion.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC)
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