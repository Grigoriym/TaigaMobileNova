package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.utils.ui.serializers.ColorSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Serializable
private data class ColorWrapper(
    @Serializable(with = ColorSerializer::class)
    val color: Color
)

class ColorSerializerTest {

    @Test
    fun `round-trip Red`() {
        assertRoundTrip(Color.Red)
    }

    @Test
    fun `round-trip Black`() {
        assertRoundTrip(Color.Black)
    }

    @Test
    fun `round-trip White`() {
        assertRoundTrip(Color.White)
    }

    @Test
    fun `round-trip Transparent`() {
        assertRoundTrip(Color.Transparent)
    }

    @Test
    fun `serialize encodes color value as ulong string`() {
        val color = Color.Red
        val encoded = Json.encodeToString(ColorWrapper(color))
        assertEquals("""{"color":"${color.value}"}""", encoded)
    }

    @Test
    fun `deserialize from known ulong string`() {
        val color = Color.Red
        val decoded = Json.decodeFromString<ColorWrapper>("""{"color":"${color.value}"}""")
        assertEquals(color, decoded.color)
    }

    @Test
    fun `deserialize invalid string throws`() {
        assertFailsWith<Exception> {
            Json.decodeFromString<ColorWrapper>("""{"color":"#FF0000"}""")
        }
    }

    private fun assertRoundTrip(color: Color) {
        val encoded = Json.encodeToString(ColorWrapper(color))
        val decoded = Json.decodeFromString<ColorWrapper>(encoded)
        assertEquals(color, decoded.color)
    }
}
