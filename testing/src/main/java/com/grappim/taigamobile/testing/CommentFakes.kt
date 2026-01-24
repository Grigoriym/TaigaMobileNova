package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import java.time.LocalDateTime

fun getCommentDTO(
    postDateTime: LocalDateTime = LocalDateTime.now(),
    deleteDate: LocalDateTime? = null
): CommentDTO = CommentDTO(
    id = getRandomString(),
    author = getUserDTO(),
    text = getRandomString(),
    postDateTime = postDateTime,
    deleteDate = deleteDate
)
