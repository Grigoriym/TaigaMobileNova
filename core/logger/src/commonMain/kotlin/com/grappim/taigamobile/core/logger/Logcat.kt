package com.grappim.taigamobile.core.logger

inline fun Any.logcat(
    priority: LogPriority = LogPriority.DEBUG,
    tag: String? = null,
    throwable: Throwable? = null,
    noinline message: () -> String
) {
    TaigaLogger.logger.log(
        priority = priority,
        tag = tag ?: this::class.simpleName,
        throwable = throwable,
        message = message
    )
}

inline fun logcat(
    priority: LogPriority = LogPriority.DEBUG,
    tag: String? = null,
    throwable: Throwable? = null,
    noinline message: () -> String
) {
    TaigaLogger.logger.log(
        priority = priority,
        tag = tag,
        throwable = throwable,
        message = message
    )
}
