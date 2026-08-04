package com.grappim.taigamobile.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.testing.MainDispatcherRule
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [FiltersStorageImpl] publishes its four sections as `StateFlow`s eagerly shared on a scope built
 * from `Dispatchers.Main`, and its `change*` methods are fire-and-forget `launch`es on that same
 * scope — so [MainDispatcherRule] is what makes the writes observable at all here.
 *
 * The filters written by these tests must differ from [FiltersData]'s own default, which is why
 * they are built locally instead of with `:testing`'s `getFiltersData()`: that factory returns a
 * value *equal to* `FiltersData()`, and a `StateFlow` conflates an update it compares equal to the
 * value it is already holding, so the write would be invisible however correct the code is.
 */
class FiltersStorageImplTest {

    private val mainDispatcherRule = MainDispatcherRule()
    private val json = Json { ignoreUnknownKeys = true }

    private lateinit var dataStore: DataStore<Preferences>

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        dataStore = createTestDataStore("filters_storage_test")
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

        assertEquals(FiltersData(), sut.epicsFilters.value)
        assertEquals(FiltersData(), sut.issuesFilters.value)
        assertEquals(FiltersData(), sut.kanbanFilters.value)
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
}
