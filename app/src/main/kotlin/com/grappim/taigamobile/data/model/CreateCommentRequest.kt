package com.grappim.taigamobile.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateCommentRequest(val comment: String, val version: Long)
