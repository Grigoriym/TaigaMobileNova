package io.eugenethedev.taigamobile.login.data

import io.eugenethedev.taigamobile.core.resultOf
import io.eugenethedev.taigamobile.data.api.AuthRequest
import io.eugenethedev.taigamobile.di.IoDispatcher
import io.eugenethedev.taigamobile.login.domain.AuthData
import io.eugenethedev.taigamobile.login.domain.IAuthRepository
import io.eugenethedev.taigamobile.state.Session
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
