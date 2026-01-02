package com.grappim.taigamobile.core.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaigaErrorResponse(
    @SerialName("_error_message") val errorMessage: String? = null,
    @SerialName("_error_type") val errorType: String? = null
)
