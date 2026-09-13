package com.grappim.taigamobile.feature.workitem.ui.delegates.mentions

import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WorkItemMentionsDelegateImplTest {

    private val usersRepository = FakeUsersRepository()

    private fun createSut(): WorkItemMentionsDelegateImpl =
        WorkItemMentionsDelegateImpl(usersRepository = usersRepository)

    @Test
    fun `initial state should have no members`() {
        val sut = createSut()

        assertTrue(sut.mentionsState.value.members.isEmpty())
    }

    @Test
    fun `loadMembers should update state with team members on success`() = runTest {
        val sut = createSut()
        val members = persistentListOf(
            TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")
        )
        usersRepository.getTeamMembersResult = members

        sut.loadMembers()

        assertEquals(members, sut.mentionsState.value.members)
    }

    @Test
    fun `loadMembers should leave members empty on failure`() = runTest {
        val sut = createSut()
        usersRepository.getTeamMembersThrows = testException

        sut.loadMembers()

        assertTrue(sut.mentionsState.value.members.isEmpty())
    }
}
