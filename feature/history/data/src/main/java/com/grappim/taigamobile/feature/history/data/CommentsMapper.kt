package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommentDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun toDomain(dto: CommentDTO, currentUserId: Long): Comment =
        withContext(ioDispatcher) {
            Comment(
                id = dto.id,
                author = dto.author,
                text = dto.text,
                postDateTime = dto.postDateTime,
                deleteDate = dto.deleteDate,
                canDelete = dto.author.actualId == currentUserId
            )
        }
}
