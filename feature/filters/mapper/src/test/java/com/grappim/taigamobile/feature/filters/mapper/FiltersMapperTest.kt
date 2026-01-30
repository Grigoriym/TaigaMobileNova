package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.feature.filters.dto.TagDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FiltersMapperTest {
    private lateinit var sut: FiltersMapper

    @Before
    fun setup() {
        sut = FiltersMapper()
    }

    @Test
    fun `toDomain should map assignees correctly and filter out null ids`() {
        val userWithId = FiltersDataResponseDTO.UserFilter(
            id = getRandomLong(),
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val userWithNullId = FiltersDataResponseDTO.UserFilter(
            id = null,
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val dto = createMinimalDTO(assignedTo = listOf(userWithId, userWithNullId))

        val result = sut.toDomain(dto)

        assertEquals(1, result.assignees.size)
        assertEquals(userWithId.id, result.assignees[0].id)
        assertEquals(userWithId.fullName, result.assignees[0].name)
        assertEquals(userWithId.count, result.assignees[0].count)
    }

    @Test
    fun `toDomain should map createdBy correctly and filter out null ids`() {
        val ownerWithId = FiltersDataResponseDTO.UserFilter(
            id = getRandomLong(),
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val ownerWithNullId = FiltersDataResponseDTO.UserFilter(
            id = null,
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val dto = createMinimalDTO(owners = listOf(ownerWithId, ownerWithNullId))

        val result = sut.toDomain(dto)

        assertEquals(1, result.createdBy.size)
        assertEquals(ownerWithId.id, result.createdBy[0].id)
        assertEquals(ownerWithId.fullName, result.createdBy[0].name)
        assertEquals(ownerWithId.count, result.createdBy[0].count)
    }

    @Test
    fun `toDomain should map statuses correctly`() {
        val status = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#FF0000",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(statuses = listOf(status))

        val result = sut.toDomain(dto)

        assertEquals(1, result.statuses.size)
        assertEquals(status.id, result.statuses[0].id)
        assertEquals(status.name, result.statuses[0].name)
        assertEquals(status.color, result.statuses[0].color)
        assertEquals(status.count, result.statuses[0].count)
    }

    @Test
    fun `toDomain should use default color when status color is null`() {
        val status = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = null,
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(statuses = listOf(status))

        val result = sut.toDomain(dto)

        assertEquals("#A9AABC", result.statuses[0].color)
    }

    @Test
    fun `toDomain should map priorities correctly`() {
        val priority = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#00FF00",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(priorities = listOf(priority))

        val result = sut.toDomain(dto)

        assertEquals(1, result.priorities.size)
        assertEquals(priority.id, result.priorities[0].id)
        assertEquals(priority.name, result.priorities[0].name)
        assertEquals(priority.color, result.priorities[0].color)
        assertEquals(priority.count, result.priorities[0].count)
    }

    @Test
    fun `toDomain should return empty priorities when null`() {
        val dto = createMinimalDTO(priorities = null)

        val result = sut.toDomain(dto)

        assertTrue(result.priorities.isEmpty())
    }

    @Test
    fun `toDomain should map severities correctly`() {
        val severity = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#0000FF",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(severities = listOf(severity))

        val result = sut.toDomain(dto)

        assertEquals(1, result.severities.size)
        assertEquals(severity.id, result.severities[0].id)
        assertEquals(severity.name, result.severities[0].name)
        assertEquals(severity.color, result.severities[0].color)
        assertEquals(severity.count, result.severities[0].count)
    }

    @Test
    fun `toDomain should return empty severities when null`() {
        val dto = createMinimalDTO(severities = null)

        val result = sut.toDomain(dto)

        assertTrue(result.severities.isEmpty())
    }

    @Test
    fun `toDomain should map roles correctly`() {
        val role = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#FFFF00",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(roles = listOf(role))

        val result = sut.toDomain(dto)

        assertEquals(1, result.roles.size)
        assertEquals(role.id, result.roles[0].id)
        assertEquals(role.name, result.roles[0].name)
        assertEquals(role.color, result.roles[0].color)
        assertEquals(role.count, result.roles[0].count)
    }

    @Test
    fun `toDomain should return empty roles when null`() {
        val dto = createMinimalDTO(roles = null)

        val result = sut.toDomain(dto)

        assertTrue(result.roles.isEmpty())
    }

    @Test
    fun `toDomain should map tags correctly`() {
        val tag = TagDTO(
            name = getRandomString(),
            color = "#FF00FF",
            count = getRandomLong()
        )
        val dto = createMinimalDTO(tags = listOf(tag))

        val result = sut.toDomain(dto)

        assertEquals(1, result.tags.size)
        assertEquals(tag.name, result.tags[0].name)
        assertEquals(tag.color, result.tags[0].color)
        assertEquals(tag.count, result.tags[0].count)
    }

    @Test
    fun `toDomain should return empty tags when null`() {
        val dto = createMinimalDTO(tags = null)

        val result = sut.toDomain(dto)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `toDomain should use default color when tag color is null`() {
        val tag = TagDTO(
            name = getRandomString(),
            color = null,
            count = getRandomLong()
        )
        val dto = createMinimalDTO(tags = listOf(tag))

        val result = sut.toDomain(dto)

        assertEquals("#A9AABC", result.tags[0].color)
    }

    @Test
    fun `toDomain should map types correctly`() {
        val type = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#00FFFF",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val dto = createMinimalDTO(types = listOf(type))

        val result = sut.toDomain(dto)

        assertEquals(1, result.types.size)
        assertEquals(type.id, result.types[0].id)
        assertEquals(type.name, result.types[0].name)
        assertEquals(type.color, result.types[0].color)
        assertEquals(type.count, result.types[0].count)
    }

    @Test
    fun `toDomain should return empty types when null`() {
        val dto = createMinimalDTO(types = null)

        val result = sut.toDomain(dto)

        assertTrue(result.types.isEmpty())
    }

    @Test
    fun `toDomain should map epics with formatted name`() {
        val epic = FiltersDataResponseDTO.EpicsFilter(
            id = getRandomLong(),
            ref = 42L,
            subject = "Epic Subject",
            count = getRandomLong()
        )
        val dto = createMinimalDTO(epics = listOf(epic))

        val result = sut.toDomain(dto)

        assertEquals(1, result.epics.size)
        assertEquals(epic.id, result.epics[0].id)
        assertEquals("#42 Epic Subject", result.epics[0].name)
        assertEquals(epic.count, result.epics[0].count)
    }

    @Test
    fun `toDomain should return empty name when epic subject is null`() {
        val epic = FiltersDataResponseDTO.EpicsFilter(
            id = getRandomLong(),
            ref = 42L,
            subject = null,
            count = getRandomLong()
        )
        val dto = createMinimalDTO(epics = listOf(epic))

        val result = sut.toDomain(dto)

        assertEquals(1, result.epics.size)
        assertEquals("", result.epics[0].name)
    }

    @Test
    fun `toDomain should return empty epics when null`() {
        val dto = createMinimalDTO(epics = null)

        val result = sut.toDomain(dto)

        assertTrue(result.epics.isEmpty())
    }

    @Test
    fun `toDomain should map all fields correctly`() {
        val assignee = FiltersDataResponseDTO.UserFilter(
            id = getRandomLong(),
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val owner = FiltersDataResponseDTO.UserFilter(
            id = getRandomLong(),
            fullName = getRandomString(),
            count = getRandomLong()
        )
        val status = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#FF0000",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val priority = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#00FF00",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val severity = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#0000FF",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val role = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#FFFF00",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val tag = TagDTO(
            name = getRandomString(),
            color = "#FF00FF",
            count = getRandomLong()
        )
        val type = FiltersDataResponseDTO.Filter(
            id = getRandomLong(),
            name = getRandomString(),
            color = "#00FFFF",
            count = getRandomLong(),
            order = getRandomLong()
        )
        val epic = FiltersDataResponseDTO.EpicsFilter(
            id = getRandomLong(),
            ref = 1L,
            subject = "Epic",
            count = getRandomLong()
        )

        val dto = FiltersDataResponseDTO(
            statuses = listOf(status),
            tags = listOf(tag),
            roles = listOf(role),
            assignedTo = listOf(assignee),
            owners = listOf(owner),
            epics = listOf(epic),
            priorities = listOf(priority),
            severities = listOf(severity),
            types = listOf(type)
        )

        val result = sut.toDomain(dto)

        assertEquals(1, result.assignees.size)
        assertEquals(1, result.createdBy.size)
        assertEquals(1, result.statuses.size)
        assertEquals(1, result.priorities.size)
        assertEquals(1, result.severities.size)
        assertEquals(1, result.roles.size)
        assertEquals(1, result.tags.size)
        assertEquals(1, result.types.size)
        assertEquals(1, result.epics.size)
    }

    private fun createMinimalDTO(
        statuses: List<FiltersDataResponseDTO.Filter> = emptyList(),
        tags: List<TagDTO>? = emptyList(),
        roles: List<FiltersDataResponseDTO.Filter>? = emptyList(),
        assignedTo: List<FiltersDataResponseDTO.UserFilter> = emptyList(),
        owners: List<FiltersDataResponseDTO.UserFilter> = emptyList(),
        epics: List<FiltersDataResponseDTO.EpicsFilter>? = emptyList(),
        priorities: List<FiltersDataResponseDTO.Filter>? = emptyList(),
        severities: List<FiltersDataResponseDTO.Filter>? = emptyList(),
        types: List<FiltersDataResponseDTO.Filter>? = emptyList()
    ) = FiltersDataResponseDTO(
        statuses = statuses,
        tags = tags,
        roles = roles,
        assignedTo = assignedTo,
        owners = owners,
        epics = epics,
        priorities = priorities,
        severities = severities,
        types = types
    )
}
