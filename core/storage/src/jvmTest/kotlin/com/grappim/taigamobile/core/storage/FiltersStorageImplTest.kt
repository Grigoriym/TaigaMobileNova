package com.grappim.taigamobile.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.grappim.kit.testing.MainDispatcherRule
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [FiltersStorageImpl]'s `change*` methods are fire-and-forget `launch`es on a scope built from
 * `Dispatchers.Main`, so [MainDispatcherRule] is what makes the writes observable at all here.
 *
 * The test [dataStore] is built with that same `Dispatchers.Main` scope (see [createTestDataStore]),
 * not the real `Dispatchers.IO` scope it would otherwise default to. Without this, the DataStore's
 * internal actor runs on the real IO thread pool, decoupled from [mainDispatcherRule]'s dispatcher,
 * and `awaitItem()` below has to real-wall-clock-wait for it — which was flaky under a full
 * multi-module `jvmTest` run (docs/revisit.md #25). Sharing the dispatcher keeps the whole
 * write -> DataStore actor -> flow emission chain on one deterministic, eagerly-executing test
 * dispatcher instead.
 *
 * The filters written by these tests must differ from [FiltersData]'s own default, which is why
 * they are built locally instead of with `:testing`'s `getFiltersData()`: that factory returns a
 * value *equal to* `FiltersData()`, and each flow drops an update equal to the value it last
 * emitted, so the write would be invisible however correct the code is.
 */
class FiltersStorageImplTest {

    private val mainDispatcherRule = MainDispatcherRule()
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var dataStore: DataStore<Preferences>

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        dataStore = createTestDataStore(
            "filters_storage_test",
            scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        )
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createSut() = FiltersStorageImpl(dataStore = dataStore, json = json)

    private fun filtersNamed(name: String) = FiltersData(
        statuses = persistentListOf(StatusFilters(id = 1L, name = name, count = 3L, color = "#FF0000"))
    )

    @Test
    fun `every section starts as an empty FiltersData when nothing was stored`() = runTest {
        val sut = createSut()

        sut.scrumFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.epicsFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.issuesFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.kanbanFilters.test { assertEquals(FiltersData(), awaitItem()) }
    }

    @Test
    fun `a blank stored value decodes to an empty FiltersData`() = runTest {
        dataStore.edit { it[stringPreferencesKey("filters_scrum")] = "   " }

        val sut = createSut()

        sut.scrumFilters.test { assertEquals(FiltersData(), awaitItem()) }
    }

    @Test
    fun `a stored value is decoded back into the filters it was saved from`() = runTest {
        val filters = filtersNamed("stored")
        dataStore.edit { it[stringPreferencesKey("filters_scrum")] = json.encodeToString(filters) }

        val sut = createSut()

        sut.scrumFilters.test { assertEquals(filters, awaitItem()) }
    }

    @Test
    fun `changeScrumFilters persists the filters and the flow emits them`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("scrum")

        sut.scrumFilters.test {
            assertEquals(FiltersData(), awaitItem())

            sut.changeScrumFilters(filters)

            assertEquals(filters, awaitItem())
        }
    }

    @Test
    fun `changeEpicsFilters persists the filters and the flow emits them`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("epics")

        sut.epicsFilters.test {
            assertEquals(FiltersData(), awaitItem())

            sut.changeEpicsFilters(filters)

            assertEquals(filters, awaitItem())
        }
    }

    @Test
    fun `changeIssuesFilters persists the filters and the flow emits them`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("issues")

        sut.issuesFilters.test {
            assertEquals(FiltersData(), awaitItem())

            sut.changeIssuesFilters(filters)

            assertEquals(filters, awaitItem())
        }
    }

    @Test
    fun `changeKanbanFilters persists the filters and the flow emits them`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("kanban")

        sut.kanbanFilters.test {
            assertEquals(FiltersData(), awaitItem())

            sut.changeKanbanFilters(filters)

            assertEquals(filters, awaitItem())
        }
    }

    @Test
    fun `each section is stored under its own key`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("scrum only")

        sut.scrumFilters.test {
            assertEquals(FiltersData(), awaitItem())

            sut.changeScrumFilters(filters)

            assertEquals(filters, awaitItem())
        }

        assertEquals(FiltersData(), sut.epicsFilters.first())
        assertEquals(FiltersData(), sut.issuesFilters.first())
        assertEquals(FiltersData(), sut.kanbanFilters.first())
    }

    @Test
    fun `resetFilters clears every section`() = runTest {
        val sut = createSut()
        val filters = filtersNamed("everything")

        sut.scrumFilters.test {
            assertEquals(FiltersData(), awaitItem())
            sut.changeScrumFilters(filters)
            assertEquals(filters, awaitItem())
        }
        sut.epicsFilters.test {
            assertEquals(FiltersData(), awaitItem())
            sut.changeEpicsFilters(filters)
            assertEquals(filters, awaitItem())
        }
        sut.issuesFilters.test {
            assertEquals(FiltersData(), awaitItem())
            sut.changeIssuesFilters(filters)
            assertEquals(filters, awaitItem())
        }
        sut.kanbanFilters.test {
            assertEquals(FiltersData(), awaitItem())
            sut.changeKanbanFilters(filters)
            assertEquals(filters, awaitItem())
        }

        sut.resetFilters()

        assertEquals(emptyMap(), dataStore.data.first().asMap())
        sut.scrumFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.epicsFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.issuesFilters.test { assertEquals(FiltersData(), awaitItem()) }
        sut.kanbanFilters.test { assertEquals(FiltersData(), awaitItem()) }
    }

    @Test
    fun `a filters flow emits nothing until the stored value has been read`() = runTest {
        val stored = filtersNamed("stored")
        dataStore.edit { it[stringPreferencesKey("filters_issues")] = json.encodeToString(stored) }
        val firstRead = CompletableDeferred<Unit>()
        val sut = FiltersStorageImpl(dataStore = HeldBackDataStore(dataStore, firstRead), json = json)

        sut.issuesFilters.test {
            expectNoEvents()

            firstRead.complete(Unit)

            assertEquals(stored, awaitItem())
        }
    }

    @Test
    fun `refreshIssuesFilters refreshes the stored selection from the server data`() = runTest {
        dataStore.edit { it[stringPreferencesKey("filters_issues")] = json.encodeToString(storedSelection) }

        createSut().refreshIssuesFilters(serverData)

        assertEquals(refreshedSelection, createSut().issuesFilters.first())
    }

    @Test
    fun `refreshEpicsFilters refreshes the stored selection from the server data`() = runTest {
        dataStore.edit { it[stringPreferencesKey("filters_epics")] = json.encodeToString(storedSelection) }

        createSut().refreshEpicsFilters(serverData)

        assertEquals(refreshedSelection, createSut().epicsFilters.first())
    }

    @Test
    fun `refreshScrumFilters refreshes the stored selection from the server data`() = runTest {
        dataStore.edit { it[stringPreferencesKey("filters_scrum")] = json.encodeToString(storedSelection) }

        createSut().refreshScrumFilters(serverData)

        assertEquals(refreshedSelection, createSut().scrumFilters.first())
    }

    private val storedSelection = FiltersData(
        statuses = persistentListOf(StatusFilters(id = 1L, name = "old", count = 3L, color = "#FF0000"))
    )
    private val serverData = FiltersData(
        statuses = persistentListOf(
            StatusFilters(id = 1L, name = "new", count = 5L, color = "#00FF00"),
            StatusFilters(id = 2L, name = "not selected", count = 1L, color = "#0000FF")
        )
    )
    private val refreshedSelection = FiltersData(
        statuses = persistentListOf(StatusFilters(id = 1L, name = "new", count = 5L, color = "#00FF00"))
    )

    /** Holds back the first read of [delegate] until [firstRead] completes; writes pass straight through. */
    private class HeldBackDataStore(
        private val delegate: DataStore<Preferences>,
        private val firstRead: CompletableDeferred<Unit>
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            firstRead.await()
            emitAll(delegate.data)
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
            delegate.updateData(transform)
    }
}
