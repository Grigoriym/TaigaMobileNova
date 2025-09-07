package com.grappim.taigamobile.core.domain

import java.time.LocalDateTime

data class Comment(
    val id: String,
    val author: UserDTO,
    val text: String,
    val postDateTime: LocalDateTime,
    val deleteDate: LocalDateTime?,
    val canDelete: Boolean
)
