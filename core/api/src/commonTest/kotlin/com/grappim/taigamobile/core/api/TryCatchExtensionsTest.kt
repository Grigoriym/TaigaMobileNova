package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame

class TryCatchExtensionsTest {

    @Test
    fun `on successful block should return its result and not invoke catchBlock`() {
        var catchCalled = false

        val actual = "receiver".defaultTryCatch(
            block = { "$this-result" },
            catchBlock = {
                catchCalled = true
                "fallback"
            }
        )

        assertEquals("receiver-result", actual)
        assertFalse(catchCalled)
    }

    @Test
    fun `on exception should invoke catchBlock with it and return its result`() {
        var caught: Exception? = null

        val actual = "receiver".defaultTryCatch(
            block = { throw testException },
            catchBlock = {
                caught = it
                "fallback"
            }
        )

        assertEquals("fallback", actual)
        assertSame(testException, caught)
    }

    @Test
    fun `on CancellationException should rethrow and not invoke catchBlock`() {
        var catchCalled = false
        val cancellation = CancellationException("cancelled")

        val thrown = assertFailsWith<CancellationException> {
            Unit.defaultTryCatch(
                block = { throw cancellation },
                catchBlock = {
                    catchCalled = true
                }
            )
        }

        assertSame(cancellation, thrown)
        assertFalse(catchCalled)
    }

    @Test
    fun `on TimeoutCancellationException should rethrow and not invoke catchBlock`() = runTest {
        var catchCalled = false

        assertFailsWith<TimeoutCancellationException> {
            Unit.defaultTryCatch(
                block = { withTimeout(10) { delay(100) } },
                catchBlock = {
                    catchCalled = true
                }
            )
        }

        assertFalse(catchCalled)
    }
}
