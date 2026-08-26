package com.grappim.taigamobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Hand-rolled stand-in for Nav3's `ResultEventBus`/`LocalResultEventBus`/`ResultEffect` recipe
 * (`androidx.navigation3.runtime.result`, see the `navigation-3` skill's
 * `recipes/results-event.md`). That package only ships starting
 * `androidx.navigation3:navigation3-runtime` 1.2.0-alpha04 — this project is pinned to the stable
 * 1.1.1 line (`jetbrainsNav3` in `gradle/libs.versions.toml`), which predates it, and the
 * JetBrains KMP-published `navigation3-ui` (needed for iOS/desktop) only reaches 1.2.0-alpha02,
 * which itself pulls that 1.2.0-alpha04 runtime — so getting the real API means adopting an alpha
 * dependency across the whole Nav3 stack. Mirrors the upstream shape (same method names, same
 * buffered-channel-per-key semantics) so call sites read the same and a future swap to the real
 * thing is a search-and-replace, not a rewrite.
 */
class ResultBus {
    internal val channelMap: SnapshotStateMap<String, Channel<Any?>> = mutableStateMapOf()

    internal fun getResultFlow(resultKey: String): Flow<Any?> = channel(resultKey).receiveAsFlow()

    fun <T> sendResult(resultKey: String, result: T) {
        channel(resultKey).trySend(result)
    }

    private fun channel(resultKey: String): Channel<Any?> = channelMap.getOrPut(resultKey) {
        Channel(capacity = Channel.BUFFERED, onBufferOverflow = BufferOverflow.SUSPEND)
    }
}

inline fun <reified T> ResultBus.sendResult(result: T) {
    sendResult(T::class.toString(), result)
}

@Composable
fun rememberResultBus(): ResultBus = remember { ResultBus() }

val LocalResultBus = staticCompositionLocalOf<ResultBus> {
    error("No ResultBus has been provided")
}

@Composable
fun <T> ResultEffect(resultKey: String, resultBus: ResultBus = LocalResultBus.current, onResult: suspend (T) -> Unit) {
    val currentOnResult by rememberUpdatedState(onResult)
    LaunchedEffect(resultKey, resultBus.channelMap[resultKey]) {
        resultBus.getResultFlow(resultKey).collect { result ->
            @Suppress("UNCHECKED_CAST")
            currentOnResult(result as T)
        }
    }
}

@Composable
inline fun <reified T> ResultEffect(
    resultBus: ResultBus = LocalResultBus.current,
    noinline onResult: suspend (T) -> Unit
) {
    ResultEffect(T::class.toString(), resultBus, onResult)
}
