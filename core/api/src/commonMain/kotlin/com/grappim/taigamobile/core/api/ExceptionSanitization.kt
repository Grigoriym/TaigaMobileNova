package com.grappim.taigamobile.core.api

// Network failures (DNS/connect/TLS) often embed the server hostname in their message; strip it before Crashlytics.
internal fun Throwable.sanitizedForCrashReporting(): Throwable = Exception(this::class.simpleName ?: "UnknownException")
