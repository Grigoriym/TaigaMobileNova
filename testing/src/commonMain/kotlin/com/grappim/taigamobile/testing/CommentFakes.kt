package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import kotlinx.datetime.LocalDateTime

fun getCommentDTO(
    postDateTime: LocalDateTime = nowLocalDateTime,
    deleteDate: LocalDateTime? = null
): CommentDTO = CommentDTO(
    id = getRandomString(),
    author = getUserDTO(),
    text = getRandomString(),
    postDateTime = postDateTime,
    deleteDate = deleteDate
)
