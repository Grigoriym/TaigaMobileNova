package com.grappim.taigamobile.feature.projects.domain

data class ProjectDetails(
    val id: Long,
    val name: String,
    val description: String,
    val isPrivate: Boolean,
    val isLookingForPeople: Boolean,
    val lookingForPeopleNote: String,
    val isContactActivated: Boolean
)
