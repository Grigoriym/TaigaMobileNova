package com.grappim.taigamobile.feature.projects.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProjectResponseDTO(val id: Long, val name: String, val members: List<ProjectMemberDTO>)
