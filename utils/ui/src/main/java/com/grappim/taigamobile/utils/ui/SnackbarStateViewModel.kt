package com.grappim.taigamobile.utils.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface SnackbarStateViewModel {
    val snackBarMessage: SharedFlow<NativeText>
    suspend fun showSnackbarSuspend(message: NativeText)
}

class SnackbarStateViewModelImpl : SnackbarStateViewModel {
    private val _snackBarMessage = MutableSharedFlow<NativeText>()
    override val snackBarMessage: SharedFlow<NativeText>
        get() = _snackBarMessage.asSharedFlow()

    override suspend fun showSnackbarSuspend(message: NativeText) {
        _snackBarMessage.emit(message)
    }
}

@Composable
fun SharedFlow<NativeText>.collectSnackbarMessage() = collectAsStateWithLifecycle(
    initialValue = NativeText.Empty
)
