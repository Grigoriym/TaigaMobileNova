package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.testing.FakeDateTimeUtils
import com.grappim.taigamobile.testing.FakeWorkItemRepository
import com.grappim.taigamobile.testing.getWorkItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecentlyCompletedItemsUseCaseTest {

    private val fakeRepo = FakeWorkItemRepository()
    private val fakeDateTimeUtils = FakeDateTimeUtils()
    private val useCase = GetRecentlyCompletedItemsUseCase(fakeRepo, fakeDateTimeUtils)

    @Test
    fun `getData returns combined stories tasks and issues`() = runTest {
        val story = getWorkItem(taskType = CommonTaskType.UserStory)
        val task = getWorkItem(taskType = CommonTaskType.Task)
        val issue = getWorkItem(taskType = CommonTaskType.Issue)

        fakeRepo.itemsByType[CommonTaskType.UserStory] = persistentListOf(story)
        fakeRepo.itemsByType[CommonTaskType.Task] = persistentListOf(task)
        fakeRepo.itemsByType[CommonTaskType.Issue] = persistentListOf(issue)

        val result = useCase.getData(projectId = 1L)

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(3, items.size)
        assertTrue(story in items)
        assertTrue(task in items)
        assertTrue(issue in items)
    }

    @Test
    fun `getData returns empty list when repository returns nothing`() = runTest {
        val result = useCase.getData(projectId = 1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `getData passes date 3 days ago as finishDateGte`() = runTest {
        fakeDateTimeUtils.fixedDate = LocalDate(2024, 3, 5)

        useCase.getData(projectId = 1L)

        assertTrue(fakeRepo.calls.all { it.finishDateGte == "2024-03-02" })
    }

    @Test
    fun `getData sorts results by createdDate descending`() = runTest {
        val older = getWorkItem(
            taskType = CommonTaskType.UserStory,
            createdDate = LocalDateTime(2024, 2, 1, 0, 0)
        )
        val newer = getWorkItem(
            taskType = CommonTaskType.Issue,
            createdDate = LocalDateTime(2024, 2, 28, 0, 0)
        )

        fakeRepo.itemsByType[CommonTaskType.UserStory] = persistentListOf(older)
        fakeRepo.itemsByType[CommonTaskType.Task] = persistentListOf()
        fakeRepo.itemsByType[CommonTaskType.Issue] = persistentListOf(newer)

        val items = useCase.getData(projectId = 1L).getOrThrow()

        assertEquals(newer, items[0])
        assertEquals(older, items[1])
    }

    @Test
    fun `getData returns failure when repository throws`() = runTest {
        fakeRepo.error = RuntimeException("network error")

        val result = useCase.getData(projectId = 1L)

        assertTrue(result.isFailure)
    }
}
