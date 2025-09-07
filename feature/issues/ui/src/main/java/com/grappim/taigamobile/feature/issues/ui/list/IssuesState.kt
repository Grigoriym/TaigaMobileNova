package com.grappim.taigamobile.feature.issues.ui.list

import com.grappim.taigamobile.core.domain.FiltersDataDTO

data class IssuesState(
    val filters: FiltersDataDTO = FiltersDataDTO(),
    val activeFilters: FiltersDataDTO = FiltersDataDTO(),
    val selectFilters: (filters: FiltersDataDTO) -> Unit = {},
    val isFiltersError: Boolean = false
)
