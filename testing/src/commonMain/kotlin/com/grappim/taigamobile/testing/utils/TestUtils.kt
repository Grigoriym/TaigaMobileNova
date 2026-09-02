package com.grappim.taigamobile.testing.utils

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock

val nowLocalDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
val nowLocalDateTime: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun getRandomLong(): Long = Random.nextLong()

fun getRandomInt(): Int = Random.nextInt()

fun getRandomBoolean(): Boolean = Random.nextBoolean()

fun getRandomLocalDateTime(): LocalDateTime = LocalDateTime(
    year = Random.nextInt(1970, 2023),
    month = Month.entries.toTypedArray().random(),
    day = Random.nextInt(1, 28),
    hour = Random.nextInt(0, 23),
    minute = Random.nextInt(0, 59),
    second = Random.nextInt(0, 59),
)

@Suppress("MagicNumber")
fun getRandomString(): String = List(15) {
    Random.nextInt(97, 123)
        .toChar()
}.joinToString("")

fun getRandomColor(): Color {
    val red = Random.nextInt(0, 256)
    val green = Random.nextInt(0, 256)
    val blue = Random.nextInt(0, 256)
    return Color(red, green, blue)
}

val testException = IllegalStateException("error")

/**
 * Asserts that [block] fails with [testException], matching **type and message rather than
 * identity**. Two reasons not to use `assertEquals(testException, caught)`:
 *
 * - a throw from inside an `async` child is rethrown as a *copy* on JVM (coroutines' stack-trace
 *   recovery), so identity and equality both fail;
 * - a bare `assertFailsWith<IllegalStateException>` also passes when a fake hits its own
 *   `error("… not set")` guard, which would make a failure-path test green for the wrong reason.
 */
inline fun assertFailsWithTestException(block: () -> Unit) {
    val thrown = assertFailsWith<IllegalStateException>(block = block)
    assertEquals(testException.message, thrown.message)
}
