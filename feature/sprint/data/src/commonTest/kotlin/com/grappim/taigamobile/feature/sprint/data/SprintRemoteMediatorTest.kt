package com.grappim.taigamobile.feature.sprint.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.grappim.taigamobile.core.storage.db.entities.SprintEntity
import com.grappim.taigamobile.testing.api.FakeSprintsApi
import com.grappim.taigamobile.testing.api.jsonHttpResponse
import com.grappim.taigamobile.testing.dao.FakeSprintDao
import com.grappim.taigamobile.testing.models.getSprintEntity
import com.grappim.taigamobile.testing.models.getSprintResponseDTO
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
 * `SprintRemoteMediator.load` is one big `try` around a `when (loadType)`, so every assertion here
 * goes through `load()` — there is nothing else public on the class.
 *
 * The mediator calls both `body()` and `hasNextPage()` on the raw `HttpResponse` returned by
 * `SprintApi.getSprintsPaging`, so `FakeSprintsApi` is fed a genuine response built by
 * `jsonHttpResponse` rather than a stub.
 */
@OptIn(ExperimentalPagingApi::class)
class SprintRemoteMediatorTest {

    private val sprintApi = FakeSprintsApi()
    private val sprintDao = FakeSprintDao()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val sprintMapper = SprintMapper()

    private val projectId = getRandomLong()

    private lateinit var sut: SprintRemoteMediator

    @BeforeTest
    fun setup() {
        taigaSessionStorage.currentProjectId = projectId
        sut = createMediator(isClosed = false)
    }

    @Test
    fun `on load with REFRESH should request the first page and clear the cached sprints`() = runTest {
        val dto = getSprintResponseDTO()
        sprintApi.sprintsPagingResponse = responseOf(dto)

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(projectId, sprintApi.lastPagingProject)
        assertEquals(1, sprintApi.lastPagingPage)
        assertEquals(false, sprintApi.lastPagingIsClosed)
        assertEquals(projectId to false, sprintDao.deletedByProjectIdAndClosed)
    }

    @Test
    fun `on load with REFRESH should map the response into entities of the current project`() = runTest {
        val dto = getSprintResponseDTO()
        sprintApi.sprintsPagingResponse = responseOf(dto)

        sut.load(LoadType.REFRESH, emptyState())

        val inserted = sprintDao.insertedAll?.single()
        assertEquals(dto.id, inserted?.id)
        assertEquals(projectId, inserted?.projectId)
        assertEquals(dto.name, inserted?.name)
        assertEquals(dto.order, inserted?.order)
        assertEquals(dto.estimatedStart, inserted?.start)
        assertEquals(dto.estimatedFinish, inserted?.end)
        assertEquals(dto.userStories.size, inserted?.storiesCount)
        assertEquals(dto.closed, inserted?.isClosed)
    }

    @Test
    fun `on load without the pagination header should report the end of pagination`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO(), hasNextPage = false)

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertTrue(assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached)
    }

    @Test
    fun `on load with the pagination header should not report the end of pagination`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO(), hasNextPage = true)

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(
            false,
            assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached
        )
    }

    @Test
    fun `on load with PREPEND should finish immediately without touching the api`() = runTest {
        val result = sut.load(LoadType.PREPEND, stateOf(pageOf(getSprintEntity())))

        assertTrue(assertIs<RemoteMediator.MediatorResult.Success>(result).endOfPaginationReached)
        assertNull(sprintApi.lastPagingPage)
        assertNull(sprintDao.insertedAll)
        assertNull(sprintDao.deletedByProjectIdAndClosed)
    }

    @Test
    fun `on load with APPEND and no loaded items should request the first page`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO())

        val result = sut.load(LoadType.APPEND, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(1, sprintApi.lastPagingPage)
    }

    @Test
    fun `on load with APPEND should derive the page from the loaded item count`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO())
        val loaded = List(PAGE_SIZE * 2 + 5) { getSprintEntity() }

        val result = sut.load(LoadType.APPEND, stateOf(pageOf(*loaded.toTypedArray())))

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(3, sprintApi.lastPagingPage)
    }

    @Test
    fun `on load with APPEND should keep the cached sprints`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO())

        sut.load(LoadType.APPEND, stateOf(pageOf(getSprintEntity())))

        assertNull(sprintDao.deletedByProjectIdAndClosed)
    }

    @Test
    fun `on load with a null response body should insert nothing`() = runTest {
        sprintApi.sprintsPagingResponse = jsonHttpResponse("null")

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertIs<RemoteMediator.MediatorResult.Success>(result)
        assertEquals(emptyList(), sprintDao.insertedAll)
    }

    @Test
    fun `on load for a closed sprint mediator should pass the flag through to the api and the dao`() = runTest {
        sut = createMediator(isClosed = true)
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO())

        sut.load(LoadType.REFRESH, emptyState())

        assertEquals(true, sprintApi.lastPagingIsClosed)
        assertEquals(projectId to true, sprintDao.deletedByProjectIdAndClosed)
    }

    @Test
    fun `on load when the api throws should return an error result`() = runTest {
        sprintApi.getSprintsPagingThrows = testException

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(testException, assertIs<RemoteMediator.MediatorResult.Error>(result).throwable)
    }

    @Test
    fun `on load when the dao throws should return an error result`() = runTest {
        sprintApi.sprintsPagingResponse = responseOf(getSprintResponseDTO())
        sprintDao.insertAllThrows = testException

        val result = sut.load(LoadType.REFRESH, emptyState())

        assertEquals(testException, assertIs<RemoteMediator.MediatorResult.Error>(result).throwable)
    }

    private fun createMediator(isClosed: Boolean) = SprintRemoteMediator(
        isClosed = isClosed,
        sprintApi = sprintApi,
        sprintDao = sprintDao,
        sprintMapper = sprintMapper,
        taigaSessionStorage = taigaSessionStorage
    )

    private suspend fun responseOf(vararg dto: SprintResponseDTO, hasNextPage: Boolean = false) =
        jsonHttpResponse(Json.encodeToString(dto.toList()), hasNextPage = hasNextPage)

    private fun pageOf(vararg entities: SprintEntity) =
        PagingSource.LoadResult.Page<Int, SprintEntity>(entities.toList(), prevKey = null, nextKey = null)

    private fun emptyState() = stateOf()

    private fun stateOf(vararg pages: PagingSource.LoadResult.Page<Int, SprintEntity>) = PagingState(
        pages = pages.toList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = PAGE_SIZE),
        leadingPlaceholderCount = 0
    )

    private companion object {
        /** Mirrors the private `PAGE_SIZE` in `SprintRemoteMediator.kt`. */
        const val PAGE_SIZE = 10
    }
}
