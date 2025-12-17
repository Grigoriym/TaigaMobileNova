package com.grappim.taigamobile.feature.projects.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProjectResponseDTO(val id: Long, val name: String, val members: List<ProjectMemberDTO>)
