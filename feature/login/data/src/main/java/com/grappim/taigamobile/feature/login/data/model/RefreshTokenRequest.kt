package com.grappim.taigamobile.feature.login.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(val refresh: String)
