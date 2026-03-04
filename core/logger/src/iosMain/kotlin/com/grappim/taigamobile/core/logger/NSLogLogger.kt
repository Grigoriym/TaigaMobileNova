package com.grappim.taigamobile.core.logger

import platform.Foundation.NSLog

class NSLogLogger : TaigaLogger {

    override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String) {
        val prefix = tag?.let { "[$it] " } ?: ""
        val priorityLabel = priority.name.first()
        val throwableText = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        val fullMessage = "$priorityLabel/$prefix${message()}$throwableText"
        // NSLog truncates at ~4096 bytes — split into safe chunks to avoid losing response bodies
        fullMessage.chunked(3000).forEach { chunk ->
            NSLog("%s", chunk)
        }
    }

    companion object {
        fun install() {
            if (!TaigaLogger.isInstalled) {
                TaigaLogger.install(NSLogLogger())
            }
        }
    }
}
