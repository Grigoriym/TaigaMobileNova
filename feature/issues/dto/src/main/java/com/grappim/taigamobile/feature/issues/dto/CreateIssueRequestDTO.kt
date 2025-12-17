package com.grappim.taigamobile.feature.issues.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateIssueRequestDTO(val project: Long, val subject: String, val description: String, val milestone: Long?)
