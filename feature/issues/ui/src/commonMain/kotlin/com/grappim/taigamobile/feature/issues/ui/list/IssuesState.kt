package com.grappim.taigamobile.feature.issues.ui.list

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData

data class IssuesState(
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val setSearchQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filters: FiltersData = FiltersData(),
    val filtersError: NativeText = NativeText.Empty,

    val canCreateIssue: Boolean = false
)
