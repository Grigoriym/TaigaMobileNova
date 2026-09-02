package com.grappim.taigamobile.core.domain

import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * [resultOf] is the most widely used helper in the project — ~140 call sites, and every use case
 * and repository failure path runs through it. Its whole reason to exist is that `runCatching`
 * swallows [CancellationException] and so breaks structured concurrency; nothing asserted that
 * until this file.
 *
 * These tests cannot move the coverage report: `ResultExtensionKt` is named in the root
 * `build.gradle.kts` `excludes` block, and the functions are `inline` besides, so they have no
 * class of their own to measure. That is a known and accepted property — see the `core/api`
 * precedent in docs/testing/improvement-plan.md.
 */
class ResultExtensionTest {

    // region resultOf — top-level overload

    @Test
    fun `resultOf wraps the block result in a success`() {
        val value = getRandomString()

        val result = resultOf { value }

        assertEquals(value, result.getOrNull())
    }

    @Test
    fun `resultOf captures a thrown exception as a failure`() {
        val result = resultOf { throw testException }

        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `resultOf rethrows CancellationException instead of capturing it`() {
        assertFailsWith<CancellationException> {
            resultOf { throw CancellationException("cancelled") }
        }
    }

    @Test
    fun `resultOf lets a real coroutine cancellation propagate`() = runTest {
        val started = CompletableDeferred<Unit>()

        val job = async {
            resultOf {
                started.complete(Unit)
                awaitCancellation()
            }
        }

        started.await()
        job.cancel()

        assertTrue(job.isCancelled)
    }

    @Test
    fun `resultOf lets a withTimeout cancellation propagate`() = runTest {
        assertFailsWith<TimeoutCancellationException> {
            withTimeout(timeMillis = 10) {
                resultOf { awaitCancellation() }
            }
        }
    }

    // endregion

    // region resultOf — receiver overload

    @Test
    fun `receiver resultOf exposes the receiver to the block and wraps the result`() {
        val receiver = getRandomString()

        val result = receiver.resultOf { uppercase() }

        assertEquals(receiver.uppercase(), result.getOrNull())
    }

    @Test
    fun `receiver resultOf captures a thrown exception as a failure`() {
        val result = getRandomString().resultOf { throw testException }

        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `receiver resultOf rethrows CancellationException instead of capturing it`() {
        assertFailsWith<CancellationException> {
            getRandomString().resultOf { throw CancellationException("cancelled") }
        }
    }

    // endregion
}
