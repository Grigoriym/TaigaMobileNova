package com.grappim.taigamobile.login.domain

import com.grappim.taigamobile.domain.entities.AuthType

data class AuthData(
    val taigaServer: String,
    val authType: AuthType,
    val password: String,
    val username: String
)
