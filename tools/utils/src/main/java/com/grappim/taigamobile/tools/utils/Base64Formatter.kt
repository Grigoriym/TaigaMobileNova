package com.grappim.taigamobile.tools.utils

import java.io.File
import java.util.Base64

/**
 * Encodes a file to a single-line base64 string suitable for storing as a GitHub secret.
 *
 * The CI workflow decodes it with:
 *   echo $ENCODED_STRING_R | base64 -d > ./taigamobilenova_keystore_release.jks
 */
private const val DEFAULT_JKS_PATH = "taigamobilenova_debug.jks"

fun main() {
    val file = File(DEFAULT_JKS_PATH)
    if (!file.exists()) {
        println("File not found: ${file.absolutePath}")
        return
    }
    val encoded = Base64.getEncoder().encodeToString(file.readBytes())
    println(encoded)
}
