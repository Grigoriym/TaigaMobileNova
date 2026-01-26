package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.testing.getStatusUI
import com.grappim.taigamobile.testing.getTagUI
import com.grappim.taigamobile.testing.getWorkItem
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkItemUIMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val statusUIMapper: StatusUIMapper = mockk()
    private val tagUIMapper: TagUIMapper = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()

    private lateinit var sut: WorkItemUIMapper

    @Before
    fun setUp() {
        sut = WorkItemUIMapper(
            dispatcher = testDispatcher,
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

        coEvery { statusUIMapper.toUI(workItem.status) } returns statusUI
        coEvery { tagUIMapper.toSelectableUI(workItem.tags) } returns tagsUI
        coEvery { dateTimeUtils.formatToMediumFormat(workItem.createdDate) } returns formattedDate

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

        coEvery { statusUIMapper.toUI(workItem1.status) } returns statusUI1
        coEvery { statusUIMapper.toUI(workItem2.status) } returns statusUI2
        coEvery { tagUIMapper.toSelectableUI(workItem1.tags) } returns tagsUI1
        coEvery { tagUIMapper.toSelectableUI(workItem2.tags) } returns tagsUI2
        coEvery { dateTimeUtils.formatToMediumFormat(workItem1.createdDate) } returns formattedDate1
        coEvery { dateTimeUtils.formatToMediumFormat(workItem2.createdDate) } returns formattedDate2

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
