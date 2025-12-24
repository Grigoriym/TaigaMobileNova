package com.grappim.taigamobile.feature.epics.ui.list

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.utils.ui.NativeText

data class EpicsState(
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val onSetQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filters: FiltersData = FiltersData(),
    val filtersError: NativeText = NativeText.Empty
)
