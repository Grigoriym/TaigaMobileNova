package com.grappim.taigamobile.feature.issues.ui.list

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.utils.ui.NativeText

data class IssuesState(
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val setSearchQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filters: FiltersData = FiltersData(),
    val filtersError: NativeText = NativeText.Empty
)
