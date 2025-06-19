package com.grappim.taigamobile.feature.login.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "auth_token") val authToken: String,
    val refresh: String?,
    val id: Long
)
