package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.login.domain.model.AuthData
import com.grappim.taigamobile.feature.login.domain.repo.AuthRepository

class FakeAuthRepository : AuthRepository {
    var authResult: Result<Unit> = Result.success(Unit)
    var authCalledWith: AuthData? = null
    var authCallCount: Int = 0

    var githubClientIdResult: Result<String> = Result.success("")
    var githubClientIdCalls = mutableListOf<String>()

    var authWithGithubResult: Result<Unit> = Result.success(Unit)
    var authWithGithubCalls = mutableListOf<String>()

    override suspend fun auth(authData: AuthData): Result<Unit> {
        authCalledWith = authData
        authCallCount++
        return authResult
    }

    override suspend fun getGithubClientId(server: String): Result<String> {
        githubClientIdCalls += server
        return githubClientIdResult
    }

    override suspend fun authWithGithub(code: String): Result<Unit> {
        authWithGithubCalls += code
        return authWithGithubResult
    }
}
