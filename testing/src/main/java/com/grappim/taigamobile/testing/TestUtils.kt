package com.grappim.taigamobile.testing

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import kotlin.random.Random

val nowLocalDate = LocalDate.now()

fun getRandomLong(): Long = Random.nextLong()

fun getRandomInt(): Int = Random.nextInt()

fun getRandomBoolean(): Boolean = Random.nextBoolean()

fun getRandomLocalDateTime(): LocalDateTime = LocalDateTime.of(
    Random.nextInt(Year.MIN_VALUE, Year.MAX_VALUE),
    Random.nextInt(1, 13),
    Random.nextInt(1, 29),
    Random.nextInt(0, 24),
    Random.nextInt(0, 60),
    Random.nextInt(0, 60),
    Random.nextInt(0, 999999999)
)

@Suppress("MagicNumber")
fun getRandomString(): String = List(15) {
    Random.nextInt(97, 123)
        .toChar()
}.joinToString("")

/**
 * If you need the result to be anything other than null, then use Robolectric
 */
fun getRandomUri(): Uri {
    val randomString = getRandomString()
    val uriString = "https://grappim.com/$randomString"
    return uriString.toUri()
}

fun getRandomFile(): File = File(getRandomString())

fun getRandomColor(): Color {
    val red = Random.nextInt(0, 256)
    val green = Random.nextInt(0, 256)
    val blue = Random.nextInt(0, 256)
    return Color(red, green, blue)
}

val testException = IllegalStateException("error")
