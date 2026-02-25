package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import kotlinx.collections.immutable.persistentListOf

class FakeHistoryRepository : HistoryRepository {
    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType) =
        persistentListOf<Comment>()

    override suspend fun deleteComment(commonTaskId: Long, commonTaskType: CommonTaskType, commentId: String): Unit =
        error("not used in this test")
}
