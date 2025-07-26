package com.grappim.taigamobile.core.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class CommentDTO(
    val id: String,
    @Json(name = "user") val author: UserDTO,
    @Json(name = "comment") val text: String,
    @Json(name = "created_at") val postDateTime: LocalDateTime,
    @Json(name = "delete_comment_date") val deleteDate: LocalDateTime?
) {
    var canDelete: Boolean = false
}
