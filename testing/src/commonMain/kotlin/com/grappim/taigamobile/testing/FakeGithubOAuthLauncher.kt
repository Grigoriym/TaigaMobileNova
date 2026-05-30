package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.login.domain.launcher.GithubOAuthLauncher

class FakeGithubOAuthLauncher : GithubOAuthLauncher {
    var launchResult: Result<String> = Result.success("")
    val launchCalls = mutableListOf<String>()

    override suspend fun launch(baseAuthUrl: String): String {
        launchCalls += baseAuthUrl
        return launchResult.getOrThrow()
    }
}
