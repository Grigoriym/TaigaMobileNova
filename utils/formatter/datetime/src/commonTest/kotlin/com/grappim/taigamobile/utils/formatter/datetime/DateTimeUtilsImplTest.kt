package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

/**
 * Covers only the platform-independent half of [DateTimeUtilsImpl]: epoch conversion, ISO
 * formatting and delegation. The medium-format methods hand off to [platformFormatMediumDate] /
 * [platformFormatMediumDateTime], whose output is locale- and platform-specific — those are
 * asserted in `jvmTest` against the JVM actual only.
 *
 * The epoch expectations below are hard-coded UTC values on purpose: they are what proves
 * [DateTimeUtilsImpl] uses `TimeZone.UTC` rather than the machine's zone. Run under a different
 * `TZ` and they must still hold.
 */
class DateTimeUtilsImplTest {

    private val sut = DateTimeUtilsImpl(KotlinxDateTimeFormatter())

    @Test
    fun `retrieveEpochMillisAtStartOfDay returns midnight UTC`() {
        assertEquals(1705276800000L, sut.retrieveEpochMillisAtStartOfDay(LocalDate(2024, 1, 15)))
    }

    @Test
    fun `retrieveEpochMillisAtStartOfDay returns zero for the epoch date`() {
        assertEquals(0L, sut.retrieveEpochMillisAtStartOfDay(LocalDate(1970, 1, 1)))
    }

    @Test
    fun `retrieveEpochMillisAtStartOfDay returns a negative value before the epoch`() {
        assertEquals(-86400000L, sut.retrieveEpochMillisAtStartOfDay(LocalDate(1969, 12, 31)))
    }

    @Test
    fun `retrieveEpochMillisAtStartOfDay handles a leap day`() {
        assertEquals(1709164800000L, sut.retrieveEpochMillisAtStartOfDay(LocalDate(2024, 2, 29)))
    }

    @Test
    fun `fromMillisToLocalDate returns the epoch date for zero`() {
        assertEquals(LocalDate(1970, 1, 1), sut.fromMillisToLocalDate(0L))
    }

    @Test
    fun `fromMillisToLocalDate returns the UTC date at midnight`() {
        assertEquals(LocalDate(2024, 1, 15), sut.fromMillisToLocalDate(1705276800000L))
    }

    @Test
    fun `fromMillisToLocalDate keeps the UTC date late in the UTC day`() {
        // 2024-01-15T23:30Z — already 2024-01-16 in Asia/Tokyo.
        assertEquals(LocalDate(2024, 1, 15), sut.fromMillisToLocalDate(1705361400000L))
    }

    @Test
    fun `fromMillisToLocalDate keeps the UTC date early in the UTC day`() {
        // 2024-01-15T00:30Z — still 2024-01-14 in Pacific/Honolulu.
        assertEquals(LocalDate(2024, 1, 15), sut.fromMillisToLocalDate(1705278600000L))
    }

    @Test
    fun `fromMillisToLocalDate handles a value before the epoch`() {
        assertEquals(LocalDate(1969, 12, 31), sut.fromMillisToLocalDate(-1L))
    }

    @Test
    fun `epoch millis round-trips back to the same date`() {
        val date = LocalDate(2024, 2, 29)
        assertEquals(date, sut.fromMillisToLocalDate(sut.retrieveEpochMillisAtStartOfDay(date)))
    }

    @Test
    fun `parseLocalDateToString produces an ISO date`() {
        assertEquals("2024-01-15", sut.parseLocalDateToString(LocalDate(2024, 1, 15)))
    }

    @Test
    fun `parseToLocalDate reads an ISO date`() {
        assertEquals(LocalDate(2024, 1, 15), sut.parseToLocalDate("2024-01-15"))
    }

    @Test
    fun `ISO string round-trips back to the same date`() {
        val date = LocalDate(2023, 3, 7)
        assertEquals(date, sut.parseToLocalDate(sut.parseLocalDateToString(date)))
    }

    @Test
    fun `getLocalDateNow uses the system time zone`() {
        val expected = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        assertEquals(expected, sut.getLocalDateNow())
    }

    @Test
    fun `formatToMediumFormat delegates a date to the formatter`() {
        val date = LocalDate(2024, 1, 15)
        assertEquals(KotlinxDateTimeFormatter().formatMediumDate(date), sut.formatToMediumFormat(date))
    }

    @Test
    fun `formatToMediumFormat delegates a date-time to the formatter`() {
        val dateTime = LocalDateTime(2024, 1, 15, 13, 45)
        assertEquals(
            KotlinxDateTimeFormatter().formatMediumDateTime(dateTime),
            sut.formatToMediumFormat(dateTime)
        )
    }
}
