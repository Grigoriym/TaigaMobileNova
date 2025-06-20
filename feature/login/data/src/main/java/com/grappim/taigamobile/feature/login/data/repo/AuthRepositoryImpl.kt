package com.grappim.taigamobile.feature.login.data.repo

import com.grappim.taigamobile.core.api.resultOf
import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.login.data.api.AuthApi
import com.grappim.taigamobile.feature.login.data.model.AuthRequest
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val session: Session,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : AuthRepository {

    companion object {
        /**
         * Compatibility with older Taiga versions without refresh token
         */
        private const val MISSING_REFRESH_TOKEN = "missing"
    }

    override suspend fun auth(authData: AuthData): Result<Unit> = withContext(dispatcher) {
        resultOf {
            val server = authData.taigaServer.removeTrailingSlashes()
            session.changeServer(server)
            val response = authApi.auth(
                AuthRequest(
                    username = authData.username,
                    password = authData.password,
                    type = authData.authType.value
                )
            )
            session.changeAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh ?: MISSING_REFRESH_TOKEN
            )
            session.changeCurrentUserId(response.id)
        }
    }

    private fun String.removeTrailingSlashes() = this.trimEnd('/')
}
