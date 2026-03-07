package com.grappim.taigamobile.core.storage

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.flow.StateFlow

interface FiltersStorage {
    val scrumFilters: StateFlow<FiltersData>
    fun changeScrumFilters(filters: FiltersData)

    val epicsFilters: StateFlow<FiltersData>
    fun changeEpicsFilters(filters: FiltersData)

    val issuesFilters: StateFlow<FiltersData>
    fun changeIssuesFilters(filters: FiltersData)

    val kanbanFilters: StateFlow<FiltersData>
    fun changeKanbanFilters(filters: FiltersData)

    suspend fun resetFilters()
}
