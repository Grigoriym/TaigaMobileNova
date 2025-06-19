package com.grappim.taigamobile.feature.login.domain.repo

import com.grappim.taigamobile.feature.login.domain.model.AuthData

interface IAuthRepository {
    suspend fun auth(authData: AuthData): Result<Unit>
}
