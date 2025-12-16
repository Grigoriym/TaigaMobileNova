package com.grappim.taigamobile.feature.epics.ui.list

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData

data class EpicsState(
    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false,
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val onSetQuery: (String) -> Unit = {}
)
