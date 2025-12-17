package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CommentsMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val userMapper: UserMapper
) {
    suspend fun toDomain(dto: CommentDTO, currentUserId: Long): Comment = withContext(ioDispatcher) {
        Comment(
            id = dto.id,
            author = userMapper.toUser(dto.author),
            text = dto.text,
            postDateTime = dto.postDateTime,
            deleteDate = dto.deleteDate,
            canDelete = dto.author.actualId == currentUserId
        )
    }
}
