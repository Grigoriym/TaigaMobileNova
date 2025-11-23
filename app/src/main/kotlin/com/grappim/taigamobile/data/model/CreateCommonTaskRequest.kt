package com.grappim.taigamobile.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCommonTaskRequest(val project: Long, val subject: String, val description: String, val status: Long?)
