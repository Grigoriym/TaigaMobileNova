package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.FiltersStorage
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

class FakeFiltersStorage : FiltersStorage {

    var resetFiltersCalled = false

    /**
     * While false, the filter flows emit nothing, like a real DataStore whose first read has not
     * finished. The stored values can still be written and read through the `current*` accessors.
     */
    val storedFiltersLoaded = MutableStateFlow(true)

    private val _scrumFilters = MutableStateFlow(FiltersData())
    val currentScrumFilters: FiltersData get() = _scrumFilters.value
    override val scrumFilters: Flow<FiltersData> = _scrumFilters.onceLoaded()
    override fun changeScrumFilters(filters: FiltersData) { _scrumFilters.value = filters }
    override suspend fun refreshScrumFilters(serverData: FiltersData) {
        _scrumFilters.update { it.updateData(serverData) }
    }

    private val _epicsFilters = MutableStateFlow(FiltersData())
    val currentEpicsFilters: FiltersData get() = _epicsFilters.value
    override val epicsFilters: Flow<FiltersData> = _epicsFilters.onceLoaded()
    override fun changeEpicsFilters(filters: FiltersData) { _epicsFilters.value = filters }
    override suspend fun refreshEpicsFilters(serverData: FiltersData) {
        _epicsFilters.update { it.updateData(serverData) }
    }

    private val _issuesFilters = MutableStateFlow(FiltersData())
    val currentIssuesFilters: FiltersData get() = _issuesFilters.value
    override val issuesFilters: Flow<FiltersData> = _issuesFilters.onceLoaded()
    override fun changeIssuesFilters(filters: FiltersData) { _issuesFilters.value = filters }
    override suspend fun refreshIssuesFilters(serverData: FiltersData) {
        _issuesFilters.update { it.updateData(serverData) }
    }

    private val _kanbanFilters = MutableStateFlow(FiltersData())
    val currentKanbanFilters: FiltersData get() = _kanbanFilters.value
    override val kanbanFilters: Flow<FiltersData> = _kanbanFilters.onceLoaded()
    override fun changeKanbanFilters(filters: FiltersData) { _kanbanFilters.value = filters }

    override suspend fun resetFilters() {
        resetFiltersCalled = true
        _scrumFilters.value = FiltersData()
        _epicsFilters.value = FiltersData()
        _issuesFilters.value = FiltersData()
        _kanbanFilters.value = FiltersData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun StateFlow<FiltersData>.onceLoaded(): Flow<FiltersData> =
        storedFiltersLoaded.filter { it }.flatMapLatest { this }
}
