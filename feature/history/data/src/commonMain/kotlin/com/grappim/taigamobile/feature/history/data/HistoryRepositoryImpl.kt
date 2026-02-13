package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.getSingularPath
import com.grappim.taigamobile.feature.workitem.mapper.CommentsMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.core.annotation.Single

@Single(binds = [HistoryRepository::class])
class HistoryRepositoryImpl(
    private val historyApi: HistoryApi,
    private val taigaSessionStorage: KmpTaigaSessionStorage,
    private val commentsMapper: CommentsMapper
) : HistoryRepository {

    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType): ImmutableList<Comment> =
        historyApi.getCommonTaskComments(
            singularTaskPath = type.getSingularPath(),
            id = commonTaskId
        ).sortedBy { it.postDateTime }
            .filter { it.deleteDate == null }
            .map { dto ->
                commentsMapper.toDomain(dto = dto, currentUserId = taigaSessionStorage.requireUserId())
            }
            .toImmutableList()

    override suspend fun deleteComment(commonTaskId: Long, commonTaskType: CommonTaskType, commentId: String) =
        historyApi.deleteCommonTaskComment(
            singularTaskPath = commonTaskType.getSingularPath(),
            id = commonTaskId,
            commentId = commentId
        )
}
