package com.grappim.taigamobile.core.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeSettingsTest {

    @Test
    fun `fromValue maps every stored value back to its entry`() {
        assertEquals(ThemeSettings.System, ThemeSettings.fromValue("system"))
        assertEquals(ThemeSettings.Light, ThemeSettings.fromValue("light"))
        assertEquals(ThemeSettings.Dark, ThemeSettings.fromValue("dark"))
    }

    @Test
    fun `fromValue returns null for null`() {
        assertNull(ThemeSettings.fromValue(null))
    }

    @Test
    fun `fromValue returns null for an unknown value`() {
        assertNull(ThemeSettings.fromValue("solarized"))
    }

    @Test
    fun `fromValue is case sensitive`() {
        assertNull(ThemeSettings.fromValue("System"))
    }

    @Test
    fun `default is System`() {
        assertEquals(ThemeSettings.System, ThemeSettings.default())
    }

    @Test
    fun `isSystemDefault is true only for System`() {
        assertTrue(ThemeSettings.System.isSystemDefault())
        assertFalse(ThemeSettings.Light.isSystemDefault())
        assertFalse(ThemeSettings.Dark.isSystemDefault())
    }

    @Test
    fun `isDark is true only for Dark`() {
        assertTrue(ThemeSettings.Dark.isDark())
        assertFalse(ThemeSettings.System.isDark())
        assertFalse(ThemeSettings.Light.isDark())
    }

    @Test
    fun `isLight is true only for Light`() {
        assertTrue(ThemeSettings.Light.isLight())
        assertFalse(ThemeSettings.System.isLight())
        assertFalse(ThemeSettings.Dark.isLight())
    }
}
