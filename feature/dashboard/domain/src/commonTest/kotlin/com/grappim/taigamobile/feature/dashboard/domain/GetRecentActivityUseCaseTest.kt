package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetRecentActivityUseCaseTest {

    private val fakeRepo = FakeWorkItemRepository()
    private val fakeDateTimeUtils = FakeDateTimeUtils()
    private val useCase = GetRecentActivityUseCaseImpl(fakeRepo, fakeDateTimeUtils)

    @Test
    fun `getData returns combined stories and tasks`() = runTest {
        val story = getWorkItem(taskType = CommonTaskType.UserStory)
        val task = getWorkItem(taskType = CommonTaskType.Task)

        fakeRepo.itemsByType[CommonTaskType.UserStory] = persistentListOf(story)
        fakeRepo.itemsByType[CommonTaskType.Task] = persistentListOf(task)

        val result = useCase.getData(projectId = 1L)

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(2, items.size)
        assertTrue(story in items)
        assertTrue(task in items)
    }

    @Test
    fun `getData returns empty list when repository returns nothing`() = runTest {
        val result = useCase.getData(projectId = 1L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `getData passes date 3 days ago as modifiedDateGte`() = runTest {
        fakeDateTimeUtils.fixedDate = LocalDate(2024, 6, 15)

        useCase.getData(projectId = 1L)

        assertTrue(fakeRepo.calls.all { it.params.modifiedDateGte == "2024-06-12" })
    }

    @Test
    fun `getData sorts results by createdDate descending`() = runTest {
        val older = getWorkItem(
            taskType = CommonTaskType.UserStory,
            createdDate = LocalDateTime(2024, 1, 10, 0, 0)
        )
        val newer = getWorkItem(
            taskType = CommonTaskType.Task,
            createdDate = LocalDateTime(2024, 1, 20, 0, 0)
        )

        fakeRepo.itemsByType[CommonTaskType.UserStory] = persistentListOf(older)
        fakeRepo.itemsByType[CommonTaskType.Task] = persistentListOf(newer)

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
