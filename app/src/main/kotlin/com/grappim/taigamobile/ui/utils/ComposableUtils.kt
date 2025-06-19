package com.grappim.taigamobile.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.strings.RString
import kotlin.math.ln

/**
 * Utility function to handle press on back button
 */
@SuppressLint("ComposableNaming")
@Composable
fun onBackPressed(action: () -> Unit) {
    LocalContext
    (LocalContext.current as? OnBackPressedDispatcherOwner)?.onBackPressedDispatcher?.let { dispatcher ->
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
inline fun <T : Any> LazyPagingItems<T>.SubscribeOnError(crossinline onError: (message: Int) -> Unit) {
    LaunchedEffect(loadState.hasError) {
        if (loadState.hasError) {
            onError(RString.common_error_message)
        }
    }
}
