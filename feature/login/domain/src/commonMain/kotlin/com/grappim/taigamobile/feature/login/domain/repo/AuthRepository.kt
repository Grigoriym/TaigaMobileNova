package com.grappim.taigamobile.feature.login.domain.repo

import com.grappim.taigamobile.feature.login.domain.model.AuthData

interface AuthRepository {
    suspend fun auth(authData: AuthData): Result<Unit>
    suspend fun getGithubClientId(server: String): Result<String>
    suspend fun authWithGithub(code: String): Result<Unit>
}
