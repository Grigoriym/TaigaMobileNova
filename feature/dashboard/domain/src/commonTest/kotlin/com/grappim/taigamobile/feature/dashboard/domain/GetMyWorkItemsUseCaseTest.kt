package com.grappim.taigamobile.feature.dashboard.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetMyWorkItemsUseCaseTest {

    private val fakeRepo = FakeWorkItemRepository()
    private val useCase = GetMyWorkItemsUseCaseImpl(fakeRepo)

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
    fun `getData passes userId as assignedId for epics stories tasks`() = runTest {
        val userId = 42L

        useCase.getData(userId = userId, projectId = 1L)

        val epicCall = fakeRepo.calls.first { it.commonTaskType == CommonTaskType.Epic }
        val storyCall = fakeRepo.calls.first { it.commonTaskType == CommonTaskType.UserStory }
        val taskCall = fakeRepo.calls.first { it.commonTaskType == CommonTaskType.Task }
        assertEquals(userId, epicCall.assignedId)
        assertEquals(userId, storyCall.assignedId)
        assertEquals(userId, taskCall.assignedId)
    }
}
