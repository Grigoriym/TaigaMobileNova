package com.grappim.taigamobile.data

import com.grappim.taigamobile.core.crashapi.CrashReporter
import org.koin.core.annotation.Single

// No-op by design: there is no Crashlytics-equivalent backend on desktop to report to. Uncaught
// exceptions are still captured — see TaigaMobileDesktop.kt's Thread.setDefaultUncaughtExceptionHandler,
// which logs via core/logger's FileLogger instead of this interface.
@Single(binds = [CrashReporter::class])
class CrashReporterImpl : CrashReporter {
    override val isAvailable: Boolean = false

    override fun setCollectionEnabled(enabled: Boolean) = Unit
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
}
