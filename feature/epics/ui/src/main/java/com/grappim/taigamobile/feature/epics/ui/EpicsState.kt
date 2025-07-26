package com.grappim.taigamobile.feature.epics.ui

import com.grappim.taigamobile.core.domain.FiltersDataDTO

data class EpicsState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val onRefresh: () -> Unit,
    val filters: FiltersDataDTO = FiltersDataDTO(),
    val activeFilters: FiltersDataDTO = FiltersDataDTO()
)
