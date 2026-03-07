package com.grappim.taigamobile.feature.workitem.ui.delegates.comments

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkItemCommentsDelegateImplTest {

    private lateinit var sut: WorkItemCommentsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val historyRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()

    @BeforeTest
    fun setup() {
        sut = WorkItemCommentsDelegateImpl(
            commonTaskType = commonTaskType,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    private fun createComment(id: String = getRandomString(), canDelete: Boolean = false): Comment = Comment(
        id = id,
        author = getUser(),
        text = getRandomString(),
        postDateTime = nowLocalDateTime,
        deleteDate = null,
        canDelete = canDelete
    )

    @Test
    fun `initial state should have default values`() {
        val state = sut.commentsState.value

        assertEquals(persistentListOf(), state.comments)
        assertFalse(state.areCommentsLoading)
        assertFalse(state.isCommentsWidgetExpanded)
    }

    @Test
    fun `on setIsCommentsWidgetExpanded should update field`() {
        assertFalse(sut.commentsState.value.isCommentsWidgetExpanded)

        sut.commentsState.value.setIsCommentsWidgetExpanded(true)

        assertTrue(sut.commentsState.value.isCommentsWidgetExpanded)
    }

    @Test
    fun `setInitialComments should update comments in state`() {
        val comments = listOf(createComment(), createComment())

        sut.setInitialComments(comments)

        assertEquals(comments, sut.commentsState.value.comments.toList())
    }

    @Test
    fun `onCommentError should set areCommentsLoading to false`() {
        sut.onCommentError(NativeText.Simple("error"))

        assertFalse(sut.commentsState.value.areCommentsLoading)
    }

    // region handleCreateComment

    @Test
    fun `handleCreateComment should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false
        val comment = getRandomString()

        sut.handleCreateComment(
            version = 1L,
            id = 123L,
            comment = comment,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleCreateComment should update comments on success`() = runTest {
        val comment = getRandomString()
        val returnedComments = persistentListOf(createComment(), createComment())
        workItemRepository.patchDataResult = PatchedData(newVersion = 1L, dueDateStatus = null)
        historyRepository.getCommentsResult = returnedComments

        sut.handleCreateComment(
            version = 1L,
            id = 123L,
            comment = comment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.commentsState.value
        assertEquals(returnedComments, state.comments.toList())
        assertFalse(state.areCommentsLoading)
    }

    @Test
    fun `handleCreateComment should call doOnSuccess with CreatedCommentData`() = runTest {
        val comment = getRandomString()
        val newVersion = getRandomLong()
        var receivedVersion: Long? = null
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleCreateComment(
            version = 1L,
            id = 123L,
            comment = comment,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it.newVersion },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleCreateComment should clear loading on error`() = runTest {
        val comment = getRandomString()

        sut.handleCreateComment(
            version = 1L,
            id = 123L,
            comment = comment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.commentsState.value.areCommentsLoading)
    }

    @Test
    fun `handleCreateComment should call repositories with correct parameters`() = runTest {
        val comment = getRandomString()
        val version = getRandomLong()
        val id = getRandomLong()

        sut.handleCreateComment(
            version = version,
            id = id,
            comment = comment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )
    }

    // endregion

    // region handleDeleteComment

    @Test
    fun `handleDeleteComment should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false

        sut.handleDeleteComment(
            id = 123L,
            commentId = getRandomString(),
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleDeleteComment should remove comment from list on success`() = runTest {
        val commentToDelete = createComment()
        val commentToKeep = createComment()
        sut.setInitialComments(listOf(commentToDelete, commentToKeep))

        sut.handleDeleteComment(
            id = 123L,
            commentId = commentToDelete.id,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.commentsState.value
        assertEquals(1, state.comments.size)
        assertEquals(commentToKeep.id, state.comments.first().id)
        assertFalse(state.areCommentsLoading)
    }

    @Test
    fun `handleDeleteComment should call doOnSuccess`() = runTest {
        var successCalled = false

        sut.handleDeleteComment(
            id = 123L,
            commentId = getRandomString(),
            doOnPreExecute = null,
            doOnSuccess = { successCalled = true },
            doOnError = {}
        )

        assertTrue(successCalled)
    }

    @Test
    fun `handleDeleteComment should clear loading on error`() = runTest {
        historyRepository.deleteCommentThrows = testException

        sut.handleDeleteComment(
            id = 123L,
            commentId = getRandomString(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.commentsState.value.areCommentsLoading)
    }

    @Test
    fun `handleDeleteComment should call doOnError on failure`() = runTest {
        var receivedError: Throwable? = null
        historyRepository.deleteCommentThrows = testException

        sut.handleDeleteComment(
            id = 123L,
            commentId = getRandomString(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleDeleteComment should call repository with correct parameters`() = runTest {
        val id = getRandomLong()
        val commentId = getRandomString()

        sut.handleDeleteComment(
            id = id,
            commentId = commentId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )
    }

    @Test
    fun `handleDeleteComment should not remove other comments on error`() = runTest {
        val comment1 = createComment()
        val comment2 = createComment()
        sut.setInitialComments(listOf(comment1, comment2))
        historyRepository.deleteCommentThrows = testException

        sut.handleDeleteComment(
            id = 123L,
            commentId = comment1.id,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(2, sut.commentsState.value.comments.size)
    }
}
