package com.grappim.taigamobile.data.interceptors

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaigaErrorResponseDTO(
    @param:Json(name = "_error_message")
    val errorMessage: String? = null
)
