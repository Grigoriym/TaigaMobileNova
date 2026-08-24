package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [toHex] and [toColor] are deprecated in favour of [ColorMapper] but are still called from several
 * screens, so they are covered here until the last call site is gone.
 */
@Suppress("DEPRECATION")
class ComposableUtilsTest {

    @Test
    fun `fixNullColor substitutes the front-end default for null and empty`() {
        assertEquals("#A9AABC", null.fixNullColor())
        assertEquals("#A9AABC", "".fixNullColor())
    }

    @Test
    fun `fixNullColor leaves a non-empty string alone`() {
        assertEquals("#FF0000", "#FF0000".fixNullColor())
    }

    @Test
    fun `textColor is white on a dark background`() {
        assertEquals(Color.White, Color.Black.textColor())
        assertEquals(Color.White, Color(0x22, 0x22, 0x22).textColor())
    }

    @Test
    fun `textColor is black on a light background`() {
        assertEquals(Color.Black, Color.White.textColor())
        assertEquals(Color.Black, Color(0xEE, 0xEE, 0xEE).textColor())
    }

    @Test
    fun `toHex drops the opaque alpha channel`() {
        assertEquals("#FF0000", Color.Red.toHex())
        assertEquals("#0000FF", Color.Blue.toHex())
    }

    @Test
    fun `toColor parses both hex lengths`() {
        assertEquals(Color.Red, "#FF0000".toColor())
        assertEquals(Color.Red, "#FFFF0000".toColor())
    }

    @Test
    fun `toColor returns transparent for an unparseable string`() {
        assertEquals(Color.Transparent, "not a color".toColor())
    }
}
