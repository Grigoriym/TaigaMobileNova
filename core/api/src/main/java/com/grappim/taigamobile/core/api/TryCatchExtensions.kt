package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.domain.NetworkException
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> T.defaultTryCatch(block: T.() -> R, catchBlock: T.(Exception) -> R): R = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (e: TimeoutCancellationException) {
    throw e
} catch (e: Exception) {
    catchBlock(e)
}

inline fun <T, R> T.tryCatchWithPagination(
    block: T.() -> R,
    catchBlock: T.(Exception) -> R,
    onPaginationEnd: T.() -> R
): R = try {
    block()
} catch (e: NetworkException) {
    if (e.errorCode == NetworkException.ERROR_404_PAGINATION) {
        onPaginationEnd()
    } else {
        catchBlock(e)
    }
} catch (e: CancellationException) {
    throw e
} catch (e: TimeoutCancellationException) {
    throw e
} catch (e: Exception) {
    catchBlock(e)
}
