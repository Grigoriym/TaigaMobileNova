package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.feature.users.domain.User
import java.time.LocalDateTime

data class Comment(
    val id: String,
    val author: User,
    val text: String,
    val postDateTime: LocalDateTime,
    val deleteDate: LocalDateTime?,
    val canDelete: Boolean
)
