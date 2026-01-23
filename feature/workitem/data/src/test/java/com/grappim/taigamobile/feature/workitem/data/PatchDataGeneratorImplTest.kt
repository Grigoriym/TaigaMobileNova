package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PatchDataGeneratorImplTest {

    private lateinit var sut: PatchDataGeneratorImpl

    @Before
    fun setup() {
        sut = PatchDataGeneratorImpl()
    }

    @Test
    fun `getTitle should return map with subject key`() {
        val title = getRandomString()

        val result = sut.getTitle(title)

        assertEquals(1, result.size)
        assertEquals(title, result["subject"])
    }

    @Test
    fun `getWatchersPatchPayload should return map with watchers key`() {
        val watchers = listOf(getRandomLong(), getRandomLong(), getRandomLong())

        val result = sut.getWatchersPatchPayload(watchers)

        assertEquals(1, result.size)
        assertEquals(watchers, result["watchers"])
    }

    @Test
    fun `getWatchersPatchPayload should handle empty list`() {
        val watchers = emptyList<Long>()

        val result = sut.getWatchersPatchPayload(watchers)

        assertEquals(1, result.size)
        assertEquals(watchers, result["watchers"])
    }

    @Test
    fun `getAssignedToPatchPayload should return map with assigned_to key`() {
        val assignee = getRandomLong()

        val result = sut.getAssignedToPatchPayload(assignee)

        assertEquals(1, result.size)
        assertEquals(assignee, result["assigned_to"])
    }

    @Test
    fun `getAssignedToPatchPayload should handle null assignee`() {
        val result = sut.getAssignedToPatchPayload(null)

        assertEquals(1, result.size)
        assertEquals(null, result["assigned_to"])
    }

    @Test
    fun `getBlockedPatchPayload should return map with is_blocked and blocked_note keys`() {
        val isBlocked = true
        val blockNote = getRandomString()

        val result = sut.getBlockedPatchPayload(isBlocked, blockNote)

        assertEquals(2, result.size)
        assertEquals(isBlocked, result["is_blocked"])
        assertEquals(blockNote, result["blocked_note"])
    }

    @Test
    fun `getBlockedPatchPayload should return empty string for null blockNote`() {
        val isBlocked = false

        val result = sut.getBlockedPatchPayload(isBlocked, null)

        assertEquals(2, result.size)
        assertEquals(isBlocked, result["is_blocked"])
        assertEquals("", result["blocked_note"])
    }

    @Test
    fun `getAssignedUsersPatchPayload should return map with assigned_users key`() {
        val assignees = persistentListOf(getRandomLong(), getRandomLong(), getRandomLong())

        val result = sut.getAssignedUsersPatchPayload(assignees)

        assertEquals(1, result.size)
        assertEquals(assignees, result["assigned_users"])
    }

    @Test
    fun `getAssignedUsersPatchPayload should handle empty list`() {
        val assignees = persistentListOf<Long>()

        val result = sut.getAssignedUsersPatchPayload(assignees)

        assertEquals(1, result.size)
        assertEquals(assignees, result["assigned_users"])
    }

    @Test
    fun `getDueDatePatchPayload should return map with due_date key`() {
        val dueDate = getRandomString()

        val result = sut.getDueDatePatchPayload(dueDate)

        assertEquals(1, result.size)
        assertEquals(dueDate, result["due_date"])
    }

    @Test
    fun `getDueDatePatchPayload should handle null due date`() {
        val result = sut.getDueDatePatchPayload(null)

        assertEquals(1, result.size)
        assertEquals(null, result["due_date"])
    }

    @Test
    fun `getTagsPatchPayload should return map with tags key`() {
        val tags = listOf(
            listOf(getRandomString(), getRandomString()),
            listOf(getRandomString(), getRandomString())
        )

        val result = sut.getTagsPatchPayload(tags)

        assertEquals(1, result.size)
        assertEquals(tags, result["tags"])
    }

    @Test
    fun `getTagsPatchPayload should handle empty list`() {
        val tags = emptyList<List<String>>()

        val result = sut.getTagsPatchPayload(tags)

        assertEquals(1, result.size)
        assertEquals(tags, result["tags"])
    }

    @Test
    fun `getDescriptionPatchPayload should return map with description key`() {
        val description = getRandomString()

        val result = sut.getDescriptionPatchPayload(description)

        assertEquals(1, result.size)
        assertEquals(description, result["description"])
    }

    @Test
    fun `getDescriptionPatchPayload should handle empty description`() {
        val description = ""

        val result = sut.getDescriptionPatchPayload(description)

        assertEquals(1, result.size)
        assertEquals(description, result["description"])
    }

    @Test
    fun `getAttributesPatchPayload should return map with attributes_values key`() {
        val attributes = mapOf<String, Any?>(getRandomString() to getRandomString(), getRandomString() to 42)

        val result = sut.getAttributesPatchPayload(attributes)

        assertEquals(1, result.size)
        assertEquals(attributes, result["attributes_values"])
    }

    @Test
    fun `getAttributesPatchPayload should handle empty attributes`() {
        val attributes = emptyMap<String, Any?>()

        val result = sut.getAttributesPatchPayload(attributes)

        assertEquals(1, result.size)
        assertEquals(attributes, result["attributes_values"])
    }

    @Test
    fun `getStatus should return map with status key`() {
        val id = getRandomLong()

        val result = sut.getStatus(id)

        assertEquals(1, result.size)
        assertEquals(id, result["status"])
    }

    @Test
    fun `getType should return map with type key`() {
        val id = getRandomLong()

        val result = sut.getType(id)

        assertEquals(1, result.size)
        assertEquals(id, result["type"])
    }

    @Test
    fun `getSeverity should return map with severity key`() {
        val id = getRandomLong()

        val result = sut.getSeverity(id)

        assertEquals(1, result.size)
        assertEquals(id, result["severity"])
    }

    @Test
    fun `getPriority should return map with priority key`() {
        val id = getRandomLong()

        val result = sut.getPriority(id)

        assertEquals(1, result.size)
        assertEquals(id, result["priority"])
    }

    @Test
    fun `getColor should return map with color key`() {
        val color = getRandomString()

        val result = sut.getColor(color)

        assertEquals(1, result.size)
        assertEquals(color, result["color"])
    }

    @Test
    fun `getSprint should return map with milestone key`() {
        val sprintId = getRandomLong()

        val result = sut.getSprint(sprintId)

        assertEquals(1, result.size)
        assertEquals(sprintId, result["milestone"])
    }

    @Test
    fun `getSprint should handle null sprint id`() {
        val result = sut.getSprint(null)

        assertEquals(1, result.size)
        assertEquals(null, result["milestone"])
    }

    @Test
    fun `getWikiContent should return map with content key`() {
        val content = getRandomString()

        val result = sut.getWikiContent(content)

        assertEquals(1, result.size)
        assertEquals(content, result["content"])
    }

    @Test
    fun `getWikiContent should handle empty content`() {
        val content = ""

        val result = sut.getWikiContent(content)

        assertEquals(1, result.size)
        assertEquals(content, result["content"])
    }
}
