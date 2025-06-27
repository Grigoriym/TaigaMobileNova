package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskPathSingular
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyApi: HistoryApi,
    private val session: Session
) : HistoryRepository {
    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType): List<Comment> =
        historyApi.getCommonTaskComments(CommonTaskPathSingular(type), commonTaskId)
            .sortedBy { it.postDateTime }
            .filter { it.deleteDate == null }
            .map { it.also { it.canDelete = it.author.actualId == session.userId } }

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
