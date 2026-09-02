package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.testing.models.getProjectSimple
import com.grappim.taigamobile.testing.models.getSwimlane
import com.grappim.taigamobile.testing.models.getTeamMember
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSwimlanesRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetKanbanDataUseCaseTest {

    private val usersRepository = FakeUsersRepository()
    private val filtersRepository = FakeFiltersRepository()
    private val swimlanesRepository = FakeSwimlanesRepository()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val projectsRepository = FakeProjectsRepository()

    private lateinit var sut: GetKanbanDataUseCase

    private val todo = Status(color = "#fff", id = 1L, name = "todo")
    private val done = Status(color = "#000", id = 2L, name = "done")

    @BeforeTest
    fun setup() {
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(defaultSwimlane = null)
        filtersRepository.statusesResult = persistentListOf(todo, done)

        sut = GetKanbanDataUseCaseImpl(
            usersRepository = usersRepository,
            filtersRepository = filtersRepository,
            swimlanesRepository = swimlanesRepository,
            userStoriesRepository = userStoriesRepository,
            projectsRepository = projectsRepository
        )
    }

    private fun story(
        id: Long,
        swimlane: Long? = null,
        kanbanOrder: Long = 0L,
        status: Status = todo,
        assignedUserIds: List<Long> = emptyList()
    ): UserStory = getUserStory(id = id).copy(
        swimlane = swimlane,
        kanbanOrder = kanbanOrder,
        status = status,
        assignedUserIds = assignedUserIds
    )

    private fun swimlane(id: Long): Swimlane = getSwimlane().copy(id = id)

    // region buildSwimlanesWithUnclassified

    @Test
    fun `getData does not add the unclassified swimlane when the project has none`() = runTest {
        swimlanesRepository.getSwimlanesResult = persistentListOf()
        userStoriesRepository.getUserStoriesResult = persistentListOf(story(id = 1L, swimlane = null))

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertTrue(data.swimlanes.isEmpty())
    }

    @Test
    fun `getData does not add the unclassified swimlane when every story has one`() = runTest {
        val first = swimlane(id = 10L)
        val second = swimlane(id = 20L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(first, second)
        userStoriesRepository.getUserStoriesResult = persistentListOf(
            story(id = 1L, swimlane = 10L),
            story(id = 2L, swimlane = 20L)
        )

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(listOf(first, second), data.swimlanes)
    }

    @Test
    fun `getData prepends the unclassified swimlane when a story has none`() = runTest {
        val first = swimlane(id = 10L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(first)
        userStoriesRepository.getUserStoriesResult = persistentListOf(
            story(id = 1L, swimlane = 10L),
            story(id = 2L, swimlane = null)
        )

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(listOf(Swimlane.unclassified(), first), data.swimlanes)
    }

    // endregion

    // region default swimlane selection

    @Test
    fun `getData selects the stored swimlane when it still exists`() = runTest {
        val first = swimlane(id = 10L)
        val stored = swimlane(id = 20L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(first, stored)
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(defaultSwimlane = 10L)

        val data = sut.getData(storageSwimlane = 20L).getOrThrow()

        assertEquals(stored, data.defaultSwimlane)
    }

    @Test
    fun `getData falls back to the first swimlane when the stored one is gone`() = runTest {
        val first = swimlane(id = 10L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(first, swimlane(id = 20L))
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(defaultSwimlane = 20L)

        val data = sut.getData(storageSwimlane = 999L).getOrThrow()

        assertEquals(first, data.defaultSwimlane)
    }

    @Test
    fun `getData selects the project default swimlane when nothing is stored`() = runTest {
        val projectDefault = swimlane(id = 20L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(swimlane(id = 10L), projectDefault)
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(defaultSwimlane = 20L)

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(projectDefault, data.defaultSwimlane)
    }

    @Test
    fun `getData falls back to the first swimlane when the project default does not match`() = runTest {
        val first = swimlane(id = 10L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(first, swimlane(id = 20L))
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(defaultSwimlane = 999L)

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(first, data.defaultSwimlane)
    }

    @Test
    fun `getData has no default swimlane when the project has none`() = runTest {
        swimlanesRepository.getSwimlanesResult = persistentListOf()

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertNull(data.defaultSwimlane)
    }

    // endregion

    // region getData assembly

    @Test
    fun `getData sorts stories by kanban order`() = runTest {
        userStoriesRepository.getUserStoriesResult = persistentListOf(
            story(id = 3L, kanbanOrder = 30L),
            story(id = 1L, kanbanOrder = 10L),
            story(id = 2L, kanbanOrder = 20L)
        )

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(listOf(1L, 2L, 3L), data.stories.map { it.id })
    }

    @Test
    fun `getData maps permissions onto the add and modify flags`() = runTest {
        projectsRepository.getCurrentProjectSimpleResult = getProjectSimple().copy(
            defaultSwimlane = null,
            myPermissions = persistentListOf(TaigaPermission.ADD_US)
        )

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertTrue(data.canAddUserStory)
        assertFalse(data.canModifyUserStory)
    }

    @Test
    fun `getData carries statuses and team members through unchanged`() = runTest {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)

        val data = sut.getData(storageSwimlane = null).getOrThrow()

        assertEquals(listOf<Statuses>(todo, done), data.statuses)
        assertEquals(listOf(member), data.teamMembers)
    }

    @Test
    fun `getData groups stories by status using the selected swimlane`() = runTest {
        val selected = swimlane(id = 10L)
        swimlanesRepository.getSwimlanesResult = persistentListOf(selected)
        val inSwimlane = story(id = 1L, swimlane = 10L, status = todo)
        userStoriesRepository.getUserStoriesResult = persistentListOf(
            inSwimlane,
            story(id = 2L, swimlane = 20L, status = todo)
        )

        val data = sut.getData(storageSwimlane = 10L).getOrThrow()

        assertEquals(listOf(inSwimlane), data.storiesByStatus.getValue(todo).map { it.userStory })
        assertTrue(data.storiesByStatus.getValue(done).isEmpty())
    }

    // endregion

    // region getData failure paths

    @Test
    fun `getData returns failure when the project lookup throws`() = runTest {
        projectsRepository.getCurrentProjectSimpleThrows = testException

        assertFailedWithTestException(sut.getData(storageSwimlane = null))
    }

    @Test
    fun `getData returns failure when the stories lookup throws`() = runTest {
        userStoriesRepository.getUserStoriesThrows = testException

        assertFailedWithTestException(sut.getData(storageSwimlane = null))
    }

    @Test
    fun `getData returns failure when the team members lookup throws`() = runTest {
        usersRepository.getTeamMembersThrows = testException

        assertFailedWithTestException(sut.getData(storageSwimlane = null))
    }

    @Test
    fun `getData returns failure when the statuses lookup throws`() = runTest {
        filtersRepository.statusesThrows = testException

        assertFailedWithTestException(sut.getData(storageSwimlane = null))
    }

    @Test
    fun `getData returns failure when the swimlanes lookup throws`() = runTest {
        swimlanesRepository.getSwimlanesThrows = testException

        assertFailedWithTestException(sut.getData(storageSwimlane = null))
    }

    // endregion

    // region computeStoriesByStatus

    @Test
    fun `computeStoriesByStatus with a null swimlane keeps every story`() = runTest {
        val classified = story(id = 1L, swimlane = 10L)
        val unclassified = story(id = 2L, swimlane = null)

        val result = compute(stories = persistentListOf(classified, unclassified), swimlane = null)

        assertEquals(listOf(classified, unclassified), result.getValue(todo).map { it.userStory })
    }

    @Test
    fun `computeStoriesByStatus with the unclassified swimlane keeps only stories without one`() = runTest {
        val unclassified = story(id = 2L, swimlane = null)

        val result = compute(
            stories = persistentListOf(story(id = 1L, swimlane = 10L), unclassified),
            swimlane = Swimlane.unclassified()
        )

        assertEquals(listOf(unclassified), result.getValue(todo).map { it.userStory })
    }

    @Test
    fun `computeStoriesByStatus with a concrete swimlane keeps only stories matching its id`() = runTest {
        val matching = story(id = 1L, swimlane = 10L)

        val result = compute(
            stories = persistentListOf(
                matching,
                story(id = 2L, swimlane = 20L),
                story(id = 3L, swimlane = null)
            ),
            swimlane = swimlane(id = 10L)
        )

        assertEquals(listOf(matching), result.getValue(todo).map { it.userStory })
    }

    @Test
    fun `computeStoriesByStatus keys every status even when it has no stories`() = runTest {
        val result = compute(stories = persistentListOf(story(id = 1L, status = todo)), swimlane = null)

        assertEquals(setOf<Statuses>(todo, done), result.keys)
        assertTrue(result.getValue(done).isEmpty())
    }

    @Test
    fun `computeStoriesByStatus resolves assignees and drops unknown user ids`() = runTest {
        val known = getTeamMember(id = 100L)

        val result = compute(
            stories = persistentListOf(story(id = 1L, assignedUserIds = listOf(100L, 999L))),
            swimlane = null,
            teamMembers = persistentListOf(known)
        )

        assertEquals(listOf(known), result.getValue(todo).single().assignees)
    }

    @Test
    fun `computeStoriesByStatus resolves no assignees when the story has none`() = runTest {
        val result = compute(
            stories = persistentListOf(story(id = 1L, assignedUserIds = emptyList())),
            swimlane = null,
            teamMembers = persistentListOf(getTeamMember(id = 100L))
        )

        assertTrue(result.getValue(todo).single().assignees.isEmpty())
    }

    // endregion

    private suspend fun compute(
        stories: ImmutableList<UserStory>,
        swimlane: Swimlane?,
        teamMembers: ImmutableList<TeamMember> = persistentListOf(),
        statuses: ImmutableList<Statuses> = listOf<Statuses>(todo, done).toImmutableList()
    ) = sut.computeStoriesByStatus(
        stories = stories,
        statuses = statuses,
        teamMembers = teamMembers,
        swimlane = swimlane
    )

    /**
     * Asserts by type and message rather than identity: the failing call happens inside an `async`
     * child, and on JVM kotlinx-coroutines' stack-trace recovery rethrows a *copy* of the original
     * exception (the original becomes its `cause`).
     */
    private fun assertFailedWithTestException(result: Result<KanbanData>) {
        val exception = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(testException.message, exception.message)
    }
}
