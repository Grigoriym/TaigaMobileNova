package com.grappim.taigamobile.feature.workitem.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusExtraInfoDTO(val color: String, val name: String)
