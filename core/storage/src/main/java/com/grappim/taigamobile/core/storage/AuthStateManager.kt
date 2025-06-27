package com.grappim.taigamobile.core.storage

import com.grappim.taigamobile.core.async.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateManager @Inject constructor(
    private val session: Session,
    private val taigaStorage: TaigaStorage,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val _logoutEvents = MutableSharedFlow<LogoutEvent>()
    val logoutEvents: SharedFlow<LogoutEvent> = _logoutEvents.asSharedFlow()

    suspend fun logoutSuspend() {
        session.reset()
        taigaStorage.clearData()
        _logoutEvents.emit(LogoutEvent.UserInitiated)
    }

    fun logout() {
        applicationScope.launch {
            logoutSuspend()
        }
    }
}
