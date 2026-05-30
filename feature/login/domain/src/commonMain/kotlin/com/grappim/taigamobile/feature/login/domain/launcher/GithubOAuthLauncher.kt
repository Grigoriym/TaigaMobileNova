package com.grappim.taigamobile.feature.login.domain.launcher

interface GithubOAuthLauncher {
    suspend fun launch(baseAuthUrl: String): String
}
