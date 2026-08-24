package com.grappim.taigamobile.core.api

import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> T.defaultTryCatch(block: T.() -> R, catchBlock: T.(Exception) -> R): R = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    catchBlock(e)
}
