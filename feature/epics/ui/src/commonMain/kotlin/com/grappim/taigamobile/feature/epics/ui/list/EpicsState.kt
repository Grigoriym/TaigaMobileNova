package com.grappim.taigamobile.feature.epics.ui.list

import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData

data class EpicsState(
    val activeFilters: FiltersData = FiltersData(),
    val selectFilters: (filters: FiltersData) -> Unit = {},
    val onSetQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filters: FiltersData = FiltersData(),
    val filtersError: NativeText = NativeText.Empty,

    val canAddEpic: Boolean = false
)
