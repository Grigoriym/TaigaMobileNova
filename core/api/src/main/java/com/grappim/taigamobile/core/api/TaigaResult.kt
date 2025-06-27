package com.grappim.taigamobile.core.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed interface TaigaResult<out T> {
    data class Success<T>(val data: T) : TaigaResult<T>
    data class Error(val exception: Throwable) : TaigaResult<Nothing>
    data object Loading : TaigaResult<Nothing>
}

fun <T> Flow<T>.asResult(): Flow<TaigaResult<T>> =
    map<T, TaigaResult<T>> { TaigaResult.Success(it) }
        .onStart { emit(TaigaResult.Loading) }
        .catch { emit(TaigaResult.Error(it)) }
