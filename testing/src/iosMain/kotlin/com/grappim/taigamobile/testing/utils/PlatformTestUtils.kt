package com.grappim.taigamobile.testing.utils

import io.github.vinceglb.filekit.PlatformFile

actual fun createTestPlatformFile(name: String, bytes: ByteArray): PlatformFile =
    error("createTestPlatformFile is not supported in iOS tests")
