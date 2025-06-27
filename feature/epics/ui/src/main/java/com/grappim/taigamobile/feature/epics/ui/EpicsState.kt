package com.grappim.taigamobile.feature.epics.ui

import com.grappim.taigamobile.core.domain.FiltersData

data class EpicsState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val onRefresh: () -> Unit,
    val filters: FiltersData = FiltersData(),
    val activeFilters: FiltersData = FiltersData()
)
