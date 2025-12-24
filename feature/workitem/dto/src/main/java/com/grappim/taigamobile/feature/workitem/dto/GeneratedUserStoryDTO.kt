package com.grappim.taigamobile.feature.workitem.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedUserStoryDTO(val id: Long, val ref: Long?, val subject: String)
