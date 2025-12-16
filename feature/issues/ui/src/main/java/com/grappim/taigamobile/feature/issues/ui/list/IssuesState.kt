package com.grappim.taigamobile.feature.issues.ui.list

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData

data class IssuesState(
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false,
    val setSearchQuery: (String) -> Unit = {}
)
