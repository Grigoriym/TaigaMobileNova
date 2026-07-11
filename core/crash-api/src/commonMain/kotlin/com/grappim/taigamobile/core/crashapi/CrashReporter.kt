package com.grappim.taigamobile.core.crashapi

interface CrashReporter {
    val isAvailable: Boolean

    fun setCollectionEnabled(enabled: Boolean)
    fun recordException(throwable: Throwable)
    fun log(message: String)
}
