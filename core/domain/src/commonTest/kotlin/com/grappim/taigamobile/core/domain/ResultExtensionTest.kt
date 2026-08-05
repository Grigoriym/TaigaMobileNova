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
import kotlin.test.assertIs
import kotlin.test.assertNull
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

    // region mapResult

    @Test
    fun `mapResult transforms the value of a success`() {
        val value = getRandomString()

        val result = Result.success(value).mapResult { it.length }

        assertEquals(value.length, result.getOrNull())
    }

    @Test
    fun `mapResult keeps the original exception of a failure`() {
        val result = Result.failure<String>(testException).mapResult { it.length }

        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `mapResult captures an exception thrown by the transform`() {
        val result = Result.success(getRandomString()).mapResult { throw testException }

        assertTrue(result.isFailure)
        assertEquals(testException, result.exceptionOrNull())
    }

    @Test
    fun `mapResult rethrows a CancellationException thrown by the transform`() {
        assertFailsWith<CancellationException> {
            Result.success(getRandomString()).mapResult { throw CancellationException("cancelled") }
        }
    }

    /**
     * Documents a latent trap rather than desired behaviour: `mapResult` decides success from
     * `getOrNull() != null`, so a *successful* result holding `null` falls into the `else` branch,
     * finds no exception and throws `error("Unreachable state")` — the state the message calls
     * unreachable is reachable. Harmless today only because `mapResult` has no call sites at all.
     * Filed as docs/revisit.md #20.
     */
    @Test
    fun `mapResult throws on a success holding null`() {
        val thrown = assertFailsWith<IllegalStateException> {
            Result.success<String?>(null).mapResult { it?.length }
        }

        assertEquals("Unreachable state", thrown.message)
    }

    @Test
    fun `mapResult transforms a success into null without failing`() {
        val result = Result.success(getRandomString()).mapResult { null }

        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }

    // endregion

    @Test
    fun `resultOf feeding mapResult carries a failure through both`() {
        val result = resultOf { throw testException }.mapResult { "unreachable" }

        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(testException.message, result.exceptionOrNull()?.message)
    }
}
