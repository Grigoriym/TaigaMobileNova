package com.grappim.taigamobile.feature.login.dto

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refresh: String)
