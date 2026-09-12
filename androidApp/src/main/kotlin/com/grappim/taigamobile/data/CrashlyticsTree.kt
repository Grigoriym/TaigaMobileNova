package com.grappim.taigamobile.data

import android.util.Log
import com.grappim.kit.crash.CrashReporter
import timber.log.Timber

class CrashlyticsTree(private val crashReporter: CrashReporter) : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority != Log.ERROR || t == null) return

        crashReporter.log(message)
        crashReporter.recordException(t)
    }
}