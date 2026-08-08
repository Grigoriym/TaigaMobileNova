package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeHistoryRepository : HistoryRepository {
    var getCommentsResult: ImmutableList<Comment> = persistentListOf()
    var getCommentsThrows: Throwable? = null
    var deleteCommentThrows: Throwable? = null

    override suspend fun getComments(commonTaskId: Long, type: CommonTaskType): ImmutableList<Comment> {
        getCommentsThrows?.let { throw it }
        return getCommentsResult
    }

    override suspend fun deleteComment(commonTaskId: Long, commonTaskType: CommonTaskType, commentId: String) {
        deleteCommentThrows?.let { throw it }
    }
}
