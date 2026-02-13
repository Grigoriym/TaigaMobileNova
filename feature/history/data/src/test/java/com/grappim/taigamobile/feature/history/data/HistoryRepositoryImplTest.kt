package com.grappim.taigamobile.feature.history.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.mapper.CommentsMapper
import com.grappim.taigamobile.testing.getCommentDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryRepositoryImplTest {

    private val historyApi: HistoryApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val commentsMapper: CommentsMapper = mockk()

    private lateinit var sut: HistoryRepository

    private val currentUserId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.requireUserId() } returns currentUserId

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
        val taskPath = WorkItemPathSingular(taskType)

        val olderComment = getCommentDTO(postDateTime = LocalDateTime.of(2024, 1, 1, 10, 0))
        val newerComment = getCommentDTO(postDateTime = LocalDateTime.of(2024, 1, 2, 10, 0))
        val apiResponse = listOf(newerComment, olderComment)

        val mappedOlderComment = mockk<Comment>()
        val mappedNewerComment = mockk<Comment>()

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns apiResponse
        coEvery { commentsMapper.toDomain(olderComment, currentUserId) } returns mappedOlderComment
        coEvery { commentsMapper.toDomain(newerComment, currentUserId) } returns mappedNewerComment

        val result = sut.getComments(taskId, taskType)

        assertEquals(2, result.size)
        assertEquals(mappedOlderComment, result[0])
        assertEquals(mappedNewerComment, result[1])
    }

    @Test
    fun `getComments should filter out deleted comments`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathSingular(taskType)

        val activeComment = getCommentDTO(deleteDate = null)
        val deletedComment = getCommentDTO(deleteDate = LocalDateTime.now())
        val apiResponse = listOf(activeComment, deletedComment)

        val mappedComment = mockk<Comment>()

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns apiResponse
        coEvery { commentsMapper.toDomain(activeComment, currentUserId) } returns mappedComment

        val result = sut.getComments(taskId, taskType)

        assertEquals(1, result.size)
        coVerify(exactly = 1) { commentsMapper.toDomain(any(), any()) }
    }

    @Test
    fun `getComments should return empty list when no comments`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Issue
        val taskPath = WorkItemPathSingular(taskType)

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns emptyList()

        val result = sut.getComments(taskId, taskType)

        assertEquals(0, result.size)
    }

    @Test
    fun `getComments should return empty list when all comments are deleted`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Epic
        val taskPath = WorkItemPathSingular(taskType)

        val deletedComment1 = getCommentDTO(deleteDate = LocalDateTime.now())
        val deletedComment2 = getCommentDTO(deleteDate = LocalDateTime.now())
        val apiResponse = listOf(deletedComment1, deletedComment2)

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns apiResponse

        val result = sut.getComments(taskId, taskType)

        assertEquals(0, result.size)
        coVerify(exactly = 0) { commentsMapper.toDomain(any(), any()) }
    }

    @Test
    fun `getComments should work with UserStory task type`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathSingular(taskType)

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns emptyList()

        sut.getComments(taskId, taskType)

        coVerify { historyApi.getCommonTaskComments(taskPath, taskId) }
    }

    @Test
    fun `getComments should work with Task task type`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathSingular(taskType)

        coEvery { historyApi.getCommonTaskComments(taskPath, taskId) } returns emptyList()

        sut.getComments(taskId, taskType)

        coVerify { historyApi.getCommonTaskComments(taskPath, taskId) }
    }

    @Test
    fun `deleteComment should call api with correct parameters`() = runTest {
        val taskId = getRandomLong()
        val taskType = CommonTaskType.UserStory
        val commentId = getRandomString()
        val taskPath = WorkItemPathSingular(taskType)

        coJustRun { historyApi.deleteCommonTaskComment(taskPath, taskId, commentId) }

        sut.deleteComment(taskId, taskType, commentId)

        coVerify { historyApi.deleteCommonTaskComment(taskPath, taskId, commentId) }
    }

    @Test
    fun `deleteComment should work with different task types`() = runTest {
        val taskId = getRandomLong()
        val commentId = getRandomString()

        CommonTaskType.entries.forEach { taskType ->
            val taskPath = WorkItemPathSingular(taskType)
            coJustRun { historyApi.deleteCommonTaskComment(taskPath, taskId, commentId) }

            sut.deleteComment(taskId, taskType, commentId)

            coVerify { historyApi.deleteCommonTaskComment(taskPath, taskId, commentId) }
        }
    }
}
