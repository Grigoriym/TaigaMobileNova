package com.grappim.taigamobile.feature.issues.ui.model

import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.testing.models.getIssueTask
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IssueUIMapperTest {

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()

    private lateinit var sut: IssueUIMapper

    @BeforeTest
    fun setUp() {
        sut = IssueUIMapper(statusUIMapper = statusUIMapper, tagUIMapper = tagUIMapper)
    }

    @Test
    fun `toUI maps all fields correctly`() {
        val issue = getIssueTask()

        val actual = sut.toUI(issue)

        assertEquals(issue.id, actual.id)
        assertEquals(issue.version, actual.version)
        assertEquals(issue.createdDateTime, actual.createdDateTime)
        assertEquals(issue.title, actual.title)
        assertEquals(issue.ref, actual.ref)
        assertEquals(issue.isClosed, actual.isClosed)
        assertEquals(issue.blockedNote, actual.blockedNote)
        assertEquals(issue.description, actual.description)
        assertEquals(issue.copyLinkUrl, actual.copyLinkUrl)
        assertEquals(issue.creatorId, actual.creatorId)
        assertEquals(issue.assignedUserIds, actual.assignedUserIds)
        assertEquals(issue.watcherUserIds, actual.watcherUserIds)
        assertEquals(issue.promotedUserStories, actual.promotedUserStories)
        assertEquals(statusUIMapper.toUI(issue.status!!), actual.status)
        assertEquals(statusUIMapper.toUI(issue.type!!), actual.type)
        assertEquals(statusUIMapper.toUI(issue.priority!!), actual.priority)
        assertEquals(statusUIMapper.toUI(issue.severity!!), actual.severity)
        assertEquals(tagUIMapper.toSelectableUI(issue.tags), actual.tags)
    }

    @Test
    fun `toUI with null status, type, priority, severity maps to null`() {
        val issue = getIssueTask().copy(
            status = null,
            type = null,
            priority = null,
            severity = null
        )

        val actual = sut.toUI(issue)

        assertNull(actual.status)
        assertNull(actual.type)
        assertNull(actual.priority)
        assertNull(actual.severity)
    }

    @Test
    fun `toUI with empty tags maps to empty list`() {
        val issue = getIssueTask().copy(tags = persistentListOf())

        val actual = sut.toUI(issue)

        assertEquals(0, actual.tags.size)
    }
}
