package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.history.data.HistoryApi
import com.grappim.taigamobile.feature.workitem.dto.CommentDTO

class FakeHistoryApi : HistoryApi {
    var commentsResponse: List<CommentDTO> = emptyList()
    var lastGetPath: String? = null
    var lastGetId: Long? = null
    var lastDeletePath: String? = null
    var lastDeleteId: Long? = null
    var lastDeleteCommentId: String? = null

    override suspend fun getCommonTaskComments(singularTaskPath: String, id: Long): List<CommentDTO> {
        lastGetPath = singularTaskPath
        lastGetId = id
        return commentsResponse
    }

    override suspend fun deleteCommonTaskComment(singularTaskPath: String, id: Long, commentId: String) {
        lastDeletePath = singularTaskPath
        lastDeleteId = id
        lastDeleteCommentId = commentId
    }
}
