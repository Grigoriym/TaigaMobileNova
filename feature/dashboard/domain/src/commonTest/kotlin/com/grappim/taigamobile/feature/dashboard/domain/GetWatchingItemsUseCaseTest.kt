package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWatchingItemsUseCaseTest {

    private val fakeRepo = FakeWorkItemRepository()
    private val useCase = GetWatchingItemsUseCase(fakeRepo)

    @Test
    fun `getData returns combined items from all four types`() = runTest {
        val epic = getWorkItem(taskType = CommonTaskType.Epic)
        val story = getWorkItem(taskType = CommonTaskType.UserStory)
        val task = getWorkItem(taskType = CommonTaskType.Task)
        val issue = getWorkItem(taskType = CommonTaskType.Issue)

        fakeRepo.itemsByType[CommonTaskType.Epic] = persistentListOf(epic)
        fakeRepo.itemsByType[CommonTaskType.UserStory] = persistentListOf(story)
        fakeRepo.itemsByType[CommonTaskType.Task] = persistentListOf(task)
        fakeRepo.itemsByType[CommonTaskType.Issue] = persistentListOf(issue)

        val result = useCase.getData(userId = 1L, projectId = 2L)

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(4, items.size)
        assertTrue(epic in items)
        assertTrue(story in items)
        assertTrue(task in items)
        assertTrue(issue in items)
    }

    @Test
    fun `getData returns empty list when repository returns nothing`() = runTest {
        val result = useCase.getData(userId = 1L, projectId = 2L)

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `getData returns failure when repository throws`() = runTest {
        fakeRepo.error = RuntimeException("network error")

        val result = useCase.getData(userId = 1L, projectId = 2L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getData passes userId as watcherId for all types`() = runTest {
        val userId = 99L

        useCase.getData(userId = userId, projectId = 1L)

        assertTrue(fakeRepo.calls.all { it.watcherId == userId })
    }
}
