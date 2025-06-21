package com.grappim.taigamobile.feature.login.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @Json(name = "auth_token") val authToken: String,
    val refresh: String
)
