package com.grappim.taigamobile.feature.issues.ui

import com.grappim.taigamobile.core.domain.FiltersData

data class IssuesState(
    val filters: FiltersData = FiltersData(),
    val activeFilters: FiltersData = FiltersData(),

    val isFiltersError: Boolean = false
)
