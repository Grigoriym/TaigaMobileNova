package com.grappim.taigamobile.feature.login.domain.model

data class AuthData(
    val taigaServer: String,
    val authType: AuthType,
    val password: String,
    val username: String
)
