package com.grappim.taigamobile.core.storage.auth

import com.grappim.taigamobile.core.asynckmp.ApplicationScope
import com.grappim.taigamobile.core.storage.KmpSession
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.wrapper.DatabaseWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class AuthStateManager(
    private val session: KmpSession,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val authStorage: AuthStorage,
    private val databaseWrapper: DatabaseWrapper,
    @param:ApplicationScope private val applicationScope: CoroutineScope
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
