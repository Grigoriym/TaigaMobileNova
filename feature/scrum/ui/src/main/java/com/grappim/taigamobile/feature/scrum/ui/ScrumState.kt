package com.grappim.taigamobile.feature.scrum.ui

import com.grappim.taigamobile.core.domain.FiltersDataDTO
import java.time.LocalDate
import kotlin.enums.EnumEntries

data class ScrumState(
    val loading: Boolean = false,
    val tabs: EnumEntries<ScrumTabs> = ScrumTabs.entries,
    val activeFilters: FiltersDataDTO = FiltersDataDTO(),
    val onSelectFilters: (filters: FiltersDataDTO) -> Unit = {},

    val isCreateSprintDialogVisible: Boolean = false,
    val setIsCreateSprintDialogVisible: (Boolean) -> Unit = {},

    val retryLoadFilters: () -> Unit = {},
    val isFiltersError: Boolean = false,
    val isFiltersLoading: Boolean = false,

    val onCreateSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> }
)
