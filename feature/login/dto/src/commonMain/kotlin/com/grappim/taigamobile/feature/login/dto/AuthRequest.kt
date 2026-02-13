package com.grappim.taigamobile.feature.login.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val password: String, val username: String, val type: String)
