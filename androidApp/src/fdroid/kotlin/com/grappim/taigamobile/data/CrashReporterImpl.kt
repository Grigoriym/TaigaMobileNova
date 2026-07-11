package com.grappim.taigamobile.data

import com.grappim.taigamobile.core.crashapi.CrashReporter
import org.koin.core.annotation.Single

@Single(binds = [CrashReporter::class])
class CrashReporterImpl : CrashReporter {
    override val isAvailable: Boolean = false

    override fun setCollectionEnabled(enabled: Boolean) = Unit
    override fun recordException(throwable: Throwable) = Unit
    override fun log(message: String) = Unit
}
