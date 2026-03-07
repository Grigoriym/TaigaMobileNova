package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import kotlinx.collections.immutable.persistentListOf
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
    fun `toUI with WorkItem should return WorkItemUI correctly`() {
        val workItem = getWorkItem()
        val formattedDate = dateTimeUtils.formatToMediumFormat(workItem.createdDate)

        val actual = sut.toUI(workItem)

        assertEquals(workItem.id, actual.id)
        assertEquals(workItem.taskType, actual.taskType)
        assertEquals(formattedDate, actual.createdDate)
        assertEquals(workItem.ref, actual.ref)
        assertEquals(workItem.title, actual.title)
        assertEquals(workItem.isBlocked, actual.isBlocked)
        assertEquals(workItem.isClosed, actual.isClosed)
        assertEquals(workItem.colors, actual.colors)
        assertEquals(workItem.assignee, actual.assignee)
    }

    @Test
    fun `toUI with list of WorkItems should return list of WorkItemUI correctly`() {
        val workItem1 = getWorkItem()
        val workItem2 = getWorkItem()
        val list = persistentListOf(workItem1, workItem2)

        val formattedDate1 = dateTimeUtils.formatToMediumFormat(workItem1.createdDate)
        val formattedDate2 = dateTimeUtils.formatToMediumFormat(workItem2.createdDate)

        val actual = sut.toUI(list)

        assertEquals(2, actual.size)
        assertEquals(workItem1.id, actual[0].id)
        assertEquals(formattedDate1, actual[0].createdDate)
        assertEquals(workItem2.id, actual[1].id)
        assertEquals(formattedDate2, actual[1].createdDate)
    }

    @Test
    fun `toUI with empty list should return empty list`() {
        val actual = sut.toUI(persistentListOf())

        assertTrue(actual.isEmpty())
    }
}
