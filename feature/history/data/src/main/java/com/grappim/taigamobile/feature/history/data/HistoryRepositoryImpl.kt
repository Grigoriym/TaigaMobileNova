package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.mapper.CommentsMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyApi: HistoryApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val commentsMapper: CommentsMapper
) : HistoryRepository {

    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType): ImmutableList<Comment> =
        historyApi.getCommonTaskComments(
            taskPath = WorkItemPathSingular(commonTaskType = type),
            id = commonTaskId
        ).sortedBy { it.postDateTime }
            .filter { it.deleteDate == null }
            .map { dto ->
                commentsMapper.toDomain(dto = dto, currentUserId = taigaSessionStorage.requireUserId())
            }
            .toImmutableList()

    override suspend fun deleteComment(commonTaskId: Long, commonTaskType: CommonTaskType, commentId: String) =
        historyApi.deleteCommonTaskComment(
            taskPath = WorkItemPathSingular(commonTaskType),
            id = commonTaskId,
            commentId = commentId
        )
}
