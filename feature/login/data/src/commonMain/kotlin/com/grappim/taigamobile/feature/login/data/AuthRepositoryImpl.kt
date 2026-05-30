package com.grappim.taigamobile.feature.login.data

import com.grappim.taigamobile.core.asynckmp.IoDispatcher
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.model.AuthType
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository
import com.grappim.taigamobile.feature.login.dto.AuthRequest
import com.grappim.taigamobile.feature.login.dto.GithubAuthRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [AuthRepository::class])
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val serverStorage: ServerStorage,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val authStorage: AuthStorage,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
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

    override suspend fun getGithubClientId(server: String): Result<String> = resultOf {
        withContext(dispatcher) {
            val cleanServer = server.removeTrailingSlashes()
            serverStorage.defineServer(cleanServer)
            authApi.getConfJson(cleanServer).gitHubClientId
                ?: error("GitHub auth is not configured on this server")
        }
    }

    override suspend fun authWithGithub(code: String): Result<Unit> = resultOf {
        withContext(dispatcher) {
            val response = authApi.githubAuth(GithubAuthRequest(code = code, type = AuthType.GITHUB.value))
            authStorage.setAuthCredentials(
                token = response.authToken,
                refreshToken = response.refresh
            )
            taigaSessionStorage.setUserId(response.id)
        }
    }

    private fun String.removeTrailingSlashes() = this.trimEnd('/')
}
