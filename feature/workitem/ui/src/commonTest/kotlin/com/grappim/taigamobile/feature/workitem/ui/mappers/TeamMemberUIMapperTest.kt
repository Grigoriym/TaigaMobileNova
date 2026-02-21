package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.testing.getTeamMember
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TeamMemberUIMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sut: TeamMemberUIMapper

    @Before
    fun setUp() {
        sut = TeamMemberUIMapper(
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `toUI with TeamMember should return TeamMemberUI correctly`() = runTest {
        val teamMember = getTeamMember()

        val actual = sut.toUI(teamMember)

        assertEquals(teamMember.id, actual.id)
        assertEquals(teamMember.name, actual.name)
        assertEquals(teamMember.avatarUrl, actual.avatarUrl)
    }

    @Test
    fun `toUI with TeamMember with null avatarUrl should return null avatarUrl`() = runTest {
        val teamMember = getTeamMember(avatarUrl = null)

        val actual = sut.toUI(teamMember)

        assertEquals(teamMember.id, actual.id)
        assertEquals(teamMember.name, actual.name)
        assertNull(actual.avatarUrl)
    }

    @Test
    fun `toUI with list of TeamMembers should return list of TeamMemberUI correctly`() = runTest {
        val teamMember1 = getTeamMember()
        val teamMember2 = getTeamMember()
        val list = persistentListOf(teamMember1, teamMember2)

        val actual = sut.toUI(list)

        assertEquals(2, actual.size)
        assertEquals(teamMember1.id, actual[0].id)
        assertEquals(teamMember1.name, actual[0].name)
        assertEquals(teamMember1.avatarUrl, actual[0].avatarUrl)
        assertEquals(teamMember2.id, actual[1].id)
        assertEquals(teamMember2.name, actual[1].name)
        assertEquals(teamMember2.avatarUrl, actual[1].avatarUrl)
    }

    @Test
    fun `toUI with empty list should return empty list`() = runTest {
        val actual = sut.toUI(persistentListOf())

        assertTrue(actual.isEmpty())
    }
}
