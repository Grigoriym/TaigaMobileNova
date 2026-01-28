package com.grappim.taigamobile.utils.ui.delegates

import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

interface SnackbarDelegate {
    val snackBarMessage: Flow<NativeText>
    suspend fun showSnackbarSuspend(message: NativeText)
}

class SnackbarDelegateImpl : SnackbarDelegate {
    private val _snackBarMessage = Channel<NativeText>()
    override val snackBarMessage: Flow<NativeText>
        get() = _snackBarMessage.receiveAsFlow()

    override suspend fun showSnackbarSuspend(message: NativeText) {
        _snackBarMessage.send(message)
    }
}
