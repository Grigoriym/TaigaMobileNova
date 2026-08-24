package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * ISO methods only — they are pure `commonMain` code and behave identically on every platform.
 * `formatMediumDate` / `formatMediumDateTime` delegate to the `expect` functions and are asserted
 * in `KotlinxDateTimeFormatterJvmTest` against the JVM actual.
 */
class KotlinxDateTimeFormatterTest {

    private val sut = KotlinxDateTimeFormatter()

    @Test
    fun `formatIsoDate zero-pads month and day`() {
        assertEquals("2024-02-03", sut.formatIsoDate(LocalDate(2024, 2, 3)))
    }

    @Test
    fun `formatIsoDate handles a leap day`() {
        assertEquals("2024-02-29", sut.formatIsoDate(LocalDate(2024, 2, 29)))
    }

    @Test
    fun `parseIsoDate reads a zero-padded date`() {
        assertEquals(LocalDate(2024, 2, 3), sut.parseIsoDate("2024-02-03"))
    }

    @Test
    fun `parseIsoDate round-trips formatIsoDate`() {
        val date = LocalDate(1999, 12, 31)
        assertEquals(date, sut.parseIsoDate(sut.formatIsoDate(date)))
    }

    @Test
    fun `parseIsoDate rejects a non-ISO layout`() {
        assertFailsWith<IllegalArgumentException> { sut.parseIsoDate("15-01-2024") }
    }

    @Test
    fun `parseIsoDate rejects an out-of-range month`() {
        assertFailsWith<IllegalArgumentException> { sut.parseIsoDate("2024-13-01") }
    }

    @Test
    fun `parseIsoDate rejects a blank string`() {
        assertFailsWith<IllegalArgumentException> { sut.parseIsoDate("") }
    }
}
