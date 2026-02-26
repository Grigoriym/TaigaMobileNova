package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.testing.models.getStatusUI
import com.grappim.taigamobile.testing.models.getTagUI
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorkItemUIMapperTest {

    private val statusUIMapper: StatusUIMapper = StatusUIMapper()
    private val tagUIMapper: TagUIMapper = TagUIMapper()
    private val dateTimeUtils = FakeDateTimeUtils()

    private lateinit var sut: WorkItemUIMapper

    @BeforeTest
    fun setUp() {
        sut = WorkItemUIMapper(
            statusUIMapper = statusUIMapper,
            tagUIMapper = tagUIMapper,
            dateTimeUtils = dateTimeUtils
        )
    }

    @Test
    fun `toUI with WorkItem should return WorkItemUI correctly`() = runTest {
        val workItem = getWorkItem()
        val statusUI = getStatusUI()
        val tagsUI = persistentListOf(getTagUI(), getTagUI())
        val formattedDate = "Dec 15, 2024"

        val actual = sut.toUI(workItem)

        assertEquals(workItem.id, actual.id)
        assertEquals(workItem.taskType, actual.taskType)
        assertEquals(formattedDate, actual.createdDate)
        assertEquals(statusUI, actual.status)
        assertEquals(workItem.ref, actual.ref)
        assertEquals(workItem.title, actual.title)
        assertEquals(workItem.isBlocked, actual.isBlocked)
        assertEquals(tagsUI, actual.tags)
        assertEquals(workItem.isClosed, actual.isClosed)
        assertEquals(workItem.colors, actual.colors)
        assertEquals(workItem.assignee, actual.assignee)
    }

    @Test
    fun `toUI with list of WorkItems should return list of WorkItemUI correctly`() = runTest {
        val workItem1 = getWorkItem()
        val workItem2 = getWorkItem()
        val list = persistentListOf(workItem1, workItem2)

        val statusUI1 = getStatusUI()
        val statusUI2 = getStatusUI()
        val tagsUI1 = persistentListOf(getTagUI())
        val tagsUI2 = persistentListOf(getTagUI())
        val formattedDate1 = "Dec 15, 2024"
        val formattedDate2 = "Dec 16, 2024"

        val actual = sut.toUI(list)

        assertEquals(2, actual.size)
        assertEquals(workItem1.id, actual[0].id)
        assertEquals(statusUI1, actual[0].status)
        assertEquals(tagsUI1, actual[0].tags)
        assertEquals(formattedDate1, actual[0].createdDate)
        assertEquals(workItem2.id, actual[1].id)
        assertEquals(statusUI2, actual[1].status)
        assertEquals(tagsUI2, actual[1].tags)
        assertEquals(formattedDate2, actual[1].createdDate)
    }

    @Test
    fun `toUI with empty list should return empty list`() = runTest {
        val actual = sut.toUI(persistentListOf())

        assertTrue(actual.isEmpty())
    }
}
