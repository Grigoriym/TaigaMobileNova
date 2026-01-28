package com.grappim.taigamobile.utils.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.strings.RString

fun CombinedLoadStates.getErrorMessage(
    fallback: NativeText = NativeText.Resource(RString.error_loading_data)
): NativeText {
    val error = (refresh as? LoadState.Error)?.error
    return error?.let { getErrorMessage(it) } ?: fallback
}

fun LazyPagingItems<*>.isEmpty(): Boolean = itemCount == 0
fun LazyPagingItems<*>.isNotEmpty(): Boolean = isEmpty().not()

fun LazyPagingItems<*>.hasError(): Boolean = loadState.hasError
fun LazyPagingItems<*>.isLoading(): Boolean = loadState.refresh is LoadState.Loading
