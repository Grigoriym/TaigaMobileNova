package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository

class FakeAuthRepository : AuthRepository {
    var authResult: Result<Unit> = Result.success(Unit)
    var authCalledWith: AuthData? = null
    var authCallCount: Int = 0

    override suspend fun auth(authData: AuthData): Result<Unit> {
        authCalledWith = authData
        authCallCount++
        return authResult
    }
}
