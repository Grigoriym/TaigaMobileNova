package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getWorkItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemEntityMapperTest {

    private lateinit var sut: WorkItemEntityMapper

    @Before
    fun setup() {
        sut = WorkItemEntityMapper(json = Json { ignoreUnknownKeys = true })
    }

    @Test
    fun `toDomain should map entity fields correctly`() {
        val entity = createEntity()

        val result = sut.toDomain(entity)

        assertEquals(entity.id, result.id)
        assertEquals(entity.taskType, result.taskType)
        assertEquals(entity.createdDate, result.createdDate)
        assertEquals(entity.ref, result.ref)
        assertEquals(entity.title, result.title)
        assertEquals(entity.isBlocked, result.isBlocked)
        assertEquals(entity.isClosed, result.isClosed)
        assertEquals(entity.blockedNote, result.blockedNote)
        assertEquals(entity.statusId, result.status.id)
        assertEquals(entity.statusName, result.status.name)
        assertEquals(entity.statusColor, result.status.color)
        assertEquals(entity.projectId, result.project.id)
        assertEquals(entity.projectName, result.project.name)
    }

    @Test
    fun `toDomain should map assignee when present`() {
        val entity = createEntity(
            assigneeId = 42L,
            assigneeName = "John Doe",
            assigneePhoto = "https://photo.url"
        )

        val result = sut.toDomain(entity)

        assertEquals(42L, result.assignee?.id)
        assertEquals("John Doe", result.assignee?.fullName)
        assertEquals("https://photo.url", result.assignee?.photo)
        assertNull(result.assignee?.bigPhoto)
        assertEquals("John Doe", result.assignee?.username)
    }

    @Test
    fun `toDomain should have null assignee when assigneeId is null`() {
        val entity = createEntity(assigneeId = null, assigneeName = null, assigneePhoto = null)

        val result = sut.toDomain(entity)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should have null assignee when assigneeName is null`() {
        val entity = createEntity(assigneeId = 42L, assigneeName = null, assigneePhoto = null)

        val result = sut.toDomain(entity)

        assertNull(result.assignee)
    }

    @Test
    fun `toDomain should parse tags from JSON`() {
        val tagsJson = """["bug|#FF0000","feature|#00FF00"]"""
        val entity = createEntity(tagsJson = tagsJson)

        val result = sut.toDomain(entity)

        assertEquals(2, result.tags.size)
        assertEquals("bug", result.tags[0].name)
        assertEquals("#FF0000", result.tags[0].color)
        assertEquals("feature", result.tags[1].name)
        assertEquals("#00FF00", result.tags[1].color)
    }

    @Test
    fun `toDomain should return empty tags for empty JSON`() {
        val entity = createEntity(tagsJson = "[]")

        val result = sut.toDomain(entity)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `toDomain should return empty tags for blank JSON`() {
        val entity = createEntity(tagsJson = "")

        val result = sut.toDomain(entity)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `toDomain should return empty tags for invalid JSON`() {
        val entity = createEntity(tagsJson = "not valid json")

        val result = sut.toDomain(entity)

        assertTrue(result.tags.isEmpty())
    }

    @Test
    fun `toDomain should parse colors from JSON`() {
        val colorsJson = """["#FF0000","#00FF00","#0000FF"]"""
        val entity = createEntity(colorsJson = colorsJson)

        val result = sut.toDomain(entity)

        assertEquals(3, result.colors.size)
        assertEquals("#FF0000", result.colors[0])
        assertEquals("#00FF00", result.colors[1])
        assertEquals("#0000FF", result.colors[2])
    }

    @Test
    fun `toDomain should return empty colors for empty JSON`() {
        val entity = createEntity(colorsJson = "[]")

        val result = sut.toDomain(entity)

        assertTrue(result.colors.isEmpty())
    }

    @Test
    fun `toDomain should return empty colors for invalid JSON`() {
        val entity = createEntity(colorsJson = "invalid")

        val result = sut.toDomain(entity)

        assertTrue(result.colors.isEmpty())
    }

    @Test
    fun `toEntity should map domain fields correctly`() {
        val workItem = getWorkItem()
        val sprintId = getRandomLong()

        val result = sut.toEntity(workItem, sprintId)

        assertEquals(workItem.id, result.id)
        assertEquals(workItem.taskType, result.taskType)
        assertEquals(workItem.project.id, result.projectId)
        assertEquals(workItem.project.name, result.projectName)
        assertEquals(workItem.ref, result.ref)
        assertEquals(workItem.title, result.title)
        assertEquals(workItem.createdDate, result.createdDate)
        assertEquals(workItem.isClosed, result.isClosed)
        assertEquals(workItem.isBlocked, result.isBlocked)
        assertEquals(workItem.blockedNote, result.blockedNote)
        assertEquals(workItem.status.id, result.statusId)
        assertEquals(workItem.status.name, result.statusName)
        assertEquals(workItem.status.color, result.statusColor)
        assertEquals(workItem.assignee?.actualId, result.assigneeId)
        assertEquals(workItem.assignee?.displayName, result.assigneeName)
        assertEquals(workItem.assignee?.avatarUrl, result.assigneePhoto)
        assertEquals(sprintId, result.sprintId)
    }

    @Test
    fun `toEntity should use null sprintId by default`() {
        val workItem = getWorkItem()

        val result = sut.toEntity(workItem)

        assertNull(result.sprintId)
    }

    @Test
    fun `toEntity should encode tags to JSON`() {
        val workItem = getWorkItem()

        val result = sut.toEntity(workItem)

        // Re-parse to verify round-trip
        val roundTripped = sut.toDomain(result)
        assertEquals(workItem.tags.size, roundTripped.tags.size)
        workItem.tags.forEachIndexed { index, tag ->
            assertEquals(tag.name, roundTripped.tags[index].name)
            assertEquals(tag.color, roundTripped.tags[index].color)
        }
    }

    @Test
    fun `toEntity should encode empty tags as empty array`() {
        val workItem = getWorkItem().copy(tags = persistentListOf())

        val result = sut.toEntity(workItem)

        assertEquals("[]", result.tagsJson)
    }

    @Test
    fun `toEntity should encode empty colors as empty array`() {
        val workItem = getWorkItem().copy(colors = persistentListOf())

        val result = sut.toEntity(workItem)

        assertEquals("[]", result.colorsJson)
    }

    @Test
    fun `toDomainList should map list of entities`() {
        val entities = listOf(createEntity(), createEntity())

        val result = sut.toDomainList(entities)

        assertEquals(2, result.size)
        assertEquals(entities[0].id, result[0].id)
        assertEquals(entities[1].id, result[1].id)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() {
        val result = sut.toDomainList(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toEntityList should map list of work items`() {
        val workItems = listOf(getWorkItem(), getWorkItem())
        val sprintId = getRandomLong()

        val result = sut.toEntityList(workItems, sprintId)

        assertEquals(2, result.size)
        assertEquals(workItems[0].id, result[0].id)
        assertEquals(workItems[1].id, result[1].id)
        assertEquals(sprintId, result[0].sprintId)
        assertEquals(sprintId, result[1].sprintId)
    }

    @Test
    fun `toEntityList should return empty list for empty input`() {
        val result = sut.toEntityList(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain should handle tag with pipe in name`() {
        val tagsJson = """["name|with|pipe|#FF0000"]"""
        val entity = createEntity(tagsJson = tagsJson)

        val result = sut.toDomain(entity)

        assertEquals(1, result.tags.size)
        assertEquals("name", result.tags[0].name)
        assertEquals("with|pipe|#FF0000", result.tags[0].color)
    }

    @Test
    fun `round trip entity to domain and back preserves data`() {
        val original = getWorkItem().copy(
            tags = persistentListOf(Tag(name = "urgent", color = "#FF0000")),
            colors = persistentListOf("#AABBCC")
        )
        val sprintId = getRandomLong()

        val entity = sut.toEntity(original, sprintId)
        val roundTripped = sut.toDomain(entity)

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.taskType, roundTripped.taskType)
        assertEquals(original.ref, roundTripped.ref)
        assertEquals(original.title, roundTripped.title)
        assertEquals(original.isBlocked, roundTripped.isBlocked)
        assertEquals(original.isClosed, roundTripped.isClosed)
        assertEquals(original.blockedNote, roundTripped.blockedNote)
        assertEquals(original.status, roundTripped.status)
        assertEquals(original.tags.size, roundTripped.tags.size)
        assertEquals(original.tags[0].name, roundTripped.tags[0].name)
        assertEquals(original.tags[0].color, roundTripped.tags[0].color)
        assertEquals(original.colors, roundTripped.colors)
        assertEquals(original.project.id, roundTripped.project.id)
        assertEquals(original.project.name, roundTripped.project.name)
    }

    private fun createEntity(
        assigneeId: Long? = getRandomLong(),
        assigneeName: String? = getRandomString(),
        assigneePhoto: String? = getRandomString(),
        tagsJson: String = "[]",
        colorsJson: String = "[]"
    ): WorkItemEntity = WorkItemEntity(
        id = getRandomLong(),
        taskType = CommonTaskType.UserStory,
        projectId = getRandomLong(),
        projectName = getRandomString(),
        ref = getRandomLong(),
        title = getRandomString(),
        createdDate = java.time.LocalDateTime.now(),
        isClosed = false,
        isBlocked = false,
        blockedNote = getRandomString(),
        statusId = getRandomLong(),
        statusName = getRandomString(),
        statusColor = "#FF5722",
        assigneeId = assigneeId,
        assigneeName = assigneeName,
        assigneePhoto = assigneePhoto,
        tagsJson = tagsJson,
        colorsJson = colorsJson,
        sprintId = null
    )
}
