package com.grappim.taigamobile.feature.workitem.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateWorkItemRequestDTO(val project: Long, val subject: String, val description: String, val status: Long?)
