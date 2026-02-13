package com.grappim.taigamobile.feature.issues.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateIssueRequestDTO(val project: Long, val subject: String, val description: String, val milestone: Long?)
