package com.grappim.taigamobile.feature.teams.ui

import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getTeamMember
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
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

internal class TeamViewModelTest {

    private val usersRepository = FakeUsersRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: TeamViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = TeamViewModel(usersRepository = usersRepository)
    }

    // --- initial state ---
    // The main dispatcher is unconfined, so the init-block load has already run by the time
    // createViewModel() returns; these assert the state the screen first sees, not a pre-load one.

    @Test
    fun `initial state is not loading and has no error`() {
        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error is NativeText.Empty)
        assertEquals(persistentListOf(), state.teamMembers)
    }

    // --- initial load ---

    @Test
    fun `on init success team members are loaded into state`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error is NativeText.Empty)
        assertEquals(persistentListOf(member), state.teamMembers)
    }

    @Test
    fun `on init team members are requested with member stats`() {
        createViewModel()

        assertEquals(1, usersRepository.getTeamMembersCallCount)
        assertEquals(true, usersRepository.getTeamMembersGenerateMemberStats)
    }

    @Test
    fun `on init failure state has error and is not loading`() {
        usersRepository.getTeamMembersThrows = testException

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error !is NativeText.Empty)
        assertNull(state.teamMembers)
    }

    // --- refresh ---

    @Test
    fun `onRefresh reloads team members`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)
        createViewModel()

        val newMember = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member, newMember)

        sut.state.value.onRefresh()

        assertEquals(2, usersRepository.getTeamMembersCallCount)
        assertEquals(persistentListOf(member, newMember), sut.state.value.teamMembers)
    }

    @Test
    fun `onRefresh clears the error left by a failed load`() {
        usersRepository.getTeamMembersThrows = testException
        createViewModel()
        assertTrue(sut.state.value.error !is NativeText.Empty)

        usersRepository.getTeamMembersThrows = null
        usersRepository.getTeamMembersResult = persistentListOf(getTeamMember())

        sut.state.value.onRefresh()

        val state = sut.state.value
        assertTrue(state.error is NativeText.Empty)
        assertFalse(state.isLoading)
        assertEquals(1, state.teamMembers?.size)
    }

    @Test
    fun `onRefresh failure sets error and keeps previously loaded members`() {
        val member = getTeamMember()
        usersRepository.getTeamMembersResult = persistentListOf(member)
        createViewModel()

        usersRepository.getTeamMembersThrows = testException

        sut.state.value.onRefresh()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.error !is NativeText.Empty)
        assertEquals(persistentListOf(member), state.teamMembers)
    }
}
