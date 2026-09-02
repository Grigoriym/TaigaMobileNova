package com.grappim.taigamobile.core.storage.auth

import kotlin.test.Test
import kotlin.test.assertEquals

class NoopTokenCipherTest {

    private val sut = NoopTokenCipher()

    @Test
    fun `encrypt returns the plaintext unchanged`() {
        assertEquals("access-token", sut.encrypt("access-token"))
    }

    @Test
    fun `decrypt returns the value unchanged`() {
        assertEquals("access-token", sut.decrypt("access-token"))
    }
}
