package com.grappim.taigamobile.login.data

import com.grappim.taigamobile.core.resultOf
import com.grappim.taigamobile.data.api.AuthRequest
import com.grappim.taigamobile.di.IoDispatcher
import com.grappim.taigamobile.login.domain.AuthData
import com.grappim.taigamobile.login.domain.IAuthRepository
import com.grappim.taigamobile.state.Session
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val session: Session,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : IAuthRepository {

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
