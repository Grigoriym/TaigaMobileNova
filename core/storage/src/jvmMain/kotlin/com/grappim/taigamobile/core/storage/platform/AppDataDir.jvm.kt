package com.grappim.taigamobile.core.storage.platform

import java.io.File

private const val APP_DIR_NAME = "TaigaMobile"

/**
 * Per-user, durable application-support directory for the desktop/JVM build — replaces
 * `java.io.tmpdir`, which the OS can clear at any time.
 */
internal fun appDataDir(): File {
    val userHome = System.getProperty("user.home")
    val osName = System.getProperty("os.name").lowercase()
    val baseDir = when {
        osName.contains("win") -> File(System.getenv("APPDATA") ?: "$userHome\\AppData\\Roaming")
        osName.contains("mac") -> File("$userHome/Library/Application Support")
        else -> File(System.getenv("XDG_DATA_HOME") ?: "$userHome/.local/share")
    }
    return File(baseDir, APP_DIR_NAME).apply { mkdirs() }
}
