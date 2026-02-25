package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.getSingularPath
import com.grappim.taigamobile.feature.workitem.mapper.CommentsMapper
import com.grappim.taigamobile.testing.api.FakeHistoryApi
import com.grappim.taigamobile.testing.models.getCommentDTO
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDateTime
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HistoryRepositoryImplTest {

    private val historyApi = FakeHistoryApi()
    private val taigaSessionStorage = FakeTaigaSessionStorage(currentUserId = 1L)
    private val commentsMapper = CommentsMapper(
        userMapper = UserMapper(),
        projectsRepository = FakeProjectsRepository()
    )

    private lateinit var sut: HistoryRepositoryImpl

    @BeforeTest
    fun setup() {
        historyApi.commentsResponse = emptyList()
        sut = HistoryRepositoryImpl(
            historyApi = historyApi,
            taigaSessionStorage = taigaSessionStorage,
            commentsMapper = commentsMapper
        )
    }

    @Test
    fun `getComments should return mapped comments sorted by date`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val olderDate = LocalDateTime(2024, 1, 1, 10, 0, 0)
        val newerDate = LocalDateTime(2024, 1, 2, 10, 0, 0)
        val olderComment = getCommentDTO(postDateTime = olderDate)
        val newerComment = getCommentDTO(postDateTime = newerDate)
        historyApi.commentsResponse = listOf(newerComment, olderComment)

        val result = sut.getComments(taskId, taskType)

        assertEquals(2, result.size)
        assertEquals(olderDate, result[0].postDateTime)
        assertEquals(newerDate, result[1].postDateTime)
    }

    @Test
    fun `getComments should filter out deleted comments`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Task
        val activeComment = getCommentDTO(deleteDate = null)
        val deletedComment = getCommentDTO(deleteDate = nowLocalDateTime)
        historyApi.commentsResponse = listOf(activeComment, deletedComment)

        val result = sut.getComments(taskId, taskType)

        assertEquals(1, result.size)
    }

    @Test
    fun `getComments should return empty list when no comments`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Issue

        val result = sut.getComments(taskId, taskType)

        assertEquals(0, result.size)
    }

    @Test
    fun `getComments should return empty list when all comments are deleted`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Epic
        historyApi.commentsResponse = listOf(
            getCommentDTO(deleteDate = nowLocalDateTime),
            getCommentDTO(deleteDate = nowLocalDateTime)
        )

        val result = sut.getComments(taskId, taskType)

        assertEquals(0, result.size)
    }

    @Test
    fun `getComments should pass correct path for UserStory task type`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.UserStory

        sut.getComments(taskId, taskType)

        assertEquals(taskType.getSingularPath(), historyApi.lastGetPath)
        assertEquals(taskId, historyApi.lastGetId)
    }

    @Test
    fun `getComments should pass correct path for Task task type`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Task

        sut.getComments(taskId, taskType)

        assertEquals(taskType.getSingularPath(), historyApi.lastGetPath)
        assertEquals(taskId, historyApi.lastGetId)
    }

    @Test
    fun `deleteComment should call api with correct parameters`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val commentId = getRandomString()

        sut.deleteComment(taskId, taskType, commentId)

        assertEquals(taskType.getSingularPath(), historyApi.lastDeletePath)
        assertEquals(taskId, historyApi.lastDeleteId)
        assertEquals(commentId, historyApi.lastDeleteCommentId)
    }

    @Test
    fun `deleteComment should work with different task types`() = runTest {
        val taskId = getRandomLong()
        val commentId = getRandomString()

        CommonTaskType.entries.forEach { taskType ->
            sut.deleteComment(taskId, taskType, commentId)

            assertEquals(taskType.getSingularPath(), historyApi.lastDeletePath)
            assertEquals(taskId, historyApi.lastDeleteId)
            assertEquals(commentId, historyApi.lastDeleteCommentId)
        }
    }
}
