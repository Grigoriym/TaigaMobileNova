package com.grappim.taigamobile.feature.workitem.dto

import com.grappim.taigamobile.core.serialization.LocalDateTimeSerializer
import com.grappim.taigamobile.feature.users.dto.UserDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class CommentDTO(
    val id: String,
    @SerialName(value = "user")
    val author: UserDTO,
    @SerialName(value = "comment")
    val text: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName(value = "created_at")
    val postDateTime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName(value = "delete_comment_date")
    val deleteDate: LocalDateTime?
) {
    var canDelete: Boolean = false
}
