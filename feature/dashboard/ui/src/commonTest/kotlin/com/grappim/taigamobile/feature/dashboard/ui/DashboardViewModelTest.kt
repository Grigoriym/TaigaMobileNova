@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.dashboard.ui

import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeGetMyWorkItemsUseCase
import com.grappim.taigamobile.testing.usecases.FakeGetRecentActivityUseCase
import com.grappim.taigamobile.testing.usecases.FakeGetRecentlyCompletedItemsUseCase
import com.grappim.taigamobile.testing.usecases.FakeGetWatchingItemsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class DashboardViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    private val sessionStorage = FakeTaigaSessionStorage(currentProjectId = 1L, currentUserId = 42L)
    private val getWatchingItemsUseCase = FakeGetWatchingItemsUseCase()
    private val getMyWorkItemsUseCase = FakeGetMyWorkItemsUseCase()
    private val getRecentActivityUseCase = FakeGetRecentActivityUseCase()
    private val getRecentlyCompletedItemsUseCase = FakeGetRecentlyCompletedItemsUseCase()
    private val projectsRepository = FakeProjectsRepository()

    private lateinit var sut: DashboardViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = DashboardViewModel(
            taigaSessionStorage = sessionStorage,
            getWatchingItemsUseCase = getWatchingItemsUseCase,
            getMyWorkItemsUseCase = getMyWorkItemsUseCase,
            getRecentActivityUseCase = getRecentActivityUseCase,
            getRecentlyCompletedItemsUseCase = getRecentlyCompletedItemsUseCase,
            projectsRepository = projectsRepository
        )
    }

    @Test
    fun `on init - success - all sections load items and set currentProjectId`() = runTest {
        val watchingItems = listOf(getWorkItem(), getWorkItem())
        val myWorkItems = listOf(getWorkItem())
        val recentActivityItems = listOf(getWorkItem(), getWorkItem(), getWorkItem())
        val recentlyCompletedItems = listOf(getWorkItem())

        getWatchingItemsUseCase.result = Result.success(watchingItems)
        getMyWorkItemsUseCase.result = Result.success(myWorkItems)
        getRecentActivityUseCase.result = Result.success(recentActivityItems)
        getRecentlyCompletedItemsUseCase.result = Result.success(recentlyCompletedItems)

        createViewModel()

        assertEquals(1L, sut.state.value.currentProjectId)

        with(sut.state.value.watchingSection) {
            assertEquals(watchingItems, items.toList())
            assertFalse(isLoading)
            assertTrue(error.isEmpty())
        }
        with(sut.state.value.myWorkSection) {
            assertEquals(myWorkItems, items.toList())
            assertFalse(isLoading)
            assertTrue(error.isEmpty())
        }
        with(sut.state.value.recentActivitySection) {
            assertEquals(recentActivityItems, items.toList())
            assertFalse(isLoading)
            assertTrue(error.isEmpty())
        }
        with(sut.state.value.recentlyCompletedSection) {
            assertEquals(recentlyCompletedItems, items.toList())
            assertFalse(isLoading)
            assertTrue(error.isEmpty())
        }
    }

    @Test
    fun `on init - watching fails - watching section shows error`() = runTest {
        val error = RuntimeException("watching failed")
        getWatchingItemsUseCase.result = Result.failure(error)

        createViewModel()

        with(sut.state.value.watchingSection) {
            assertTrue(items.isEmpty())
            assertFalse(isLoading)
            assertTrue(this.error.isNotEmpty())
        }
        // Other sections unaffected
        assertTrue(sut.state.value.myWorkSection.error.isEmpty())
    }

    @Test
    fun `on init - all sections fail - all show error`() = runTest {
        val error = RuntimeException("network error")
        getWatchingItemsUseCase.result = Result.failure(error)
        getMyWorkItemsUseCase.result = Result.failure(error)
        getRecentActivityUseCase.result = Result.failure(error)
        getRecentlyCompletedItemsUseCase.result = Result.failure(error)

        createViewModel()

        assertTrue(sut.state.value.watchingSection.error.isNotEmpty())
        assertTrue(sut.state.value.myWorkSection.error.isNotEmpty())
        assertTrue(sut.state.value.recentActivitySection.error.isNotEmpty())
        assertTrue(sut.state.value.recentlyCompletedSection.error.isNotEmpty())
    }

    @Test
    fun `onToggleWatching - toggles isExpanded`() = runTest {
        createViewModel()

        assertFalse(sut.state.value.watchingSection.isExpanded)

        sut.state.value.onToggleWatching()
        assertTrue(sut.state.value.watchingSection.isExpanded)

        sut.state.value.onToggleWatching()
        assertFalse(sut.state.value.watchingSection.isExpanded)
    }

    @Test
    fun `onToggleMyWork - toggles isExpanded`() = runTest {
        createViewModel()

        assertFalse(sut.state.value.myWorkSection.isExpanded)

        sut.state.value.onToggleMyWork()
        assertTrue(sut.state.value.myWorkSection.isExpanded)

        sut.state.value.onToggleMyWork()
        assertFalse(sut.state.value.myWorkSection.isExpanded)
    }

    @Test
    fun `onToggleRecentActivity - toggles isExpanded`() = runTest {
        createViewModel()

        assertFalse(sut.state.value.recentActivitySection.isExpanded)

        sut.state.value.onToggleRecentActivity()
        assertTrue(sut.state.value.recentActivitySection.isExpanded)

        sut.state.value.onToggleRecentActivity()
        assertFalse(sut.state.value.recentActivitySection.isExpanded)
    }

    @Test
    fun `onToggleRecentlyCompleted - toggles isExpanded`() = runTest {
        createViewModel()

        assertFalse(sut.state.value.recentlyCompletedSection.isExpanded)

        sut.state.value.onToggleRecentlyCompleted()
        assertTrue(sut.state.value.recentlyCompletedSection.isExpanded)

        sut.state.value.onToggleRecentlyCompleted()
        assertFalse(sut.state.value.recentlyCompletedSection.isExpanded)
    }

    @Test
    fun `onRetryWatching - after error - reloads and clears error`() = runTest {
        getWatchingItemsUseCase.result = Result.failure(RuntimeException("fail"))
        createViewModel()
        assertTrue(sut.state.value.watchingSection.error.isNotEmpty())

        val items = listOf(getWorkItem())
        getWatchingItemsUseCase.result = Result.success(items)
        sut.state.value.onRetryWatching()

        with(sut.state.value.watchingSection) {
            assertEquals(items, this.items.toList())
            assertTrue(error.isEmpty())
            assertFalse(isLoading)
            assertFalse(isRetrying)
        }
    }

    @Test
    fun `onRetryMyWork - after error - reloads and clears error`() = runTest {
        getMyWorkItemsUseCase.result = Result.failure(RuntimeException("fail"))
        createViewModel()
        assertTrue(sut.state.value.myWorkSection.error.isNotEmpty())

        val items = listOf(getWorkItem())
        getMyWorkItemsUseCase.result = Result.success(items)
        sut.state.value.onRetryMyWork()

        with(sut.state.value.myWorkSection) {
            assertEquals(items, this.items.toList())
            assertTrue(error.isEmpty())
            assertFalse(isLoading)
            assertFalse(isRetrying)
        }
    }

    @Test
    fun `onRetryRecentActivity - after error - reloads and clears error`() = runTest {
        getRecentActivityUseCase.result = Result.failure(RuntimeException("fail"))
        createViewModel()
        assertTrue(sut.state.value.recentActivitySection.error.isNotEmpty())

        val items = listOf(getWorkItem())
        getRecentActivityUseCase.result = Result.success(items)
        sut.state.value.onRetryRecentActivity()

        with(sut.state.value.recentActivitySection) {
            assertEquals(items, this.items.toList())
            assertTrue(error.isEmpty())
            assertFalse(isLoading)
            assertFalse(isRetrying)
        }
    }

    @Test
    fun `onRetryRecentlyCompleted - after error - reloads and clears error`() = runTest {
        getRecentlyCompletedItemsUseCase.result = Result.failure(RuntimeException("fail"))
        createViewModel()
        assertTrue(sut.state.value.recentlyCompletedSection.error.isNotEmpty())

        val items = listOf(getWorkItem())
        getRecentlyCompletedItemsUseCase.result = Result.success(items)
        sut.state.value.onRetryRecentlyCompleted()

        with(sut.state.value.recentlyCompletedSection) {
            assertEquals(items, this.items.toList())
            assertTrue(error.isEmpty())
            assertFalse(isLoading)
            assertFalse(isRetrying)
        }
    }

    @Test
    fun `updateInternalProjectData - success - calls repository`() = runTest {
        createViewModel()

        sut.updateInternalProjectData()

        assertTrue(projectsRepository.fetchAndSaveProjectInfoCalled)
    }

    @Test
    fun `updateInternalProjectData - failure - does not crash`() = runTest {
        projectsRepository.fetchAndSaveProjectInfoThrows = RuntimeException("network error")
        createViewModel()

        sut.updateInternalProjectData()

        // No exception propagated — ViewModel swallows it and logs
        assertTrue(projectsRepository.fetchAndSaveProjectInfoCalled)
    }
}
