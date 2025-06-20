package com.grappim.taigamobile.testing

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.core.net.toUri
import java.io.File
import java.time.OffsetDateTime
import kotlin.random.Random

val nowDate = OffsetDateTime.now()

fun getRandomLong(): Long = Random.nextLong()

fun getRandomBoolean(): Boolean = Random.nextBoolean()

@Suppress("MagicNumber")
fun getRandomString(): String = List(15) { // Generate a list of 10 characters
    Random.nextInt(97, 123) // ASCII range for lowercase letters a-z
        .toChar() // Convert ASCII value to char
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
