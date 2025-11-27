package com.grappim.taigamobile.feature.epics.ui.list

import com.grappim.taigamobile.core.domain.FiltersDataDTO

data class EpicsState(
    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false,
    val activeFilters: FiltersDataDTO = FiltersDataDTO(),
    val selectFilters: (filters: FiltersDataDTO) -> Unit = {},
)
