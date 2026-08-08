package com.grappim.taigamobile.feature.userstories.domain

import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.models.getComment
import com.grappim.taigamobile.testing.models.getCustomFields
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.assertFailsWithTestException
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UserStoryDetailsDataUseCaseTest {

    private val filtersRepository = FakeFiltersRepository()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val historyRepository = FakeHistoryRepository()
    private val sprintsRepository = FakeSprintsRepository()
    private val usersRepository = FakeUsersRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val projectsRepository = FakeProjectsRepository()

    private lateinit var sut: UserStoryDetailsDataUseCase

    @BeforeTest
    fun setup() {
        userStoriesRepository.getUserStoryResult = getUserStory()
        filtersRepository.filtersDataResult = getFiltersData()
        workItemRepository.getWorkItemAttachmentsResult = persistentListOf(getAttachment())
        workItemRepository.getCustomFieldsResult = getCustomFields()
        historyRepository.getCommentsResult = persistentListOf(getComment())
        sprintsRepository.getSprintResult = getSprint()
        usersRepository.getUserResult = getUser()
        usersRepository.getUsersListResult = persistentListOf(getUser())
        usersRepository.isAnyAssignedToMeResult = false
        projectsRepository.permissions = persistentListOf()

        sut = UserStoryDetailsDataUseCaseImpl(
            filtersRepository = filtersRepository,
            userStoriesRepository = userStoriesRepository,
            historyRepository = historyRepository,
            sprintsRepository = sprintsRepository,
            usersRepository = usersRepository,
            workItemRepository = workItemRepository,
            projectsRepository = projectsRepository
        )
    }

    // region getUserStory

    @Test
    fun `getUserStory returns the story from the repository`() = runTest {
        val userStory = getUserStory()
        userStoriesRepository.getUserStoryResult = userStory

        val result = sut.getUserStory(userStory.id)

        assertEquals(userStory, result)
    }

    @Test
    fun `getUserStory propagates a repository failure`() = runTest {
        userStoriesRepository.getUserStoryThrows = testException

        assertFailsWithTestException {
            sut.getUserStory(getRandomLong())
        }
    }

    // endregion

    // region getUserStoryData assembly

    @Test
    fun `getUserStoryData assembles UserStoryDetailsData from every source`() = runTest {
        val userStory = getUserStory().copy(milestone = null)
        val attachments = persistentListOf(getAttachment(), getAttachment())
        val customFields = getCustomFields()
        val comments = persistentListOf(getComment())
        val creator = getUser()
        val members = persistentListOf(getUser(), getUser())
        val filtersData = getFiltersData()
        userStoriesRepository.getUserStoryResult = userStory
        workItemRepository.getWorkItemAttachmentsResult = attachments
        workItemRepository.getCustomFieldsResult = customFields
        historyRepository.getCommentsResult = comments
        usersRepository.getUserResult = creator
        usersRepository.getUsersListResult = members
        usersRepository.isAnyAssignedToMeResult = true
        filtersRepository.filtersDataResult = filtersData
        projectsRepository.permissions = persistentListOf(
            TaigaPermission.DELETE_US,
            TaigaPermission.MODIFY_EPIC
        )

        val result = sut.getUserStoryData(userStory.id)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(userStory, data.userStory)
        assertEquals(attachments, data.attachments)
        assertNull(data.sprint)
        assertEquals(customFields, data.customFields)
        assertEquals(comments, data.comments)
        assertEquals(creator, data.creator)
        assertEquals(members, data.assignees)
        assertEquals(members, data.watchers)
        assertTrue(data.isAssignedToMe)
        assertTrue(data.isWatchedByMe)
        assertEquals(filtersData, data.filtersData)
        assertTrue(data.canDeleteUserStory)
        assertTrue(data.canModifyRelatedEpic)
        assertEquals(false, data.canModifyUserStory)
        assertEquals(false, data.canComment)
    }

    @Test
    fun `getUserStoryData fetches the sprint when the story has a milestone`() = runTest {
        val sprint = getSprint()
        val userStory = getUserStory().copy(milestone = getRandomLong())
        userStoriesRepository.getUserStoryResult = userStory
        sprintsRepository.getSprintResult = sprint

        val result = sut.getUserStoryData(userStory.id)

        assertEquals(sprint, result.getOrThrow().sprint)
    }

    @Test
    fun `getUserStoryData has no sprint when the story has no milestone`() = runTest {
        val userStory = getUserStory().copy(milestone = null)
        userStoriesRepository.getUserStoryResult = userStory

        val result = sut.getUserStoryData(userStory.id)

        assertNull(result.getOrThrow().sprint)
    }

    // endregion

    // region getUserStoryData failure paths

    @Test
    fun `getUserStoryData returns failure when getFiltersData throws`() = runTest {
        filtersRepository.filtersDataThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getUserStory throws`() = runTest {
        userStoriesRepository.getUserStoryThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getWorkItemAttachments throws`() = runTest {
        workItemRepository.getWorkItemAttachmentsThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getCustomFields throws`() = runTest {
        workItemRepository.getCustomFieldsThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getComments throws`() = runTest {
        historyRepository.getCommentsThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getSprint throws`() = runTest {
        val userStory = getUserStory().copy(milestone = getRandomLong())
        userStoriesRepository.getUserStoryResult = userStory
        sprintsRepository.getSprintThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(userStory.id))
    }

    @Test
    fun `getUserStoryData returns failure when getUser for the creator throws`() = runTest {
        usersRepository.getUserThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getUsersList throws`() = runTest {
        usersRepository.getUsersListThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when isAnyAssignedToMe throws`() = runTest {
        usersRepository.isAnyAssignedToMeThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    @Test
    fun `getUserStoryData returns failure when getPermissions throws`() = runTest {
        projectsRepository.getPermissionsThrows = testException

        assertFailedWithTestException(sut.getUserStoryData(getRandomLong()))
    }

    // endregion

    // region deleteUserStory

    @Test
    fun `deleteUserStory succeeds when the repository call succeeds`() = runTest {
        val id = getRandomLong()

        val result = sut.deleteUserStory(id)

        assertTrue(result.isSuccess)
        assertTrue(userStoriesRepository.deleteUserStoryCalled)
    }

    @Test
    fun `deleteUserStory returns failure when the repository throws`() = runTest {
        userStoriesRepository.deleteUserStoryThrows = testException

        val result = sut.deleteUserStory(getRandomLong())

        assertFailedWithTestException(result)
    }

    // endregion

    /**
     * Asserts by type and message rather than identity: several of the failing calls happen inside
     * an [kotlinx.coroutines.async] child, and on JVM kotlinx-coroutines' stack-trace recovery
     * rethrows a *copy* of the original exception (the original becomes its `cause`). So
     * `assertEquals(testException, …)` fails here even though nothing wrapped the exception on
     * purpose.
     */
    private fun assertFailedWithTestException(result: Result<*>) {
        val exception = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(testException.message, exception.message)
    }
}
