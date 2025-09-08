package com.grappim.taigamobile.feature.issues.ui.list

import com.grappim.taigamobile.core.domain.FiltersDataDTO

data class IssuesState(
    val activeFilters: FiltersDataDTO = FiltersDataDTO(),
    val selectFilters: (filters: FiltersDataDTO) -> Unit = {},
    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false
)
