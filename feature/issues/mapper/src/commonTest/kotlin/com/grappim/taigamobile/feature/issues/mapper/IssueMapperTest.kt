package com.grappim.taigamobile.feature.issues.mapper

import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.dto.GeneratedUserStoryDTO
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IssueMapperTest {

    private lateinit var sut: IssueMapper

    @BeforeTest
    fun setup() {
        sut = IssueMapper(
            userMapper = UserMapper(),
            statusesMapper = StatusesMapper(),
            projectMapper = ProjectMapper(),
            tagsMapper = TagsMapper(),
            dueDateStatusMapper = DueDateStatusMapper(),
            serverStorage = FakeServerStorage()
        )
    }

    @Test
    fun `toDomain without filters should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()

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
    }

    @Test
    fun `toDomain should map basic fields correctly`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

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
    }

    @Test
    fun `toDomain should handle null owner with error`() {
        val response = getWorkItemResponseDTO().copy(owner = null)
        val filtersData = getFiltersData()

        val exception = assertFailsWith<IllegalStateException> {
            sut.toDomain(response, filtersData)
        }
        assertEquals("Owner field is null", exception.message)
    }

    @Test
    fun `toDomain should map due date status correctly`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
    }

    @Test
    fun `toDomain should map null due date status to null`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = null)

        val result = sut.toDomain(response)

        assertNull(result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle blocked note correctly`() {
        val blockedNote = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            isBlocked = true,
            blockedNote = blockedNote
        )
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        assertEquals(blockedNote, result.blockedNote)
    }

    @Test
    fun `toDomain should return null blocked note when not blocked`() {
        val response = getWorkItemResponseDTO().copy(
            isBlocked = false,
            blockedNote = getRandomString()
        )

        val result = sut.toDomain(response)

        assertNull(result.blockedNote)
    }

    @Test
    fun `toDomain should build correct copy link URL`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        val expectedUrl =
            "https://taiga.example.com/project/${response.projectDTOExtraInfo.slug}/issue/${response.ref}"
        assertEquals(expectedUrl, result.copyLinkUrl)
    }

    @Test
    fun `toDomain should map tags correctly from nested list`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        assertEquals(response.tags?.size, result.tags.size)
    }

    @Test
    fun `toDomain should map assignee from assignedToExtraInfo`() {
        val response = getWorkItemResponseDTO()

        val result = sut.toDomain(response)

        assertNotNull(result.assignee)
        assertEquals(response.assignedToExtraInfo!!.id, result.assignee!!.id)
        assertEquals(response.assignedToExtraInfo!!.fullName, result.assignee!!.fullName)
    }

    @Test
    fun `toDomain should return null assignee when assignedToExtraInfo is null`() {
        val response = getWorkItemResponseDTO().copy(assignedToExtraInfo = null)

        val result = sut.toDomain(response)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should map type from filters when matching id exists`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData().copy(
            types = persistentListOf(
                StatusFilters(
                    id = response.type!!,
                    name = "Bug",
                    color = "#FF0000",
                    count = 1
                )
            )
        )

        val result = sut.toDomain(response, filtersData)

        assertNotNull(result.type)
        assertEquals(response.type, result.type!!.id)
        assertEquals("Bug", result.type!!.name)
    }

    @Test
    fun `toDomain should map generatedUserStories to PromotedUserStoryInfo correctly`() {
        val storyId = getRandomLong()
        val storyRef = getRandomLong()
        val storySubject = getRandomString()
        val response = getWorkItemResponseDTO().copy(
            generatedUserStories = listOf(
                GeneratedUserStoryDTO(id = storyId, ref = storyRef, subject = storySubject)
            )
        )
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        assertEquals(1, result.promotedUserStories.size)
        val promoted = result.promotedUserStories[0]
        assertEquals(storyId, promoted.id)
        assertEquals(storyRef, promoted.ref)
        assertEquals(storySubject, promoted.subject)
        assertEquals("#$storyRef $storySubject", promoted.titleToDisplay)
    }

    @Test
    fun `toDomain should return empty promotedUserStories when generatedUserStories is null`() {
        val response = getWorkItemResponseDTO().copy(generatedUserStories = null)
        val filtersData = getFiltersData()

        val result = sut.toDomain(response, filtersData)

        assertTrue(result.promotedUserStories.isEmpty())
    }

    @Test
    fun `toDomain should throw when generatedUserStory has null ref`() {
        val response = getWorkItemResponseDTO().copy(
            generatedUserStories = listOf(
                GeneratedUserStoryDTO(id = getRandomLong(), ref = null, subject = getRandomString())
            )
        )
        val filtersData = getFiltersData()

        assertFailsWith<IllegalArgumentException> {
            sut.toDomain(response, filtersData)
        }
    }
}
