package com.grappim.taigamobile.feature.login.domain.repo

import com.grappim.taigamobile.feature.login.domain.model.AuthData

interface AuthRepository {
    suspend fun auth(authData: AuthData): Result<Unit>
}
