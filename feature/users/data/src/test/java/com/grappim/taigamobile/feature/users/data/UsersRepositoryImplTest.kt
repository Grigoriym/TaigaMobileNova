package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.UserStats
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.dto.StatsDTO
import com.grappim.taigamobile.feature.users.mapper.TeamMemberMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.users.mapper.UserStatsMapper
import com.grappim.taigamobile.testing.getProjectResponseDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getUserDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UsersRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val usersApi: UsersApi = mockk()
    private val projectsApi: ProjectsApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val userMapper: UserMapper = mockk()
    private val session: Session = mockk()
    private val teamMemberMapper: TeamMemberMapper = mockk()
    private val userStatsMapper: UserStatsMapper = mockk()

    private lateinit var sut: UsersRepository

    private val projectId = getRandomLong()
    private val currentUserId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery { taigaSessionStorage.requireUserId() } returns currentUserId

        sut = UsersRepositoryImpl(
            usersApi = usersApi,
            projectsApi = projectsApi,
            taigaSessionStorage = taigaSessionStorage,
            dispatcher = testDispatcher,
            userMapper = userMapper,
            session = session,
            teamMemberMapper = teamMemberMapper,
            userStatsMapper = userStatsMapper
        )
    }

    @Test
    fun `getMe should return mapped user`() = runTest {
        val userDTO = getUserDTO()
        val expectedUser = getUser()

        coEvery { usersApi.getMyProfile() } returns userDTO
        coEvery { userMapper.toUser(userDTO) } returns expectedUser

        val result = sut.getMe()

        assertEquals(expectedUser, result)
        coVerify { usersApi.getMyProfile() }
        coVerify { userMapper.toUser(userDTO) }
    }

    @Test
    fun `getUser should return mapped user by id`() = runTest {
        val userId = getRandomLong()
        val userDTO = getUserDTO()
        val expectedUser = getUser()

        coEvery { usersApi.getUser(userId) } returns userDTO
        coEvery { userMapper.toUser(userDTO) } returns expectedUser

        val result = sut.getUser(userId)

        assertEquals(expectedUser, result)
        coVerify { usersApi.getUser(userId) }
    }

    @Test
    fun `getUsersList should return list of mapped users`() = runTest {
        val userId1 = getRandomLong()
        val userId2 = getRandomLong()
        val userDTO1 = getUserDTO()
        val userDTO2 = getUserDTO()
        val user1 = getUser()
        val user2 = getUser()

        coEvery { usersApi.getUser(userId1) } returns userDTO1
        coEvery { usersApi.getUser(userId2) } returns userDTO2
        coEvery { userMapper.toUser(userDTO1) } returns user1
        coEvery { userMapper.toUser(userDTO2) } returns user2

        val result = sut.getUsersList(listOf(userId1, userId2))

        assertEquals(2, result.size)
    }

    @Test
    fun `getUsersList should return empty list for empty input`() = runTest {
        val result = sut.getUsersList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `getUserStats should return mapped user stats`() = runTest {
        val userId = getRandomLong()
        val statsDTO = mockk<StatsDTO>()
        val expectedStats = mockk<UserStats>()

        coEvery { usersApi.getUserStats(userId) } returns statsDTO
        coEvery { userStatsMapper.toDomain(statsDTO) } returns expectedStats

        val result = sut.getUserStats(userId)

        assertEquals(expectedStats, result)
        coVerify { usersApi.getUserStats(userId) }
        coVerify { userStatsMapper.toDomain(statsDTO) }
    }

    @Test
    fun `getTeamMembers should use current project id`() = runTest {
        val projectDTO = getProjectResponseDTO()
        val expectedMembers = persistentListOf<TeamMember>()

        coEvery { projectsApi.getProject(projectId) } returns projectDTO
        coEvery { teamMemberMapper.toDomain(projectDTO.members, emptyMap()) } returns expectedMembers

        val result = sut.getTeamMembers(generateMemberStats = false)

        assertEquals(expectedMembers, result)
        coVerify { taigaSessionStorage.getCurrentProjectId() }
        coVerify { projectsApi.getProject(projectId) }
    }

    @Test
    fun `getTeamMembersByProjectId should return mapped team members without stats`() = runTest {
        val customProjectId = getRandomLong()
        val projectDTO = getProjectResponseDTO()
        val expectedMembers = persistentListOf<TeamMember>()

        coEvery { projectsApi.getProject(customProjectId) } returns projectDTO
        coEvery { teamMemberMapper.toDomain(projectDTO.members, emptyMap()) } returns expectedMembers

        val result = sut.getTeamMembersByProjectId(customProjectId, generateMemberStats = false)

        assertEquals(expectedMembers, result)
        coVerify { teamMemberMapper.toDomain(projectDTO.members, emptyMap()) }
    }

    @Test
    fun `getTeamMembersByProjectId should generate member stats when requested`() = runTest {
        val projectDTO = getProjectResponseDTO()
        val memberStatsResponse = MemberStatsResponseDTO(
            closedBugs = mapOf("1" to 5, "2" to 3),
            closedTasks = mapOf("1" to 2),
            createdBugs = mapOf("2" to 1),
            iocaineTasks = emptyMap(),
            wikiChanges = emptyMap()
        )
        val expectedMembers = persistentListOf<TeamMember>()

        coEvery { projectsApi.getProject(projectId) } returns projectDTO
        coEvery { usersApi.getMemberStats(projectId) } returns memberStatsResponse
        coEvery { teamMemberMapper.toDomain(projectDTO.members, any()) } returns expectedMembers

        sut.getTeamMembersByProjectId(projectId, generateMemberStats = true)

        coVerify { usersApi.getMemberStats(projectId) }
    }

    @Test
    fun `isAnyAssignedToMe should return true when current user is in list`() = runTest {
        val currentUser = getUser().copy(id = currentUserId, pk = currentUserId)
        val otherUser = getUser()

        val result = sut.isAnyAssignedToMe(persistentListOf(currentUser, otherUser))

        assertTrue(result)
    }

    @Test
    fun `isAnyAssignedToMe should return false when current user is not in list`() = runTest {
        val otherUser1 = getUser()
        val otherUser2 = getUser()

        val result = sut.isAnyAssignedToMe(persistentListOf(otherUser1, otherUser2))

        assertFalse(result)
    }

    @Test
    fun `isAnyAssignedToMe should return false for empty list`() = runTest {
        val result = sut.isAnyAssignedToMe(persistentListOf())

        assertFalse(result)
    }
}
