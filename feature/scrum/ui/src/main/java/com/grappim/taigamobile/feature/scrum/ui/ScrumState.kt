package com.grappim.taigamobile.feature.scrum.ui

import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.utils.ui.NativeText
import kotlin.enums.EnumEntries

data class ScrumState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val tabs: EnumEntries<ScrumTabs> = ScrumTabs.entries,
    val activeFilters: FiltersData = FiltersData(),
    val onSelectFilters: (filters: FiltersData) -> Unit = {},
    val filters: FiltersData = FiltersData(),

    val onSetSearchQuery: (String) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersLoading: Boolean = false,
    val filtersError: NativeText = NativeText.Empty,

    val onCreateSprintConfirm: () -> Unit = {},
    val onCreateSprintClick: () -> Unit = {}
)
