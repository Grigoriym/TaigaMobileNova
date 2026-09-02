@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.core.asynckmp

import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.TaigaLogger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class KmpCoroutinesModuleTest {

    private class RecordingLogger : TaigaLogger {
        var priority: LogPriority? = null
        var throwable: Throwable? = null

        override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String) {
            this.priority = priority
            this.throwable = throwable
        }
    }

    private val sut = KmpCoroutinesModule()

    @AfterTest
    fun tearDown() {
        TaigaLogger.uninstall()
    }

    @Test
    fun `exception thrown on ApplicationScope is logged, not propagated`() = runTest {
        val recordingLogger = RecordingLogger()
        TaigaLogger.install(recordingLogger)
        val exception = IllegalStateException("boom")
        val scope = sut.provideApplicationScope(UnconfinedTestDispatcher(testScheduler))

        scope.launch { throw exception }

        assertEquals(LogPriority.ERROR, recordingLogger.priority)
        assertSame(exception, recordingLogger.throwable)
    }
}
