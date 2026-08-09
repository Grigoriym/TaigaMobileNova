package com.grappim.taigamobile.core.logger

import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val MAX_LOG_FILE_BYTES = 5L * 1024 * 1024
private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

/**
 * Writes log lines to [logFile], rotating it to `<name>.old` once it exceeds
 * [MAX_LOG_FILE_BYTES] so a long-running desktop session can't grow the file unbounded.
 */
class FileLogger(private val logFile: File) : TaigaLogger {

    init {
        logFile.parentFile?.mkdirs()
    }

    override fun log(priority: LogPriority, tag: String?, throwable: Throwable?, message: () -> String) {
        val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
        val prefix = tag?.let { "[$it] " } ?: ""
        val throwableText = throwable?.let { "\n${it.stackTraceToString()}" } ?: ""
        val line = "$timestamp ${priority.name.first()}/$prefix${message()}$throwableText\n"
        synchronized(this) {
            if (logFile.length() > MAX_LOG_FILE_BYTES) {
                rotate()
            }
            FileWriter(logFile, true).use { it.write(line) }
        }
    }

    private fun rotate() {
        val oldFile = File(logFile.parentFile, "${logFile.name}.old")
        oldFile.delete()
        logFile.renameTo(oldFile)
    }

    companion object {
        fun install(logFile: File) {
            if (!TaigaLogger.isInstalled) {
                TaigaLogger.install(FileLogger(logFile))
            }
        }
    }
}
