package com.grappim.taigamobile.core.logger

import platform.Foundation.NSLog

class NSLogLogger : TaigaLogger {

    override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String) {
        val prefix = tag?.let { "[$it] " } ?: ""
        val priorityLabel = priority.name.first()
        val throwableText = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        NSLog("$priorityLabel/$prefix${message()}$throwableText")
    }

    companion object {
        fun install() {
            if (!TaigaLogger.isInstalled) {
                TaigaLogger.install(NSLogLogger())
            }
        }
    }
}
