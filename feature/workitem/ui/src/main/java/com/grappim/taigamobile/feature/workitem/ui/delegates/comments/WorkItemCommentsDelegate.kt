package com.grappim.taigamobile.feature.workitem.ui.delegates.comments

import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.CreatedCommentData
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemCommentsDelegate {
    val commentsState: StateFlow<WorkItemCommentsState>

    suspend fun handleCreateComment(
        version: Long,
        id: Long,
        comment: String,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((CreatedCommentData) -> Unit)?,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleDeleteComment(
        id: Long,
        commentId: String,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun onCommentError(error: NativeText)
    fun setInitialComments(comments: List<Comment>)
}

data class WorkItemCommentsState(
    val comments: PersistentList<Comment> = persistentListOf(),
    val areCommentsLoading: Boolean = false,
    val isCommentsWidgetExpanded: Boolean = false,
    val setIsCommentsWidgetExpanded: (Boolean) -> Unit = {}
)
