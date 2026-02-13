package com.grappim.taigamobile.feature.login.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(@SerialName(value = "auth_token") val authToken: String, val refresh: String, val id: Long)
