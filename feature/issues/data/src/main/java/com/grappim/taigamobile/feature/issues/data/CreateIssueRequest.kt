package com.grappim.taigamobile.feature.issues.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreateIssueRequest(
    val project: Long,
    val subject: String,
    val description: String,
    val milestone: Long?
)
