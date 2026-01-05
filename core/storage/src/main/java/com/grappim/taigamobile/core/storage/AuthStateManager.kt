package com.grappim.taigamobile.core.storage

import com.grappim.taigamobile.core.async.ApplicationScope
import com.grappim.taigamobile.core.storage.db.wrapper.DatabaseWrapper
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
    private val taigaSessionStorage: TaigaSessionStorage,
    private val authStorage: AuthStorage,
    private val databaseWrapper: DatabaseWrapper,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val _logoutEvents = MutableSharedFlow<LogoutEvent>()
    val logoutEvents: SharedFlow<LogoutEvent> = _logoutEvents.asSharedFlow()

    suspend fun logoutSuspend() {
        session.reset()
        taigaSessionStorage.clearData()
        authStorage.clear()
        databaseWrapper.clearAllTables()
        _logoutEvents.emit(LogoutEvent.UserInitiated)
    }

    fun logout() {
        applicationScope.launch {
            logoutSuspend()
        }
    }
}
