package com.grappim.taigamobile.feature.login.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val password: String, val username: String, val type: String)
