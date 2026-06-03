package com.grappim.taigamobile.feature.login.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TaigaConfJson(@SerialName("gitHubClientId") val gitHubClientId: String? = null)
