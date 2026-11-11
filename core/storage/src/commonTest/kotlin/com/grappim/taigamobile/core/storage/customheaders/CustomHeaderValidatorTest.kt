package com.grappim.taigamobile.core.storage.customheaders

import kotlin.test.Test
import kotlin.test.assertEquals

class CustomHeaderValidatorTest {

    private fun validateOne(name: String, value: String = "v") =
        CustomHeaderValidator.validate(listOf(CustomHeader(name, value))).single()

    @Test
    fun `a well-formed header is valid`() {
        assertEquals(null, validateOne("CF-Access-Client-Id", "abc.123 with spaces\tand tab"))
    }

    @Test
    fun `a row with both fields blank is not an error`() {
        assertEquals(null, validateOne(" ", ""))
    }

    @Test
    fun `a value with no name is MissingName`() {
        assertEquals(CustomHeaderError.MissingName, validateOne("", "secret"))
    }

    @Test
    fun `delimiters, spaces and non-ASCII in the name are InvalidName`() {
        listOf("X Token", "X:Token", "X(Token)", "Tökén", "X/Y").forEach {
            assertEquals(CustomHeaderError.InvalidName, validateOne(it), it)
        }
    }

    @Test
    fun `control characters and non-ASCII in the value are InvalidValue`() {
        listOf("a\nb", "a\rb", "naïve").forEach {
            assertEquals(CustomHeaderError.InvalidValue, validateOne("X-Token", it), it)
        }
    }

    @Test
    fun `reserved names are refused regardless of case`() {
        listOf(
            "Host",
            "content-length",
            "Content-Type",
            "Transfer-Encoding",
            "upgrade",
            "Connection",
            "Cookie",
            "authorization",
            "User-Agent"
        ).forEach {
            assertEquals(CustomHeaderError.ReservedName, validateOne(it), it)
        }
    }

    @Test
    fun `a repeated name is DuplicateName on the second row only, ignoring case`() {
        val result = CustomHeaderValidator.validate(
            listOf(CustomHeader("X-Token", "a"), CustomHeader("x-token", "b"), CustomHeader("X-Other", "c"))
        )

        assertEquals(listOf(null, CustomHeaderError.DuplicateName, null), result)
    }
}
