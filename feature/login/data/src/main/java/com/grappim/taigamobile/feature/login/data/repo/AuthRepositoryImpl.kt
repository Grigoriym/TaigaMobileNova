package com.grappim.taigamobile.feature.login.data.repo

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.AuthStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.login.data.model.AuthRequest
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val serverStorage: ServerStorage,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val authStorage: AuthStorage,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : AuthRepository {

    override suspend fun auth(authData: AuthData): Result<Unit> = resultOf {
        withContext(dispatcher) {
            val server = authData.taigaServer.removeTrailingSlashes()
            serverStorage.defineServer(server)
            val response = authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
            authStorage.setAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh
            )
            taigaSessionStorage.setUserId(response.id)
        }
    }

    private fun String.removeTrailingSlashes() = this.trimEnd('/')
}
