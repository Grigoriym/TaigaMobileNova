package com.grappim.taigamobile.core.api

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ExceptionSanitizationTest {

    @Test
    fun `sanitizedForCrashReporting replaces the message with the exception class name`() {
        val original = RuntimeException("""Unable to resolve host "myserver.example.com"""")

        val sanitized = original.sanitizedForCrashReporting()

        assertEquals("RuntimeException", sanitized.message)
    }

    @Test
    fun `sanitizedForCrashReporting drops the original message text`() {
        val original = IllegalStateException("connection refused to myserver.example.com:443")

        val sanitized = original.sanitizedForCrashReporting()

        assertFalse(sanitized.message.orEmpty().contains("myserver.example.com"))
    }
}
