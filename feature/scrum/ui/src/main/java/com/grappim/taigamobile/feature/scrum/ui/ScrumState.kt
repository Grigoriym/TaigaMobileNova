package com.grappim.taigamobile.feature.scrum.ui

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import java.time.LocalDate
import kotlin.enums.EnumEntries

data class ScrumState(
    val loading: Boolean = false,
    val tabs: EnumEntries<ScrumTabs> = ScrumTabs.entries,
    val activeFilters: FiltersData = FiltersData(),
    val onSelectFilters: (filters: FiltersData) -> Unit = {},

    val isCreateSprintDialogVisible: Boolean = false,
    val setIsCreateSprintDialogVisible: (Boolean) -> Unit = {},

    val onSetSearchQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false,

    val onCreateSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> }
)
