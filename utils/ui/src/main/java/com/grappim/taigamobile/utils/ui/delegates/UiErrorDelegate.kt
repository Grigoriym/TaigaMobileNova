package com.grappim.taigamobile.utils.ui.delegates

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.receiveAsFlow

interface UiErrorDelegate {
    val uiError: Flow<NativeText>
    suspend fun showUiErrorSuspend(message: NativeText)
}

class UiErrorDelegateImpl : UiErrorDelegate {
    private val _uiError = Channel<NativeText>()
    override val uiError: Flow<NativeText>
        get() = _uiError.receiveAsFlow()

    override suspend fun showUiErrorSuspend(message: NativeText) {
        _uiError.send(message)
    }
}

@Composable
fun SharedFlow<NativeText>.collectUiError() = collectAsStateWithLifecycle(
    initialValue = NativeText.Empty
)
