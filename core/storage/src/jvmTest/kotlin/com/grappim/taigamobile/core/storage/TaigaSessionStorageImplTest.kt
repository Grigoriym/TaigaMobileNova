package com.grappim.taigamobile.core.storage

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TaigaSessionStorageImplTest {

    private val colorMapper = ColorMapper()

    /**
     * The eight colours [TaigaSessionStorageImpl] falls back to when nothing has been stored,
     * spelled out here rather than re-derived through [ColorMapper] so that a change to either
     * the list or the encoding is caught.
     */
    private val defaultPresetColors = listOf(
        "#E57373",
        "#81C784",
        "#64B5F6",
        "#FFD54F",
        "#BA68C8",
        "#4DB6AC",
        "#FF8A65",
        "#90A4AE"
    )

    private fun createSut() = TaigaSessionStorageImpl(
        dataStore = createTestDataStore("taiga_session_storage_test"),
        colorMapper = colorMapper
    )

    @Test
    fun `kanbanDefaultSwimline is null when nothing was stored`() = runTest {
        val sut = createSut()

        assertNull(sut.kanbanDefaultSwimline.first())
    }

    @Test
    fun `setKanbanDefaultSwimline stores the value`() = runTest {
        val sut = createSut()

        sut.setKanbanDefaultSwimline(42L)

        assertEquals(42L, sut.kanbanDefaultSwimline.first())
    }

    @Test
    fun `tagPresetColors falls back to the defaults when nothing was stored`() = runTest {
        val sut = createSut()

        assertEquals(defaultPresetColors, sut.tagPresetColors.first())
    }

    @Test
    fun `tagPresetColors falls back to the defaults when the stored value is blank`() = runTest {
        val sut = createSut()

        sut.setTagPresetColors(emptyList())

        assertEquals(defaultPresetColors, sut.tagPresetColors.first())
    }

    @Test
    fun `setTagPresetColors replaces the defaults with the given colors`() = runTest {
        val sut = createSut()

        sut.setTagPresetColors(listOf("#111111", "#222222"))

        assertEquals(listOf("#111111", "#222222"), sut.tagPresetColors.first())
    }

    @Test
    fun `tagPresetColors drops blank entries from the stored value`() = runTest {
        val sut = createSut()

        sut.setTagPresetColors(listOf("#111111", "", "#222222"))

        assertEquals(listOf("#111111", "#222222"), sut.tagPresetColors.first())
    }

    @Test
    fun `getPresetColors returns the defaults when nothing was stored`() = runTest {
        val sut = createSut()

        assertEquals(defaultPresetColors, sut.getPresetColors())
    }

    @Test
    fun `getPresetColorsAsColor maps every stored hex string to a Color`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#FF0000", "#00FF00"))

        assertEquals(listOf(Color(0xFFFF0000), Color(0xFF00FF00)), sut.getPresetColorsAsColor())
    }

    @Test
    fun `addTagPresetColor appends a color that is not already present`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#111111"))

        sut.addTagPresetColor("#222222")

        assertEquals(listOf("#111111", "#222222"), sut.getPresetColors())
    }

    @Test
    fun `addTagPresetColor is a no-op when the color is already present`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#111111", "#222222"))

        sut.addTagPresetColor("#222222")

        assertEquals(listOf("#111111", "#222222"), sut.getPresetColors())
    }

    @Test
    fun `addTagPresetColor taking a Color encodes it before appending`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#111111"))

        sut.addTagPresetColor(Color(0xFFFF0000))

        assertEquals(listOf("#111111", "#FF0000"), sut.getPresetColors())
    }

    @Test
    fun `removeTagPresetColor drops the matching color`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#111111", "#222222"))

        sut.removeTagPresetColor("#111111")

        assertEquals(listOf("#222222"), sut.getPresetColors())
    }

    @Test
    fun `removeTagPresetColor for an absent color leaves the list unchanged`() = runTest {
        val sut = createSut()
        sut.setTagPresetColors(listOf("#111111", "#222222"))

        sut.removeTagPresetColor("#333333")

        assertEquals(listOf("#111111", "#222222"), sut.getPresetColors())
    }

    @Test
    fun `themeSettings defaults to System when nothing was stored`() = runTest {
        val sut = createSut()

        assertEquals(ThemeSettings.System, sut.themeSettings.first())
    }

    @Test
    fun `setThemSetting stores the chosen theme`() = runTest {
        val sut = createSut()

        sut.setThemSetting(ThemeSettings.Dark)

        assertEquals(ThemeSettings.Dark, sut.themeSettings.first())
    }

    @Test
    fun `userId is null when nothing was stored`() = runTest {
        val sut = createSut()

        assertNull(sut.userId.first())
        assertNull(sut.userIdOrNull())
    }

    @Test
    fun `setUserId stores the id and both accessors return it`() = runTest {
        val sut = createSut()

        sut.setUserId(7L)

        assertEquals(7L, sut.userId.first())
        assertEquals(7L, sut.userIdOrNull())
        assertEquals(7L, sut.requireUserId())
    }

    @Test
    fun `requireUserId throws when no user is logged in`() = runTest {
        val sut = createSut()

        val error = assertFailsWith<IllegalStateException> { sut.requireUserId() }
        assertEquals("User not logged in", error.message)
    }

    @Test
    fun `currentProjectId defaults to -1 when nothing was stored`() = runTest {
        val sut = createSut()

        assertEquals(-1L, sut.currentProjectIdFlow.first())
        assertEquals(-1L, sut.getCurrentProjectId())
    }

    @Test
    fun `setCurrentProjectId stores the id`() = runTest {
        val sut = createSut()

        sut.setCurrentProjectId(99L)

        assertEquals(99L, sut.currentProjectIdFlow.first())
        assertEquals(99L, sut.getCurrentProjectId())
    }

    @Test
    fun `crashReportingEnabled defaults to true when nothing was stored`() = runTest {
        val sut = createSut()

        assertTrue(sut.crashReportingEnabled.first())
    }

    @Test
    fun `setCrashReportingEnabled stores the flag`() = runTest {
        val sut = createSut()

        sut.setCrashReportingEnabled(false)

        assertFalse(sut.crashReportingEnabled.first())
    }

    @Test
    fun `clearData resets every stored value back to its default`() = runTest {
        val sut = createSut()
        sut.setUserId(7L)
        sut.setCurrentProjectId(99L)
        sut.setKanbanDefaultSwimline(42L)
        sut.setThemSetting(ThemeSettings.Light)
        sut.setCrashReportingEnabled(false)
        sut.setTagPresetColors(listOf("#111111"))

        sut.clearData()

        assertNull(sut.userIdOrNull())
        assertEquals(-1L, sut.getCurrentProjectId())
        assertNull(sut.kanbanDefaultSwimline.first())
        assertEquals(ThemeSettings.System, sut.themeSettings.first())
        assertTrue(sut.crashReportingEnabled.first())
        assertEquals(defaultPresetColors, sut.getPresetColors())
    }
}
