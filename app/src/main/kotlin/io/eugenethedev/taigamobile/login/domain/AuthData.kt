package io.eugenethedev.taigamobile.login.domain

import io.eugenethedev.taigamobile.domain.entities.AuthType

data class AuthData(
    val taigaServer: String,
    val authType: AuthType,
    val password: String,
    val username: String
)
