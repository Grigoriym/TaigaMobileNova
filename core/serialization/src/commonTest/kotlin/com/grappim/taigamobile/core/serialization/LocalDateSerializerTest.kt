package com.grappim.taigamobile.core.serialization

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
private data class LocalDateWrapper(
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate
)

class LocalDateSerializerTest {

    @Test
    fun `round-trip preserves value`() {
        val date = LocalDate(2024, 6, 15)
        val encoded = Json.encodeToString(LocalDateWrapper(date))
        val decoded = Json.decodeFromString<LocalDateWrapper>(encoded)
        assertEquals(date, decoded.date)
    }

    @Test
    fun `serialize encodes as ISO date string`() {
        val date = LocalDate(2024, 6, 15)
        val encoded = Json.encodeToString(LocalDateWrapper(date))
        assertEquals("""{"date":"2024-06-15"}""", encoded)
    }

    @Test
    fun `deserialize from ISO date string`() {
        val decoded = Json.decodeFromString<LocalDateWrapper>("""{"date":"2024-06-15"}""")
        assertEquals(LocalDate(2024, 6, 15), decoded.date)
    }
}
