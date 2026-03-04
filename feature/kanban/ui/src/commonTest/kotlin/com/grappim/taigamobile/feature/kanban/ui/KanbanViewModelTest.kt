@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.UsersFilters
import com.grappim.taigamobile.feature.kanban.domain.KanbanData
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSwimlane
import com.grappim.taigamobile.testing.models.getTeamMember
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeGetKanbanDataUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class KanbanViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    private val getKanbanDataUseCase = FakeGetKanbanDataUseCase()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val filtersRepository = FakeFiltersRepository()

    private lateinit var sut: KanbanViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = KanbanViewModel(
            getKanbanDataUseCase = getKanbanDataUseCase,
            taigaSessionStorage = sessionStorage,
            userStoriesRepository = userStoriesRepository,
            filtersRepository = filtersRepository
        )
    }

    private fun storiesByStatusOf(
        vararg pairs: Pair<Statuses, ImmutableList<KanbanUserStory>>
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> =
        mapOf<Statuses, ImmutableList<KanbanUserStory>>(*pairs).toImmutableMap()

    @Test
    fun `on init - getData success - state populated with kanban data`() = runTest {
        val status = Status(color = "#fff", id = 1L, name = "In Progress")
        val story = getUserStory()
        val swimlane = getSwimlane()
        val teamMember = getTeamMember()
        val kanbanStory = KanbanUserStory(userStory = story, assignees = persistentListOf())
        val storiesByStatus = storiesByStatusOf(status to persistentListOf(kanbanStory))
        val kanbanData = KanbanData(
            statuses = persistentListOf(status),
            stories = persistentListOf(story),
            swimlanes = persistentListOf(swimlane),
            teamMembers = persistentListOf(teamMember),
            canAddUserStory = true,
            defaultSwimlane = swimlane,
            storiesByStatus = storiesByStatus
        )
        getKanbanDataUseCase.getDataResult = Result.success(kanbanData)
        getKanbanDataUseCase.computeStoriesByStatusResult = storiesByStatus
        filtersRepository.filtersDataResult = FiltersData()

        createViewModel()

        with(sut.state.value) {
            assertFalse(isLoading)
            assertTrue(error.isEmpty())
            assertEquals(kanbanData.statuses, statuses)
            assertEquals(kanbanData.swimlanes, swimlanes)
            assertEquals(kanbanData.stories, stories)
            assertEquals(kanbanData.teamMembers, teamMembers)
            assertTrue(canAddUserStory)
            assertEquals(swimlane, selectedSwimlane)
        }
    }

    @Test
    fun `on init - getData failure - error is set and isLoading is false`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.failure(RuntimeException("network error"))
        filtersRepository.filtersDataResult = FiltersData()

        createViewModel()

        with(sut.state.value) {
            assertFalse(isLoading)
            assertTrue(error.isNotEmpty())
        }
    }

    @Test
    fun `on init - loadFiltersData success - filtersLoading false and no filtersError`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.failure(RuntimeException("not testing this"))
        filtersRepository.filtersDataResult = FiltersData()

        createViewModel()

        with(sut.state.value) {
            assertFalse(isFiltersLoading)
            assertTrue(filtersError.isEmpty())
        }
    }

    @Test
    fun `on init - loadFiltersData failure - filtersError is set`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.failure(RuntimeException("not testing this"))
        filtersRepository.filtersDataResult = null

        createViewModel()

        with(sut.state.value) {
            assertFalse(isFiltersLoading)
            assertTrue(filtersError.isNotEmpty())
        }
    }

    @Test
    fun `onRefresh - triggers getKanbanData again`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(),
                stories = persistentListOf(),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = persistentMapOf()
            )
        )
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        assertEquals(1, getKanbanDataUseCase.getDataCallCount)

        sut.state.value.onRefresh()

        assertEquals(2, getKanbanDataUseCase.getDataCallCount)
    }

    @Test
    fun `onSelectSwimlane - non-null swimlane - updates selectedSwimlane and saves to storage`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(),
                stories = persistentListOf(),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = persistentMapOf()
            )
        )
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val newSwimlane = getSwimlane()
        sut.state.value.onSelectSwimlane(newSwimlane)

        assertEquals(newSwimlane, sut.state.value.selectedSwimlane)
        assertTrue(sessionStorage.setKanbanDefaultSwimlineCalls.contains(newSwimlane.id))
    }

    @Test
    fun `onSelectSwimlane - null swimlane - selectedSwimlane is null and storage not updated`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(),
                stories = persistentListOf(),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = persistentMapOf()
            )
        )
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        sut.state.value.onSelectSwimlane(null)

        assertEquals(null, sut.state.value.selectedSwimlane)
        assertTrue(sessionStorage.setKanbanDefaultSwimlineCalls.isEmpty())
    }

    @Test
    fun `onSelectFilters - unassigned filter applied - only unassigned stories remain`() = runTest {
        val status = Status(color = "#fff", id = 1L, name = "In Progress")
        val assignedStory = getUserStory(id = 10L).copy(assignedUserIds = listOf(100L))
        val unassignedStory = getUserStory(id = 20L).copy(assignedUserIds = emptyList())
        val storiesByStatus = storiesByStatusOf(
            status to persistentListOf(
                KanbanUserStory(userStory = assignedStory, assignees = persistentListOf()),
                KanbanUserStory(userStory = unassignedStory, assignees = persistentListOf())
            )
        )
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(status),
                stories = persistentListOf(),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = storiesByStatus
            )
        )
        getKanbanDataUseCase.computeStoriesByStatusResult = storiesByStatus
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        val unassignedFilter = UsersFilters(id = null, name = "Unassigned", count = 1L)
        sut.state.value.onSelectFilters(FiltersData(assignees = persistentListOf(unassignedFilter)))

        val filtered = sut.state.value.storiesByStatus[status].orEmpty()
        assertEquals(1, filtered.size)
        assertEquals(20L, filtered.first().userStory.id)
    }

    @Test
    fun `onMoveStory - success - story moved to new status optimistically`() = runTest {
        val statusA = Status(color = "#fff", id = 1L, name = "New")
        val statusB = Status(color = "#000", id = 2L, name = "In Progress")
        val story = getUserStory(id = 42L)
        val kanbanStory = KanbanUserStory(userStory = story, assignees = persistentListOf())
        val initialStoriesByStatus = storiesByStatusOf(
            statusA to persistentListOf(kanbanStory),
            statusB to persistentListOf()
        )
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(statusA, statusB),
                stories = persistentListOf(story),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = initialStoriesByStatus
            )
        )
        getKanbanDataUseCase.computeStoriesByStatusResult = initialStoriesByStatus
        filtersRepository.filtersDataResult = FiltersData()
        createViewModel()

        sut.state.value.onMoveStory(42L, statusB.id, null, null, null)

        val storiesByStatus = sut.state.value.storiesByStatus
        assertTrue(storiesByStatus[statusA].orEmpty().isEmpty())
        assertEquals(1, storiesByStatus[statusB].orEmpty().size)
        assertEquals(42L, storiesByStatus[statusB]!!.first().userStory.id)
        assertTrue(userStoriesRepository.bulkUpdateKanbanOrderCalled)
    }

    @Test
    fun `onMoveStory - API fails - state reverts to previous and error is shown`() = runTest {
        val statusA = Status(color = "#fff", id = 1L, name = "New")
        val statusB = Status(color = "#000", id = 2L, name = "In Progress")
        val story = getUserStory(id = 42L)
        val kanbanStory = KanbanUserStory(userStory = story, assignees = persistentListOf())
        val initialStoriesByStatus = storiesByStatusOf(
            statusA to persistentListOf(kanbanStory),
            statusB to persistentListOf()
        )
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = persistentListOf(statusA, statusB),
                stories = persistentListOf(story),
                swimlanes = persistentListOf(),
                teamMembers = persistentListOf(),
                canAddUserStory = false,
                defaultSwimlane = null,
                storiesByStatus = initialStoriesByStatus
            )
        )
        getKanbanDataUseCase.computeStoriesByStatusResult = initialStoriesByStatus
        filtersRepository.filtersDataResult = FiltersData()
        userStoriesRepository.bulkUpdateKanbanOrderThrows = RuntimeException("API error")
        createViewModel()

        sut.state.value.onMoveStory(42L, statusB.id, null, null, null)

        val storiesByStatus = sut.state.value.storiesByStatus
        assertEquals(1, storiesByStatus[statusA].orEmpty().size)
        assertEquals(42L, storiesByStatus[statusA]!!.first().userStory.id)
        assertTrue(storiesByStatus[statusB].orEmpty().isEmpty())
        assertTrue(sut.state.value.error.isNotEmpty())
    }
}
