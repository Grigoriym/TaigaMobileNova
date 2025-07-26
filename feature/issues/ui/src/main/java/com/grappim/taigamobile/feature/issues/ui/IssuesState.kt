package com.grappim.taigamobile.feature.issues.ui

import com.grappim.taigamobile.core.domain.FiltersDataDTO

data class IssuesState(
    val filters: FiltersDataDTO = FiltersDataDTO(),
    val activeFilters: FiltersDataDTO = FiltersDataDTO(),

    val isFiltersError: Boolean = false,
    val onUpdateData: () -> Unit
)
