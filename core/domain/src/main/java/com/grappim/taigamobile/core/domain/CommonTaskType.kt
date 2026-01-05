package com.grappim.taigamobile.core.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CommonTaskType {
    UserStory,
    Task,
    Epic,
    Issue
}
