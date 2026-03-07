package com.grappim.taigamobile.feature.users.data

import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.users.dto.MemberStatsResponseDTO
import com.grappim.taigamobile.feature.users.mapper.TeamMemberMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.users.mapper.UserStatsMapper
import com.grappim.taigamobile.testing.api.FakeProjectsApi
import com.grappim.taigamobile.testing.api.FakeUsersApi
import com.grappim.taigamobile.testing.models.getProjectMemberDTO
import com.grappim.taigamobile.testing.models.getProjectResponseDTO
import com.grappim.taigamobile.testing.models.getStatsDTO
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getUserDTO
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UsersRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val usersApi = FakeUsersApi()
    private val projectsApi = FakeProjectsApi()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val userMapper = UserMapper()
    private val teamMemberMapper = TeamMemberMapper()
    private val userStatsMapper = UserStatsMapper()

    private lateinit var sut: UsersRepository

    private val projectId = getRandomLong()
    private val currentUserId = getRandomLong()

    @BeforeTest
    fun setup() {
        sut = UsersRepositoryImpl(
            usersApi = usersApi,
            projectsApi = projectsApi,
            taigaSessionStorage = taigaSessionStorage,
            dispatcher = testDispatcher,
            userMapper = userMapper,
            teamMemberMapper = teamMemberMapper,
            userStatsMapper = userStatsMapper
        )
    }

    @Test
    fun `getMe should return mapped user`() = runTest {
        val dto = getUserDTO()
        usersApi.myProfile = dto

        val result = sut.getMe()

        assertEquals(userMapper.toUser(dto), result)
    }

    @Test
    fun `getUser should return mapped user by id`() = runTest {
        val dto = getUserDTO()
        usersApi.userById[dto.id!!] = dto

        val result = sut.getUser(dto.id!!)

        assertEquals(userMapper.toUser(dto), result)
    }

    @Test
    fun `getUsersList should return list of mapped users`() = runTest {
        val dto1 = getUserDTO()
        val dto2 = getUserDTO()
        usersApi.userById[dto1.id!!] = dto1
        usersApi.userById[dto2.id!!] = dto2

        val result = sut.getUsersList(listOf(dto1.id!!, dto2.id!!))

        assertEquals(2, result.size)
        assertEquals(userMapper.toUser(dto1), result.first { it.actualId == dto1.id!! })
        assertEquals(userMapper.toUser(dto2), result.first { it.actualId == dto2.id!! })
    }

    @Test
    fun `getUsersList should return empty list for empty input`() = runTest {
        val result = sut.getUsersList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `getUserStats should return mapped user stats`() = runTest {
        val dto = getStatsDTO()
        usersApi.statsDTO = dto

        val result = sut.getUserStats(getRandomLong())

        assertEquals(userStatsMapper.toDomain(dto), result)
    }

    @Test
    fun `getTeamMembers should use current project id`() = runTest {
        val dto = getProjectResponseDTO()
        projectsApi.projectResponseDTO = dto
        taigaSessionStorage.currentProjectId = projectId

        val result = sut.getTeamMembers(generateMemberStats = false)

        assertEquals(teamMemberMapper.toDomain(dto.members, emptyMap()), result)
    }

    @Test
    fun `getTeamMembersByProjectId should return mapped team members without stats`() = runTest {
        val dto = getProjectResponseDTO()
        projectsApi.projectResponseDTO = dto

        val result = sut.getTeamMembersByProjectId(projectId, generateMemberStats = false)

        assertEquals(teamMemberMapper.toDomain(dto.members, emptyMap()), result)
    }

    @Test
    fun `getTeamMembersByProjectId should generate member stats when requested`() = runTest {
        val member1 = getProjectMemberDTO()
        val member2 = getProjectMemberDTO()
        projectsApi.projectResponseDTO = ProjectResponseDTO(
            id = projectId,
            name = getRandomString(),
            members = listOf(member1, member2)
        )
        usersApi.memberStatsResponseDTO = MemberStatsResponseDTO(
            closedBugs = mapOf(member1.id.toString() to 3),
            closedTasks = mapOf(member1.id.toString() to 2),
            createdBugs = emptyMap(),
            iocaineTasks = emptyMap(),
            wikiChanges = emptyMap()
        )
        taigaSessionStorage.currentProjectId = projectId

        val result = sut.getTeamMembersByProjectId(projectId, generateMemberStats = true)

        assertEquals(5, result.first { it.id == member1.id }.totalPower)
        assertNull(result.first { it.id == member2.id }.totalPower)
    }

    @Test
    fun `isAnyAssignedToMe should return true when current user is in list`() = runTest {
        taigaSessionStorage.currentUserId = currentUserId
        val currentUser = getUser().copy(id = currentUserId, pk = currentUserId)
        val otherUser = getUser()

        val result = sut.isAnyAssignedToMe(persistentListOf(currentUser, otherUser))

        assertTrue(result)
    }

    @Test
    fun `isAnyAssignedToMe should return false when current user is not in list`() = runTest {
        taigaSessionStorage.currentUserId = currentUserId
        val otherUser1 = getUser()
        val otherUser2 = getUser()

        val result = sut.isAnyAssignedToMe(persistentListOf(otherUser1, otherUser2))

        assertFalse(result)
    }

    @Test
    fun `isAnyAssignedToMe should return false for empty list`() = runTest {
        taigaSessionStorage.currentUserId = currentUserId

        val result = sut.isAnyAssignedToMe(persistentListOf())

        assertFalse(result)
    }
}
