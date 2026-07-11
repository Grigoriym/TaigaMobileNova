package com.grappim.taigamobile.data

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.grappim.taigamobile.core.crashapi.CrashReporter
import org.koin.core.annotation.Single

@Single(binds = [CrashReporter::class])
class CrashReporterImpl : CrashReporter {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    override val isAvailable: Boolean = true

    override fun setCollectionEnabled(enabled: Boolean) {
        crashlytics.setCrashlyticsCollectionEnabled(enabled)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }
}
