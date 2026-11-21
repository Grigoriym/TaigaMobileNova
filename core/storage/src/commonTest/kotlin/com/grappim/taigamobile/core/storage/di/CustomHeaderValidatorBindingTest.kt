package com.grappim.taigamobile.core.storage.di

import com.grappim.kit.customheaders.CustomHeader
import com.grappim.kit.customheaders.CustomHeaderError
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomHeaderValidatorBindingTest {

    private val sut = StorageModule().provideCustomHeaderValidator()

    private fun validateOne(name: String) = sut.validate(listOf(CustomHeader(name, "v"))).single()

    @Test
    fun `Authorization and User-Agent are reserved regardless of case`() {
        listOf("Authorization", "authorization", "User-Agent", "user-agent").forEach {
            assertEquals(CustomHeaderError.ReservedName, validateOne(it), it)
        }
    }

    @Test
    fun `the kit's default reserved names are still reserved`() {
        listOf("Host", "content-length", "Content-Type", "Transfer-Encoding", "upgrade", "Connection", "Cookie")
            .forEach { assertEquals(CustomHeaderError.ReservedName, validateOne(it), it) }
    }

    @Test
    fun `an ordinary proxy header is accepted`() {
        assertEquals(null, validateOne("CF-Access-Client-Id"))
    }
}
