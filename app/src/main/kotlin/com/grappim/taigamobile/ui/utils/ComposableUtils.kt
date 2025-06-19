package com.grappim.taigamobile.ui.utils

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.strings.RString

/**
 * Utility function to handle press on back button
 */
@Composable
@Deprecated("remove it")
fun OnBackPressed(action: () -> Unit) {
    LocalContext
    (LocalContext.current as? OnBackPressedDispatcherOwner)
        ?.onBackPressedDispatcher
        ?.let { dispatcher ->
            val callback = remember {
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        action()
                        remove()
                    }
                }.also {
                    dispatcher.addCallback(it)
                }
            }

            DisposableEffect(Unit) {
                onDispose(callback::remove)
            }
        }
}

@Composable
inline fun Result<*>.SubscribeOnError(crossinline onError: (message: Int) -> Unit) =
    (this as? ErrorResult)?.message?.let {
        LaunchedEffect(this) {
            onError(it)
        }
    }

@Composable
inline fun <T : Any> LazyPagingItems<T>.SubscribeOnError(
    crossinline onError: (message: Int) -> Unit
) {
    LaunchedEffect(loadState.hasError) {
        if (loadState.hasError) {
            onError(RString.common_error_message)
        }
    }
}
