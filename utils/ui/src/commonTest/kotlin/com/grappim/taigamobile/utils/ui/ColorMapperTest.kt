package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * [ColorMapper.fromColorToString] delegates to the `formatColor` `expect` function, so the exact
 * string it produces is platform-specific. These assertions hold on JVM and Android, whose actuals
 * are both `"#%08X".format(color)`.
 */
class ColorMapperTest {

    private val sut = ColorMapper()

    @Test
    fun `fromColorToString drops the opaque alpha channel`() {
        assertEquals("#FF0000", sut.fromColorToString(Color.Red))
        assertEquals("#00FF00", sut.fromColorToString(Color.Green))
        assertEquals("#0000FF", sut.fromColorToString(Color.Blue))
        assertEquals("#000000", sut.fromColorToString(Color.Black))
    }

    /**
     * The alpha is stripped by a plain `replace("#FF", "#")`, so a non-opaque colour whose alpha is
     * not `FF` keeps all eight digits — the backend rejects those, which is what the comment on
     * [ColorMapper.fromColorToString] is about.
     */
    @Test
    fun `fromColorToString keeps the alpha channel when it is not opaque`() {
        assertEquals("#80FF0000", sut.fromColorToString(Color.Red.copy(alpha = 128f / 255f)))
    }

    @Test
    fun `fromStringToColor parses a six digit hex string as opaque`() {
        assertEquals(Color.Red, sut.fromStringToColor("#FF0000"))
        assertEquals(Color.Green, sut.fromStringToColor("00FF00"))
    }

    @Test
    fun `fromStringToColor parses an eight digit hex string with its alpha`() {
        assertEquals(Color.Red, sut.fromStringToColor("#FFFF0000"))
        assertEquals(Color.Transparent, sut.fromStringToColor("#00000000"))
    }

    @Test
    fun `fromStringToColor returns transparent for a string of the wrong length`() {
        assertEquals(Color.Transparent, sut.fromStringToColor("#FFF"))
        assertEquals(Color.Transparent, sut.fromStringToColor(""))
    }

    @Test
    fun `fromStringToColor returns transparent for a non-hex string`() {
        assertEquals(Color.Transparent, sut.fromStringToColor("#ZZZZZZ"))
    }

    @Test
    fun `fromColorToString and fromStringToColor round-trip`() {
        val color = Color(0x12, 0x34, 0x56)

        assertEquals(color, sut.fromStringToColor(sut.fromColorToString(color)))
    }

    @Test
    fun `fromStringToInt handles both lengths with and without the leading hash`() {
        assertEquals(0xFFFF0000.toInt(), fromStringToInt("#FF0000"))
        assertEquals(0xFFFF0000.toInt(), fromStringToInt("FF0000"))
        assertEquals(0x80FF0000.toInt(), fromStringToInt("#80FF0000"))
        assertEquals(0x80FF0000.toInt(), fromStringToInt("80FF0000"))
    }

    @Test
    fun `fromStringToInt throws on an unknown length`() {
        assertFailsWith<IllegalArgumentException> { fromStringToInt("#FFFFFFF") }
    }
}
