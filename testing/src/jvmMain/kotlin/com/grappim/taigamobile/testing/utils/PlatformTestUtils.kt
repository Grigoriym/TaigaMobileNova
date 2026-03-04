package com.grappim.taigamobile.testing.utils

import io.github.vinceglb.filekit.PlatformFile
import java.io.File

actual fun createTestPlatformFile(name: String, bytes: ByteArray): PlatformFile {
    val file = File(System.getProperty("java.io.tmpdir"), name)
    file.writeBytes(bytes)
    file.deleteOnExit()
    return PlatformFile(file)
}
