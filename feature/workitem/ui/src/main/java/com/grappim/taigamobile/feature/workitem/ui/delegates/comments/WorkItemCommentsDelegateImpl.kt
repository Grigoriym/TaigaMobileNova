package com.grappim.taigamobile.feature.workitem.ui.delegates.comments

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.CreatedCommentData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemCommentsDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val historyRepository: HistoryRepository,
    private val workItemRepository: WorkItemRepository
) : WorkItemCommentsDelegate {

    private val _commentsState = MutableStateFlow(
        WorkItemCommentsState(
            setIsCommentsWidgetExpanded = ::setIsCommentsWidgetExpanded
        )
    )
    override val commentsState: StateFlow<WorkItemCommentsState> = _commentsState.asStateFlow()

    override suspend fun handleCreateComment(
        version: Long,
        id: Long,
        comment: String,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((CreatedCommentData) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _commentsState.update {
            it.copy(areCommentsLoading = true)
        }

        resultOf {
            coroutineScope {
                val payload = persistentMapOf(
                    "comment" to comment
                )

                val patchedData = async {
                    workItemRepository.patchData(
                        version = version,
                        workItemId = id,
                        payload = payload,
                        commonTaskType = commonTaskType
                    )
                }

                val newComments = async {
                    historyRepository.getComments(
                        commonTaskId = id,
                        type = commonTaskType
                    )
                }

                CreatedCommentData(
                    newVersion = patchedData.await().newVersion,
                    comments = newComments.await()
                )
            }
        }.onSuccess { result ->
            doOnSuccess?.invoke(result)

            _commentsState.update {
                it.copy(
                    areCommentsLoading = false,
                    comments = result.comments.toPersistentList()
                )
            }
        }.onFailure { error ->
            _commentsState.update {
                it.copy(areCommentsLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleDeleteComment(
        id: Long,
        commentId: String,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _commentsState.update {
            it.copy(areCommentsLoading = true)
        }

        resultOf {
            historyRepository.deleteComment(
                commonTaskId = id,
                commentId = commentId,
                commonTaskType = commonTaskType
            )
        }.onSuccess {
            doOnSuccess?.invoke()
            _commentsState.update { currentState ->
                currentState.copy(
                    areCommentsLoading = false,
                    comments = currentState.comments.removeAll { it.id == commentId }
                )
            }
        }.onFailure { error ->
            _commentsState.update {
                it.copy(areCommentsLoading = false)
            }
            doOnError(error)
        }
    }

    override fun onCommentError(error: NativeText) {
        _commentsState.update {
            it.copy(areCommentsLoading = false)
        }
    }

    override fun setInitialComments(comments: List<Comment>) {
        _commentsState.update {
            it.copy(comments = comments.toPersistentList())
        }
    }

    private fun setIsCommentsWidgetExpanded(isExpanded: Boolean) {
        _commentsState.update {
            it.copy(isCommentsWidgetExpanded = isExpanded)
        }
    }
}
