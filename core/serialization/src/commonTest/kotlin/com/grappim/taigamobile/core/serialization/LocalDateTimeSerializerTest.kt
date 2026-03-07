package com.grappim.taigamobile.core.serialization

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class LocalDateTimeWrapper(
    @Serializable(with = LocalDateTimeSerializer::class)
    val dateTime: LocalDateTime
)

class LocalDateTimeSerializerTest {

    @Test
    fun `round-trip preserves value`() {
        val dateTime = LocalDateTime(2024, 6, 15, 12, 30, 45)
        val encoded = Json.encodeToString(LocalDateTimeWrapper(dateTime))
        val decoded = Json.decodeFromString<LocalDateTimeWrapper>(encoded)
        assertEquals(dateTime, decoded.dateTime)
    }

    @Test
    fun `round-trip preserves value at midnight`() {
        val dateTime = LocalDateTime(2024, 1, 1, 0, 0, 0)
        val encoded = Json.encodeToString(LocalDateTimeWrapper(dateTime))
        val decoded = Json.decodeFromString<LocalDateTimeWrapper>(encoded)
        assertEquals(dateTime, decoded.dateTime)
    }

    @Test
    fun `round-trip preserves value with nanoseconds`() {
        val dateTime = LocalDateTime(2024, 6, 15, 12, 30, 45, 123456789)
        val encoded = Json.encodeToString(LocalDateTimeWrapper(dateTime))
        val decoded = Json.decodeFromString<LocalDateTimeWrapper>(encoded)
        assertEquals(dateTime, decoded.dateTime)
    }
}
