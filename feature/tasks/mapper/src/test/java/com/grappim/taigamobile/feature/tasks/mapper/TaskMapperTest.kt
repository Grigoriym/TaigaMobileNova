package com.grappim.taigamobile.feature.tasks.mapper

import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.UserStoryShortInfo
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.UserStoryShortInfoMapper
import com.grappim.taigamobile.testing.models.getTag
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getUserStoryShortInfoDTO
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaskMapperTest {

    private val userMapper: UserMapper = UserMapper()
    private val statusesMapper: StatusesMapper = StatusesMapper()
    private val projectMapper: ProjectMapper = ProjectMapper()
    private val tagsMapper: TagsMapper = TagsMapper()
    private val dueDateStatusMapper: DueDateStatusMapper = DueDateStatusMapper()
    private val userStoryShortInfoMapper: UserStoryShortInfoMapper = UserStoryShortInfoMapper()
    private val serverStorage = FakeServerStorage()

    private lateinit var sut: TaskMapper

    @BeforeTest
    fun setup() {
        sut = TaskMapper(
            serverStorage = serverStorage,
            userMapper = userMapper,
            statusesMapper = statusesMapper,
            projectMapper = projectMapper,
            tagsMapper = tagsMapper,
            dueDateStatusMapper = dueDateStatusMapper,
            userStoryShortInfoMapper = userStoryShortInfoMapper
        )
    }

    @Test
    fun `toDomain should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()
        val user = getUser()
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
    fun `toDomain should handle null owner with error`() {
        val response = getWorkItemResponseDTO().copy(owner = null)

        try {
            sut.toDomain(response)
            assert(false) { "Expected error for null owner" }
        } catch (e: IllegalStateException) {
            assertEquals("Owner field is null", e.message)
        }
    }

    @Test
    fun `toDomain should map due date status correctly`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)

        val result = sut.toDomain(response)

        assertNotNull(result.dueDateStatus)
        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly when blocked`() {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )

        val result = sut.toDomain(response)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should not include blocked note when not blocked`() {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = false,
            blockedNote = blockedNote
        )
        val result = sut.toDomain(response)

        assertNull(result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() {
        val response = getWorkItemResponseDTO()

        val result = sut.toDomain(response)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/task/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly`() {
        val response = getWorkItemResponseDTO()
        val firstTag = getTag()
        val secondTag = getTag()

        val result = sut.toDomain(response)

        assertEquals(2, result.tags.size)
        assertEquals(firstTag, result.tags[0])
        assertEquals(secondTag, result.tags[1])
    }

    @Test
    fun `toDomain should handle null assignee`() {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

        val result = sut.toDomain(response)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should use assignedUsers when available`() {
        val user = getUser()
        val assignedUsers = listOf(getRandomLong(), getRandomLong(), getRandomLong())
        val response = getWorkItemResponseDTO().copy(assignedUsers = assignedUsers)

        val result = sut.toDomain(response)

        assertEquals(assignedUsers, result.assignedUserIds)
    }

    @Test
    fun `toDomain should fallback to assignedTo when assignedUsers is null`() {
        val user = getUser()
        val assignedTo = getRandomLong()
        val response = getWorkItemResponseDTO().copy(
            assignedUsers = null,
            assignedTo = assignedTo
        )

        val result = sut.toDomain(response)

        assertEquals(listOf(assignedTo), result.assignedUserIds)
    }

    @Test
    fun `toDomain should handle empty watchers`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(watchers = null)

        val result = sut.toDomain(response)

        assertEquals(emptyList(), result.watcherUserIds)
    }

    @Test
    fun `toDomain should handle null description`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(description = null)

        val result = sut.toDomain(response)

        assertEquals("", result.description)
    }

    @Test
    fun `toDomain should map userStory when present`() {
        val user = getUser()
        val userStoryShortInfoDTO = getUserStoryShortInfoDTO()
        val userStoryShortInfo = UserStoryShortInfo(
            id = userStoryShortInfoDTO.id,
            ref = userStoryShortInfoDTO.ref,
            title = userStoryShortInfoDTO.title,
            epics = persistentListOf()
        )
        val response = getWorkItemResponseDTO().copy(userStoryExtraInfo = userStoryShortInfoDTO)

        val result = sut.toDomain(response)

        assertNotNull(result.userStory)
        assertEquals(userStoryShortInfo.id, result.userStory?.id)
        assertEquals(userStoryShortInfo.ref, result.userStory?.ref)
        assertEquals(userStoryShortInfo.title, result.userStory?.title)
    }

    @Test
    fun `toDomain should handle null userStory`() {
        val user = getUser()
        val response = getWorkItemResponseDTO().copy(userStoryExtraInfo = null)

        val result = sut.toDomain(response)

        assertNull(result.userStory)
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() {
        val response1 = getWorkItemResponseDTO()
        val response2 = getWorkItemResponseDTO()
        val dtos = listOf(response1, response2)

        val result = sut.toDomainList(dtos)

        assertEquals(2, result.size)
        assertEquals(response1.id, result[0].id)
        assertEquals(response2.id, result[1].id)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }
}
