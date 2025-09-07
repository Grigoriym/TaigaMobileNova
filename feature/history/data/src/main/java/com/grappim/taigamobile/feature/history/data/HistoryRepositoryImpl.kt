package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommentDTO
import com.grappim.taigamobile.core.domain.CommonTaskPathSingular
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyApi: HistoryApi,
    private val session: Session,
    private val commentsMapper: CommentsMapper
) : HistoryRepository {
    @Deprecated("use getComments")
    override suspend fun getCommentsDTO(
        commonTaskId: Long,
        type: CommonTaskType
    ): List<CommentDTO> = historyApi.getCommonTaskComments(
        taskPath = CommonTaskPathSingular(commonTaskType = type),
        id = commonTaskId
    ).sortedBy { it.postDateTime }
        .filter { it.deleteDate == null }
        .map { it.also { it.canDelete = it.author.actualId == session.userId } }

    override suspend fun getComments(
        commonTaskId: Long,
        type: CommonTaskType
    ): ImmutableList<Comment> = historyApi.getCommonTaskComments(
        taskPath = CommonTaskPathSingular(commonTaskType = type),
        id = commonTaskId
    ).sortedBy { it.postDateTime }
        .filter { it.deleteDate == null }
        .map { dto ->
            commentsMapper.toDomain(dto = dto, currentUserId = session.userId)
        }.toImmutableList()

    override suspend fun deleteComment(
        commonTaskId: Long,
        commonTaskType: CommonTaskType,
        commentId: String
    ) = historyApi.deleteCommonTaskComment(
        taskPath = CommonTaskPathSingular(commonTaskType),
        id = commonTaskId,
        commentId = commentId
    )
}
