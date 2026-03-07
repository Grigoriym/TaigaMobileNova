package com.grappim.taigamobile.testing.utils

import io.github.vinceglb.filekit.PlatformFile

expect fun createTestPlatformFile(name: String, bytes: ByteArray): PlatformFile
