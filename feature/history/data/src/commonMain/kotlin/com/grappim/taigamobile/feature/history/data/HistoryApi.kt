package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.feature.workitem.dto.CommentDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

@Single
class HistoryApi(private val httpClient: HttpClient) {

    suspend fun getCommonTaskComments(singularTaskPath: String, id: Long): List<CommentDTO> =
        httpClient.get("history/$singularTaskPath/$id") {
            url { parameters.append("type", "comment") }
        }.body()

    suspend fun deleteCommonTaskComment(singularTaskPath: String, id: Long, commentId: String) {
        httpClient.post("history/$singularTaskPath/$id/delete_comment") {
            url { parameters.append("id", commentId) }
        }.discardRemaining()
    }
}
