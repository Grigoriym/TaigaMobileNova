package com.grappim.taigamobile.feature.workitem.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.api.jsonHttpResponse
import com.grappim.taigamobile.testing.dao.FakeWorkItemDao
import com.grappim.taigamobile.testing.models.getWorkItemEntity
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `WorkItemRemoteMediator.load` is the whole public surface of the class, so every assertion here
 * goes through it.
 *
 * The mediator calls both `body()` and `hasNextPage()` on the raw `HttpResponse` returned by
 * `WorkItemApi.getWorkItemsPagination`, so `FakeWorkItemApi` is fed a genuine response built by
 * `jsonHttpResponse` rather than a stub.
 */
@OptIn(ExperimentalPagingApi::class)
class WorkItemRemoteMediatorTest {

    private val workItemApi = FakeWorkItemApi()
    private val workItemDao = FakeWorkItemDao()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )
    private val workItemEntityMapper = WorkItemEntityMapper(Json)

    private val projectId = getRandomLong()

    private lateinit var sut: WorkItemRemoteMediator

    @BeforeTest
    fun setup() {
        taigaSessionStorage.currentProjectId = projectId
        sut = createMediator(CommonTaskType.Task)
    }

    @Test
    fun `on load with REFRESH should request the first page and clear the cached work items`() = runTest {
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals("tasks", workItemApi.lastPaginationTaskPath)
        assertEquals(projectId, workItemApi.lastPaginationProject)
        assertEquals(1, workItemApi.lastPaginationPage)
        assertEquals(PAGE_SIZE, workItemApi.lastPaginationPageSize)
        assertEquals(projectId to CommonTaskType.Task, workItemDao.deletedByProjectIdAndType)
    }

    @Test
    fun `on load with REFRESH should map the response into entities of the requested type`() = runTest {
        val dto = getWorkItemResponseDTO()
        workItemApi.workItemsPaginationResponse = responseOf(dto)

        sut.load(LoadType.REFRESH, emptyState())

        val inserted = workItemDao.insertAllCalls.single().single()
        assertEquals(dto.id, inserted.id)
        assertEquals(CommonTaskType.Task, inserted.taskType)
        assertEquals(dto.subject, inserted.title)
        assertEquals(dto.ref, inserted.ref)
        assertEquals(dto.statusExtraInfo?.name, inserted.statusName)
        assertNull(inserted.sprintId)
    }

    @Test
    fun `on load without the pagination header should report the end of pagination`() = runTest {
        workItemApi.workItemsPaginationResponse =
            responseOf(getWorkItemResponseDTO(), hasNextPage = false)

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertTrue(assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached)
    }

    @Test
    fun `on load with the pagination header should not report the end of pagination`() = runTest {
        workItemApi.workItemsPaginationResponse =
            responseOf(getWorkItemResponseDTO(), hasNextPage = true)

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(
            false,
            assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached
        )
    }

    @Test
    fun `on load with PREPEND should finish immediately without touching the api`() = runTest {
        val result = sut.load(LoadType.PREPEND, stateOf(pageOf(getWorkItemEntity())))

        assertTrue(assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached)
        assertNull(workItemApi.lastPaginationPage)
        assertTrue(workItemDao.insertAllCalls.isEmpty())
        assertNull(workItemDao.deletedByProjectIdAndType)
    }

    @Test
    fun `on load with APPEND and no loaded items should request the first page`() = runTest {
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())

        val result = sut.load(LoadType.APPEND, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(1, workItemApi.lastPaginationPage)
    }

    @Test
    fun `on load with APPEND should derive the page from the loaded item count`() = runTest {
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())
        val loaded = List(PAGE_SIZE * 2 + 5) { getWorkItemEntity() }

        val result = sut.load(LoadType.APPEND, stateOf(pageOf(*loaded.toTypedArray())))

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(3, workItemApi.lastPaginationPage)
    }

    @Test
    fun `on load with APPEND should keep the cached work items`() = runTest {
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())

        sut.load(LoadType.APPEND, stateOf(pageOf(getWorkItemEntity())))

        assertNull(workItemDao.deletedByProjectIdAndType)
    }

    @Test
    fun `on load for user stories should ask the api for items outside of any sprint`() = runTest {
        sut = createMediator(CommonTaskType.UserStory)
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())

        sut.load(LoadType.REFRESH, emptyState())

        assertEquals("userstories", workItemApi.lastPaginationTaskPath)
        assertEquals("null", workItemApi.lastPaginationSprint)
    }

    @Test
    fun `on load for a type other than user story should not filter by sprint`() = runTest {
        sut = createMediator(CommonTaskType.Issue)
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())

        sut.load(LoadType.REFRESH, emptyState())

        assertEquals("issues", workItemApi.lastPaginationTaskPath)
        assertNull(workItemApi.lastPaginationSprint)
    }

    @Test
    fun `on load with a null response body should insert nothing`() = runTest {
        workItemApi.workItemsPaginationResponse = jsonHttpResponse("null")

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(emptyList(), workItemDao.insertAllCalls.single())
    }

    @Test
    fun `on load when the api throws should return an error result`() = runTest {
        workItemApi.getWorkItemsPaginationThrows = testException

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(testException, assertIs<RemoteMediator.MediatorResult.Error>(result).throwable)
    }

    @Test
    fun `on load when the dao throws should return an error result`() = runTest {
        workItemApi.workItemsPaginationResponse = responseOf(getWorkItemResponseDTO())
        workItemDao.insertAllThrows = testException

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(testException, assertIs<RemoteMediator.MediatorResult.Error>(result).throwable)
    }

    private fun createMediator(taskType: CommonTaskType) = WorkItemRemoteMediator(
        taskType = taskType,
        workItemApi = workItemApi,
        workItemDao = workItemDao,
        workItemMapper = workItemMapper,
        workItemEntityMapper = workItemEntityMapper,
        taigaSessionStorage = taigaSessionStorage
    )

    private suspend fun responseOf(vararg dto: WorkItemResponseDTO, hasNextPage: Boolean = false) =
        jsonHttpResponse(Json.encodeToString(dto.toList()), hasNextPage = hasNextPage)

    private fun pageOf(vararg entities: WorkItemEntity) =
        PagingSource.LoadResult.Page<Int, WorkItemEntity>(entities.toList(), prevKey = null, nextKey = null)

    private fun emptyState() = stateOf()

    private fun stateOf(vararg pages: PagingSource.LoadResult.Page<Int, WorkItemEntity>) = PagingState(
        pages = pages.toList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = PAGE_SIZE),
        leadingPlaceholderCount = 0
    )

    private companion object {
        /** Mirrors the private `PAGE_SIZE` in `WorkItemRemoteMediator.kt`. */
        const val PAGE_SIZE = 10
    }
}
