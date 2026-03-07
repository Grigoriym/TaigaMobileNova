package com.grappim.taigamobile.core.logger

import com.grappim.taigamobile.core.logger.TaigaLogger.Companion.install
import com.grappim.taigamobile.core.logger.TaigaLogger.Companion.uninstall
import kotlin.concurrent.Volatile

/**
 * Logger that [logcat] delegates to. Call [install] to set a logger,
 * the default is a no-op logger. Call [uninstall] to revert to no-op.
 */
interface TaigaLogger {

    fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String)

    companion object {
        @Volatile
        @PublishedApi
        internal var logger: TaigaLogger = NoLog
            private set

        val isInstalled: Boolean
            get() = logger !== NoLog

        fun install(logger: TaigaLogger) {
            this.logger = logger
        }

        fun uninstall() {
            logger = NoLog
        }
    }

    private object NoLog : TaigaLogger {
        override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String) {
            // no-op
        }
    }
}
