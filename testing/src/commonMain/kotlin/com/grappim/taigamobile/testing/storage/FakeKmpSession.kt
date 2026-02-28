package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.storage.FiltersSession
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeKmpSession : FiltersSession {

    private val _scrumFilters = MutableStateFlow(FiltersData())
    override val scrumFilters: StateFlow<FiltersData> = _scrumFilters.asStateFlow()
    override fun changeScrumFilters(filters: FiltersData) { _scrumFilters.value = filters }

    private val _epicsFilters = MutableStateFlow(FiltersData())
    override val epicsFilters: StateFlow<FiltersData> = _epicsFilters.asStateFlow()
    override fun changeEpicsFilters(filters: FiltersData) { _epicsFilters.value = filters }

    private val _issuesFilters = MutableStateFlow(FiltersData())
    override val issuesFilters: StateFlow<FiltersData> = _issuesFilters.asStateFlow()
    override fun changeIssuesFilters(filters: FiltersData) { _issuesFilters.value = filters }

    private val _kanbanFilters = MutableStateFlow(FiltersData())
    override val kanbanFilters: StateFlow<FiltersData> = _kanbanFilters.asStateFlow()
    override fun changeKanbanFilters(filters: FiltersData) { _kanbanFilters.value = filters }

    override fun resetFilters() {
        _scrumFilters.value = FiltersData()
        _epicsFilters.value = FiltersData()
        _issuesFilters.value = FiltersData()
        _kanbanFilters.value = FiltersData()
    }
}
