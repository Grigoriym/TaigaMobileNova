package com.grappim.taigamobile.feature.history.domain

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommentDTO
import com.grappim.taigamobile.core.domain.CommonTaskType

interface HistoryRepository {
    suspend fun getCommentsDTO(commonTaskId: Long, type: CommonTaskType): List<CommentDTO>
    suspend fun getComments(commonTaskId: Long, type: CommonTaskType): List<Comment>

    suspend fun deleteComment(commonTaskId: Long, commonTaskType: CommonTaskType, commentId: String)
}
