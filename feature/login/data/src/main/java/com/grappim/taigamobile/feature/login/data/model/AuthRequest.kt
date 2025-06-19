package com.grappim.taigamobile.feature.login.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRequest(
    val password: String,
    val username: String,
    val type: String
)
