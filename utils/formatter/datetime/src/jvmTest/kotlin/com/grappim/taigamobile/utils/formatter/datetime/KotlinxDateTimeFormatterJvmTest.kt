package com.grappim.taigamobile.utils.formatter.datetime

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * **This asserts the JVM actual of [platformFormatMediumDate] / [platformFormatMediumDateTime]
 * only** (`KotlinxDateTimeFormatter.jvm.kt`). Android's actual is byte-for-byte the same
 * `java.time` code, so it is covered by proxy; **iOS's `NSDateFormatter` actual is not covered by
 * anything** and would have to be verified on a native test target.
 *
 * The exact rendering is locale-dependent — "Jan 15, 2024" in `en-US`, "15.01.2024" in `de-DE`,
 * "2024. jan. 15." in `hu-HU`. Most assertions here use locale-independent properties (day, month
 * and year all present; no time-of-day; no time-zone influence) plus a comparison against a JDK
 * formatter built with the current default locale. Those four properties were checked to hold in
 * en-US, de-DE, ja-JP, sv-SE, th-TH, ar-EG and hu-HU. One test does pin an exact rendering across a
 * locale change — see `medium date picks up a locale changed after the formatter was first used`.
 */
class KotlinxDateTimeFormatterJvmTest {

    private val sut = KotlinxDateTimeFormatter()
    private val defaultTimeZone: TimeZone = TimeZone.getDefault()
    private val defaultFormatLocale: Locale = Locale.getDefault(Locale.Category.FORMAT)

    @AfterTest
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
        Locale.setDefault(Locale.Category.FORMAT, defaultFormatLocale)
    }

    @Test
    fun `medium date matches the JDK medium localized date`() {
        val date = LocalDate(2024, 1, 15)
        val expected = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault(Locale.Category.FORMAT))
            .format(date.toJavaLocalDate())

        assertEquals(expected, sut.formatMediumDate(date))
    }

    @Test
    fun `medium date encodes the day`() {
        assertNotEquals(
            sut.formatMediumDate(LocalDate(2024, 1, 15)),
            sut.formatMediumDate(LocalDate(2024, 1, 16))
        )
    }

    @Test
    fun `medium date encodes the month`() {
        assertNotEquals(
            sut.formatMediumDate(LocalDate(2024, 1, 15)),
            sut.formatMediumDate(LocalDate(2024, 2, 15))
        )
    }

    @Test
    fun `medium date encodes the year`() {
        assertNotEquals(
            sut.formatMediumDate(LocalDate(2024, 1, 15)),
            sut.formatMediumDate(LocalDate(2023, 1, 15))
        )
    }

    @Test
    fun `medium date ignores the default time zone`() {
        val date = LocalDate(2024, 1, 15)

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        val inUtc = sut.formatMediumDate(date)
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
        val inTokyo = sut.formatMediumDate(date)
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"))
        val inHonolulu = sut.formatMediumDate(date)

        assertEquals(inUtc, inTokyo)
        assertEquals(inUtc, inHonolulu)
    }

    @Test
    fun `medium date picks up a locale changed after the formatter was first used`() {
        val date = LocalDate(2024, 1, 15)

        Locale.setDefault(Locale.Category.FORMAT, Locale.US)
        sut.formatMediumDate(date)

        Locale.setDefault(Locale.Category.FORMAT, Locale.GERMANY)
        val actual = sut.formatMediumDate(date)
        val expected = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(Locale.GERMANY)
            .format(date.toJavaLocalDate())

        assertEquals(expected, actual)
    }

    @Test
    fun `medium date-time drops the time of day`() {
        val date = LocalDate(2024, 1, 15)

        assertEquals(
            sut.formatMediumDate(date),
            sut.formatMediumDateTime(LocalDateTime(2024, 1, 15, 0, 0))
        )
        assertEquals(
            sut.formatMediumDate(date),
            sut.formatMediumDateTime(LocalDateTime(2024, 1, 15, 23, 59, 59))
        )
    }
}
