package com.grappim.taigamobile.feature.profile.ui

import com.grappim.taigamobile.feature.profile.domain.GetProfileDataUseCase
import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ProfileViewModelTest {

    private val userId = getRandomLong()

    private val usersRepository = FakeUsersRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private val route = ProfileNavDestination(userId = userId)

    private lateinit var sut: ProfileViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        usersRepository.getUserResult = getUser()
        usersRepository.getUserStatsResult = UserStats(
            totalNumClosedUserStories = 1,
            totalNumContacts = 2,
            totalNumProjects = 3
        )
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = ProfileViewModel(
            route = route,
            getProfileDataUseCase = GetProfileDataUseCase(usersRepository, projectsRepository),
            taigaSessionStorage = sessionStorage
        )
    }

    // --- initial load success ---

    @Test
    fun `on init success user is loaded into state`() {
        val user = getUser()
        usersRepository.getUserResult = user

        createViewModel()

        assertEquals(user, sut.state.value.user)
    }

    @Test
    fun `on init success userStats is loaded into state`() {
        val stats = UserStats(
            totalNumClosedUserStories = 5,
            totalNumContacts = 10,
            totalNumProjects = 15
        )
        usersRepository.getUserStatsResult = stats

        createViewModel()

        assertEquals(stats, sut.state.value.userStats)
    }

    @Test
    fun `on init success projects are loaded into state`() {
        val project = getProject()
        projectsRepository.getUserProjectsResult = persistentListOf(project)

        createViewModel()

        assertEquals(1, sut.state.value.projects.size)
        assertEquals(project.id, sut.state.value.projects[0].id)
    }

    @Test
    fun `on init success currentProjectId comes from session storage`() {
        val projectId = getRandomLong()
        sessionStorage.currentProjectId = projectId

        createViewModel()

        assertEquals(projectId, sut.state.value.currentProjectId)
    }

    @Test
    fun `on init success state has no error and is not loading`() {
        createViewModel()

        assertTrue(sut.state.value.error is NativeText.Empty)
        assertFalse(sut.state.value.isLoading)
    }

    // --- initial load failure ---

    @Test
    fun `on init failure state has error and is not loading`() {
        usersRepository.getUserThrows = testException

        createViewModel()

        assertTrue(sut.state.value.error !is NativeText.Empty)
        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `on init failure user and stats remain null`() {
        usersRepository.getUserThrows = testException

        createViewModel()

        assertNull(sut.state.value.user)
        assertNull(sut.state.value.userStats)
    }

    @Test
    fun `on init failure projects remain empty`() {
        usersRepository.getUserThrows = testException

        createViewModel()

        assertTrue(sut.state.value.projects.isEmpty())
    }

    // --- reload ---

    @Test
    fun `onReload reloads profile data with updated values`() {
        createViewModel()

        val newUser = getUser()
        usersRepository.getUserResult = newUser

        sut.state.value.onReload()

        assertEquals(newUser, sut.state.value.user)
    }

    @Test
    fun `onReload after failure clears error on success`() {
        usersRepository.getUserThrows = testException
        createViewModel()

        assertTrue(sut.state.value.error !is NativeText.Empty)

        usersRepository.getUserThrows = null
        sut.state.value.onReload()

        assertTrue(sut.state.value.error is NativeText.Empty)
    }
}
