package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.api.UserMapper
import com.grappim.taigamobile.core.domain.DueDateStatus
import com.grappim.taigamobile.core.domain.DueDateStatusDTO
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.data.StatusMapper
import com.grappim.taigamobile.feature.filters.data.TagsMapper
import com.grappim.taigamobile.feature.projects.data.ProjectMapper
import com.grappim.taigamobile.feature.workitem.data.DueDateStatusMapper
import com.grappim.taigamobile.testing.getProject
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getStatus
import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class UserStoryMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val userMapper: UserMapper = mockk()
    private val statusMapper: StatusMapper = mockk()
    private val projectMapper: ProjectMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()
    private val dueDateStatusMapper: DueDateStatusMapper = mockk()
    private val serverStorage: ServerStorage = mockk()

    private lateinit var sut: UserStoryMapper

    @Before
    fun setup() {
        coEvery { serverStorage.server } returns "https://taiga.example.com"

        sut = UserStoryMapper(
            ioDispatcher = testDispatcher,
            userMapper = userMapper,
            statusMapper = statusMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            serverStorage = serverStorage
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() = runTest {
        val response = getWorkItemResponseDTO()
        val user = getUser()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(response.id, result.id)
        assertEquals(response.version, result.version)
        assertEquals(response.createdDate, result.createdDateTime)
        assertEquals(response.dueDate, result.dueDate)
        assertEquals(response.owner, result.creatorId)
        assertEquals(response.subject, result.title)
        assertEquals(response.description, result.description)
        assertEquals(response.ref, result.ref)
        assertEquals(response.isClosed, result.isClosed)
        assertEquals(response.milestone, result.milestone)
        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
        assertEquals(user, result.assignee)
    }

    @Test
    fun `toDomain should handle null owner with error`() = runTest {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertNotNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() = runTest {
        val user = getUser()
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(getTag())
        coEvery { statusMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/us/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() = runTest {
        val user = getUser()
        val response = getWorkItemResponseDTO()

        val firstTag = getTag()
        val secondTag = getTag()

        coEvery { userMapper.toUser(response.assignedToExtraInfo!!) } returns user
        coEvery { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns
            DueDateStatus.DueSoon
        coEvery { projectMapper.toProject(response.projectDTOExtraInfo) } returns getProject()
        coEvery { tagsMapper.toTags(response.tags) } returns persistentListOf(
            firstTag,
            secondTag
        )
        coEvery { statusMapper.getStatus(response) } returns getStatus()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
    }
}
