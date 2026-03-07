package com.grappim.taigamobile.testing.models

import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
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
