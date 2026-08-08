@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.kanban.ui

import com.grappim.taigamobile.feature.filters.domain.model.EpicsFilters
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.RoleFilters
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.filters.domain.model.TagFilters
import com.grappim.taigamobile.feature.filters.domain.model.UsersFilters
import com.grappim.taigamobile.feature.kanban.domain.KanbanData
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSwimlane
import com.grappim.taigamobile.testing.models.getTeamMember
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeGetKanbanDataUseCase
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
            storiesByStatus = storiesByStatus,
            canModifyUserStory = false
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
        filtersRepository.filtersDataThrows = testException

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
                storiesByStatus = persistentMapOf(),
                canModifyUserStory = false
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
                storiesByStatus = persistentMapOf(),
                canModifyUserStory = false
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
                storiesByStatus = persistentMapOf(),
                canModifyUserStory = false
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
                storiesByStatus = storiesByStatus,
                canModifyUserStory = false
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
                storiesByStatus = initialStoriesByStatus,
                canModifyUserStory = false
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
                storiesByStatus = initialStoriesByStatus,
                canModifyUserStory = false
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

    // region helpers for the board-shaped tests below
    //
    // `computeSwimlaneFilters` and `filterStories` are private and read almost every field of
    // `UserStory`, so the tests drive them through the state callbacks with a fully specified
    // board. `getUserStory()` randomises the fields these functions branch on, hence the copy.

    private val statusNew = Status(color = "#fff", id = 1L, name = "New")
    private val statusInProgress = Status(color = "#000", id = 2L, name = "In Progress")

    @Suppress("LongParameterList")
    private fun story(
        id: Long,
        assignedUserIds: List<Long> = emptyList(),
        creatorId: Long = 0L,
        status: Statuses? = null,
        tags: List<Tag> = emptyList(),
        epics: List<UserStoryEpic> = emptyList(),
        swimlane: Long? = null
    ): UserStory = getUserStory(id = id).copy(
        assignedUserIds = assignedUserIds,
        creatorId = creatorId,
        status = status,
        tags = tags.toImmutableList(),
        userStoryEpics = epics.toImmutableList(),
        swimlane = swimlane
    )

    private fun kanbanStory(userStory: UserStory, assignees: List<TeamMember> = emptyList()) =
        KanbanUserStory(userStory = userStory, assignees = assignees.toImmutableList())

    private fun epic(id: Long) = UserStoryEpic(id = id, title = "epic $id", ref = id, color = "#fff")

    /**
     * Sets up a successful board load and creates the ViewModel. [filters] is what
     * `FiltersRepository` returns — i.e. the `allFilters` the ViewModel scopes to the board.
     */
    private fun givenBoard(
        storiesByStatus: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>>,
        filters: FiltersData = FiltersData(),
        teamMembers: ImmutableList<TeamMember> = persistentListOf(),
        statuses: ImmutableList<Statuses> = storiesByStatus.keys.toImmutableList(),
        swimlanes: ImmutableList<Swimlane> = persistentListOf(),
        defaultSwimlane: Swimlane? = null
    ) {
        getKanbanDataUseCase.getDataResult = Result.success(
            KanbanData(
                statuses = statuses,
                stories = storiesByStatus.values.flatten().map { it.userStory }.toImmutableList(),
                swimlanes = swimlanes,
                teamMembers = teamMembers,
                canAddUserStory = false,
                defaultSwimlane = defaultSwimlane,
                storiesByStatus = storiesByStatus,
                canModifyUserStory = false
            )
        )
        getKanbanDataUseCase.computeStoriesByStatusResult = storiesByStatus
        filtersRepository.filtersDataResult = filters
        createViewModel()
    }

    private fun storiesOf(vararg stories: KanbanUserStory) = storiesByStatusOf(statusNew to persistentListOf(*stories))

    private fun idsIn(status: Statuses) = sut.state.value.storiesByStatus[status].orEmpty().map { it.userStory.id }
    // endregion

    // region computeSwimlaneFilters — scoping the project-wide filters to the visible board

    @Test
    fun `computeSwimlaneFilters - repository returns no filters - board filters stay empty`() = runTest {
        givenBoard(storiesOf(kanbanStory(story(id = 10L))))

        assertEquals(0, sut.state.value.filters.filtersNumber)
    }

    @Test
    fun `computeSwimlaneFilters - assignees are counted and unused ones dropped`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(100L, 200L))),
                kanbanStory(story(id = 12L, assignedUserIds = emptyList()))
            ),
            filters = FiltersData(
                assignees = persistentListOf(
                    UsersFilters(id = 100L, name = "A", count = 0L),
                    UsersFilters(id = 200L, name = "B", count = 0L),
                    UsersFilters(id = 300L, name = "Not on this board", count = 0L),
                    UsersFilters(id = null, name = "Unassigned", count = 0L)
                )
            )
        )

        assertEquals(
            listOf(100L to 2L, 200L to 1L, null to 1L),
            sut.state.value.filters.assignees.map { it.id to it.count }
        )
    }

    @Test
    fun `computeSwimlaneFilters - creators are counted and the null-id filter is dropped`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, creatorId = 7L)),
                kanbanStory(story(id = 11L, creatorId = 7L)),
                kanbanStory(story(id = 12L, creatorId = 8L))
            ),
            filters = FiltersData(
                createdBy = persistentListOf(
                    UsersFilters(id = 7L, name = "Seven", count = 0L),
                    UsersFilters(id = 9L, name = "Not on this board", count = 0L),
                    UsersFilters(id = null, name = "No id", count = 0L)
                )
            )
        )

        assertEquals(
            listOf(7L to 2L),
            sut.state.value.filters.createdBy.map { it.id to it.count }
        )
    }

    @Test
    fun `computeSwimlaneFilters - statuses are counted and a story without a status is ignored`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, status = statusNew)),
                kanbanStory(story(id = 11L, status = statusNew)),
                kanbanStory(story(id = 12L, status = null))
            ),
            filters = FiltersData(
                statuses = persistentListOf(
                    StatusFilters(id = statusNew.id, name = "New", count = 0L, color = "#fff"),
                    StatusFilters(id = 99L, name = "Not on this board", count = 0L, color = "#fff")
                )
            )
        )

        assertEquals(
            listOf(statusNew.id to 2L),
            sut.state.value.filters.statuses.map { it.id to it.count }
        )
    }

    @Test
    fun `computeSwimlaneFilters - tags are counted by name`() = runTest {
        val bug = Tag(color = "#f00", name = "bug")
        val ui = Tag(color = "#0f0", name = "ui")
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, tags = listOf(bug, ui))),
                kanbanStory(story(id = 11L, tags = listOf(bug)))
            ),
            filters = FiltersData(
                tags = persistentListOf(
                    TagFilters(name = "bug", count = 0L, color = "#f00"),
                    TagFilters(name = "not-on-this-board", count = 0L, color = "#00f")
                )
            )
        )

        assertEquals(
            listOf("bug" to 2L),
            sut.state.value.filters.tags.map { it.name to it.count }
        )
    }

    @Test
    fun `computeSwimlaneFilters - epics are counted and the null-id filter is dropped`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, epics = listOf(epic(5L)))),
                kanbanStory(story(id = 11L, epics = listOf(epic(5L))))
            ),
            filters = FiltersData(
                epics = persistentListOf(
                    EpicsFilters(id = 5L, name = "Five", count = 0L),
                    EpicsFilters(id = 6L, name = "Not on this board", count = 0L),
                    EpicsFilters(id = null, name = "No id", count = 0L)
                )
            )
        )

        assertEquals(
            listOf(5L to 2L),
            sut.state.value.filters.epics.map { it.id to it.count }
        )
    }

    @Test
    fun `computeSwimlaneFilters - roles are counted and an assignee outside the team is skipped`() = runTest {
        val developer = getTeamMember(id = 100L).copy(role = "Developer")
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(100L))),
                // 999 is not a team member — contributes no role
                kanbanStory(story(id = 12L, assignedUserIds = listOf(999L))),
                // unassigned — counted under the null key, which the role loop skips
                kanbanStory(story(id = 13L, assignedUserIds = emptyList()))
            ),
            teamMembers = persistentListOf(developer),
            filters = FiltersData(
                roles = persistentListOf(
                    RoleFilters(color = "#fff", count = 0L, id = 1L, name = "Developer"),
                    RoleFilters(color = "#fff", count = 0L, id = 2L, name = "Not on this board")
                )
            )
        )

        assertEquals(
            listOf("Developer" to 2L),
            sut.state.value.filters.roles.map { it.name to it.count }
        )
    }
    // endregion

    // region filterStories — applying the selected filters to the board

    @Test
    fun `onSelectFilters - clearing the selection restores every story`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(200L)))
            )
        )
        sut.state.value.onSelectFilters(
            FiltersData(assignees = persistentListOf(UsersFilters(id = 100L, name = "A", count = 1L)))
        )
        assertEquals(listOf(10L), idsIn(statusNew))

        sut.state.value.onSelectFilters(FiltersData())

        assertEquals(listOf(10L, 11L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - assignee filter - only stories assigned to them remain`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L, 200L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(200L))),
                kanbanStory(story(id = 12L, assignedUserIds = emptyList()))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(assignees = persistentListOf(UsersFilters(id = 100L, name = "A", count = 1L)))
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - creator filter - only stories created by them remain`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, creatorId = 7L)),
                kanbanStory(story(id = 11L, creatorId = 8L))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(createdBy = persistentListOf(UsersFilters(id = 7L, name = "Seven", count = 1L)))
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - status filter - a story without a status is excluded`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, status = statusNew)),
                kanbanStory(story(id = 11L, status = statusInProgress)),
                kanbanStory(story(id = 12L, status = null))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(
                statuses = persistentListOf(
                    StatusFilters(id = statusNew.id, name = "New", count = 1L, color = "#fff")
                )
            )
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - tag filter - only stories carrying the tag remain`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, tags = listOf(Tag(color = "#f00", name = "bug")))),
                kanbanStory(story(id = 11L, tags = listOf(Tag(color = "#0f0", name = "ui")))),
                kanbanStory(story(id = 12L, tags = emptyList()))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(tags = persistentListOf(TagFilters(name = "bug", count = 1L, color = "#f00")))
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - epic filter - only stories in the epic remain`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, epics = listOf(epic(5L)))),
                kanbanStory(story(id = 11L, epics = listOf(epic(6L)))),
                kanbanStory(story(id = 12L, epics = emptyList()))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(epics = persistentListOf(EpicsFilters(id = 5L, name = "Five", count = 1L)))
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - role filter - matches on the resolved assignees, not the story`() = runTest {
        val developer = getTeamMember(id = 100L).copy(role = "Developer")
        val tester = getTeamMember(id = 200L).copy(role = "Tester")
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L)), assignees = listOf(developer)),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(200L)), assignees = listOf(tester)),
                kanbanStory(story(id = 12L))
            ),
            teamMembers = persistentListOf(developer, tester)
        )

        sut.state.value.onSelectFilters(
            FiltersData(
                roles = persistentListOf(RoleFilters(color = "#fff", count = 1L, id = 1L, name = "Developer"))
            )
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onSelectFilters - two filters - a story must match both`() = runTest {
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L), creatorId = 7L)),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(100L), creatorId = 8L))
            )
        )

        sut.state.value.onSelectFilters(
            FiltersData(
                assignees = persistentListOf(UsersFilters(id = 100L, name = "A", count = 2L)),
                createdBy = persistentListOf(UsersFilters(id = 7L, name = "Seven", count = 1L))
            )
        )

        assertEquals(listOf(10L), idsIn(statusNew))
    }
    // endregion

    // region moveStory — neighbour validation and the optimistic update

    /** A board with story 42 in [statusNew] and stories 1 and 2 already in [statusInProgress]. */
    private fun givenTwoColumnBoard(movedStorySwimlane: Long? = null, neighbourSwimlane: Long? = null) = givenBoard(
        storiesByStatusOf(
            statusNew to persistentListOf(kanbanStory(story(id = 42L, swimlane = movedStorySwimlane))),
            statusInProgress to persistentListOf(
                kanbanStory(story(id = 1L, swimlane = neighbourSwimlane)),
                kanbanStory(story(id = 2L, swimlane = neighbourSwimlane))
            )
        ),
        statuses = persistentListOf(statusNew, statusInProgress)
    )

    @Test
    fun `onMoveStory - beforeStoryId is honoured - story inserted at that position`() = runTest {
        givenTwoColumnBoard()

        sut.state.value.onMoveStory(42L, statusInProgress.id, null, 2L, null)

        assertEquals(listOf(1L, 42L, 2L), idsIn(statusInProgress))
        assertEquals(2L, userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
    }

    @Test
    fun `onMoveStory - beforeStoryId not in the target column - dropped and story appended`() = runTest {
        givenTwoColumnBoard()

        sut.state.value.onMoveStory(42L, statusInProgress.id, null, 999L, null)

        assertEquals(listOf(1L, 2L, 42L), idsIn(statusInProgress))
        assertNull(userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
    }

    @Test
    fun `onMoveStory - neighbour in another swimlane is not a valid neighbour`() = runTest {
        givenTwoColumnBoard(movedStorySwimlane = 5L, neighbourSwimlane = 6L)

        sut.state.value.onMoveStory(42L, statusInProgress.id, null, 2L, null)

        assertEquals(listOf(1L, 2L, 42L), idsIn(statusInProgress))
        assertNull(userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
    }

    @Test
    fun `onMoveStory - afterStoryId is sent when there is no beforeStoryId`() = runTest {
        givenTwoColumnBoard()

        sut.state.value.onMoveStory(42L, statusInProgress.id, null, null, 1L)

        assertNull(userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
        assertEquals(1L, userStoriesRepository.bulkUpdateKanbanOrderAfterStoryId)
    }

    @Test
    fun `onMoveStory - afterStoryId is dropped when beforeStoryId is valid`() = runTest {
        givenTwoColumnBoard()

        sut.state.value.onMoveStory(42L, statusInProgress.id, null, 2L, 1L)

        assertEquals(2L, userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
        assertNull(userStoriesRepository.bulkUpdateKanbanOrderAfterStoryId)
    }

    @Test
    fun `onMoveStory - the story's own swimlane wins over the swimlaneId argument`() = runTest {
        givenTwoColumnBoard(movedStorySwimlane = 5L, neighbourSwimlane = 5L)

        sut.state.value.onMoveStory(42L, statusInProgress.id, 9L, null, null)

        assertEquals(5L, userStoriesRepository.bulkUpdateKanbanOrderSwimlaneId)
    }

    @Test
    fun `onMoveStory - unknown story id - board unchanged and the swimlaneId argument is used`() = runTest {
        givenTwoColumnBoard()

        sut.state.value.onMoveStory(777L, statusInProgress.id, 9L, null, null)

        assertEquals(listOf(42L), idsIn(statusNew))
        assertEquals(listOf(1L, 2L), idsIn(statusInProgress))
        assertEquals(9L, userStoriesRepository.bulkUpdateKanbanOrderSwimlaneId)
    }

    @Test
    fun `onMoveStory - unknown target status - neighbour ids are dropped`() = runTest {
        givenTwoColumnBoard()

        // No column matches id 777, so there are no valid neighbours to order against. The
        // optimistic update also drops the story from the board, since no column claims it —
        // unreachable from the UI, where the status id always comes from a rendered column.
        sut.state.value.onMoveStory(42L, 777L, null, 2L, 1L)

        assertNull(userStoriesRepository.bulkUpdateKanbanOrderBeforeStoryId)
        assertNull(userStoriesRepository.bulkUpdateKanbanOrderAfterStoryId)
        assertEquals(777L, userStoriesRepository.bulkUpdateKanbanOrderStatusId)
        assertEquals(listOf(42L), userStoriesRepository.bulkUpdateKanbanOrderStoryIds)
    }
    // endregion

    // region per-swimlane filter memory

    @Test
    fun `onSelectSwimlane - returning to a swimlane restores the filters selected for it`() = runTest {
        val first = getSwimlane()
        val second = getSwimlane()
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(200L)))
            ),
            swimlanes = persistentListOf(first, second)
        )
        val onlyFirstAssignee =
            FiltersData(assignees = persistentListOf(UsersFilters(id = 100L, name = "A", count = 1L)))

        sut.state.value.onSelectSwimlane(first)
        sut.state.value.onSelectFilters(onlyFirstAssignee)
        assertEquals(listOf(10L), idsIn(statusNew))

        sut.state.value.onSelectSwimlane(second)
        assertEquals(FiltersData(), sut.state.value.activeFilters)
        assertEquals(listOf(10L, 11L), idsIn(statusNew))

        sut.state.value.onSelectSwimlane(first)

        assertEquals(onlyFirstAssignee, sut.state.value.activeFilters)
        assertEquals(listOf(10L), idsIn(statusNew))
    }

    @Test
    fun `onRefresh - reapplies the filters stored for the default swimlane`() = runTest {
        val swimlane = getSwimlane()
        givenBoard(
            storiesOf(
                kanbanStory(story(id = 10L, assignedUserIds = listOf(100L))),
                kanbanStory(story(id = 11L, assignedUserIds = listOf(200L)))
            ),
            swimlanes = persistentListOf(swimlane),
            defaultSwimlane = swimlane
        )
        sut.state.value.onSelectFilters(
            FiltersData(assignees = persistentListOf(UsersFilters(id = 100L, name = "A", count = 1L)))
        )
        assertEquals(listOf(10L), idsIn(statusNew))

        sut.state.value.onRefresh()

        assertEquals(listOf(10L), idsIn(statusNew))
    }
    // endregion

    @Test
    fun `onRetryFilters - reloads the filters and clears a previous filtersError`() = runTest {
        getKanbanDataUseCase.getDataResult = Result.failure(RuntimeException("not testing this"))
        filtersRepository.filtersDataThrows = testException
        createViewModel()
        assertTrue(sut.state.value.filtersError.isNotEmpty())
        assertEquals(1, filtersRepository.getFiltersDataCallCount)

        filtersRepository.filtersDataThrows = null
        filtersRepository.filtersDataResult = FiltersData()
        sut.state.value.onRetryFilters()

        assertEquals(2, filtersRepository.getFiltersDataCallCount)
        assertTrue(sut.state.value.filtersError.isEmpty())
    }
}
