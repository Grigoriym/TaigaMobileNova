package com.grappim.taigamobile.feature.login.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(@SerialName(value = "auth_token") val authToken: String, val refresh: String)
