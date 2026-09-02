package com.grappim.taigamobile.feature.profile.domain

import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.utils.getRandomInt
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GetProfileDataUseCaseTest {

    private val userId = getRandomLong()

    private val usersRepository = FakeUsersRepository()
    private val projectsRepository = FakeProjectsRepository()

    private lateinit var sut: GetProfileDataUseCase

    @BeforeTest
    fun setup() {
        usersRepository.getUserResult = getUser()
        usersRepository.getUserStatsResult = userStats()

        sut = GetProfileDataUseCase(
            usersRepository = usersRepository,
            projectsRepository = projectsRepository
        )
    }

    private fun userStats() = UserStats(
        totalNumClosedUserStories = getRandomInt(),
        totalNumContacts = getRandomInt(),
        totalNumProjects = getRandomInt()
    )

    @Test
    fun `getProfileData assembles ProfileData from all three sources`() = runTest {
        val user = getUser()
        val stats = userStats()
        val projects = persistentListOf(getProject(), getProject())
        usersRepository.getUserResult = user
        usersRepository.getUserStatsResult = stats
        projectsRepository.getUserProjectsResult = projects

        val result = sut.getProfileData(userId)

        assertTrue(result.isSuccess)
        assertEquals(ProfileData(user = user, userStats = stats, projects = projects), result.getOrThrow())
    }

    @Test
    fun `getProfileData succeeds when the user has no projects`() = runTest {
        projectsRepository.getUserProjectsResult = persistentListOf()

        val result = sut.getProfileData(userId)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().projects.isEmpty())
    }

    @Test
    fun `getProfileData returns failure when getUser throws`() = runTest {
        usersRepository.getUserThrows = testException

        val result = sut.getProfileData(userId)

        assertFailedWithTestException(result)
    }

    @Test
    fun `getProfileData returns failure when getUserStats throws`() = runTest {
        usersRepository.getUserStatsThrows = testException

        val result = sut.getProfileData(userId)

        assertFailedWithTestException(result)
    }

    @Test
    fun `getProfileData returns failure when getUserProjects throws`() = runTest {
        projectsRepository.getUserProjectsThrows = testException

        val result = sut.getProfileData(userId)

        assertFailedWithTestException(result)
    }

    /**
     * Asserts by type and message rather than identity: the failing call happens inside an [async]
     * child, and on JVM kotlinx-coroutines' stack-trace recovery rethrows a *copy* of the original
     * exception (the original becomes its `cause`). So `assertEquals(testException, …)` fails here
     * even though nothing wrapped the exception on purpose.
     */
    private fun assertFailedWithTestException(result: Result<ProfileData>) {
        val exception = assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(testException.message, exception.message)
    }
}
