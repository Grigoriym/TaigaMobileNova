package com.grappim.taigamobile.feature.login.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RefreshTokenRequest(val refresh: String)
