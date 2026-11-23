package com.grappim.taigamobile.core.storage

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.flow.Flow

/**
 * The filter selection saved per list screen.
 *
 * Each flow emits only once the stored selection has been read, never a placeholder before that —
 * an empty [FiltersData] always means "nothing selected".
 */
interface FiltersStorage {
    val scrumFilters: Flow<FiltersData>
    fun changeScrumFilters(filters: FiltersData)

    /**
     * Refreshes the names, colors and counts of the stored selection from [serverData]
     * (see [FiltersData.updateData]). Reads the stored value itself, so it is safe to call before
     * [scrumFilters] has emitted.
     */
    suspend fun refreshScrumFilters(serverData: FiltersData)

    val epicsFilters: Flow<FiltersData>
    fun changeEpicsFilters(filters: FiltersData)

    /** Same as [refreshScrumFilters], for [epicsFilters]. */
    suspend fun refreshEpicsFilters(serverData: FiltersData)

    val issuesFilters: Flow<FiltersData>
    fun changeIssuesFilters(filters: FiltersData)

    /** Same as [refreshScrumFilters], for [issuesFilters]. */
    suspend fun refreshIssuesFilters(serverData: FiltersData)

    val kanbanFilters: Flow<FiltersData>
    fun changeKanbanFilters(filters: FiltersData)

    suspend fun resetFilters()
}
