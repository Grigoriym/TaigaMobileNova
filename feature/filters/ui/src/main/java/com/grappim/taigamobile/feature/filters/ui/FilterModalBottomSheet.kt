@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.filters.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.strings.RString

@Composable
fun FilterModalBottomSheet(
    bottomSheetState: SheetState,
    unselectedFilters: FiltersDataDTO,
    isBottomSheetVisible: Boolean,
    setBottomSheetVisible: (Boolean) -> Unit,
    selected: FiltersDataDTO,
    onSelect: (FiltersDataDTO) -> Unit,
    isFiltersError: Boolean = false,
    onRetryFilters: () -> Unit = {},
    isFiltersLoading: Boolean = false
) {
    if (isBottomSheetVisible) {
        FilterModalBottomSheetContent(
            bottomSheetState = bottomSheetState,
            unselectedFilters = unselectedFilters,
            setBottomSheetVisible = setBottomSheetVisible,
            selected = selected,
            onSelect = onSelect,
            isFiltersError = isFiltersError,
            onRetryFilters = onRetryFilters,
            isFiltersLoading = isFiltersLoading
        )
    }
}

@Composable
private fun FilterModalBottomSheetContent(
    bottomSheetState: SheetState,
    unselectedFilters: FiltersDataDTO,
    setBottomSheetVisible: (Boolean) -> Unit,
    selected: FiltersDataDTO,
    onSelect: (FiltersDataDTO) -> Unit,
    isFiltersError: Boolean = false,
    onRetryFilters: () -> Unit = {},
    isFiltersLoading: Boolean = false
) {
    val space = 6.dp

    ModalBottomSheet(
        sheetState = bottomSheetState,
        onDismissRequest = {
            setBottomSheetVisible(false)
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(space)
            ) {
                Text(
                    text = stringResource(RString.filters),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = space)
                )

                Spacer(Modifier.height(space))

                if (isFiltersError) {
                    FilterErrorContent(
                        onRetry = onRetryFilters,
                        isLoading = isFiltersLoading
                    )
                } else {
                    FilterNormalContent(
                        unselectedFilters = unselectedFilters,
                        selected = selected,
                        onSelect = onSelect
                    )
                }

                Spacer(Modifier.height(space))
            }
        }
    )
}

@Composable
private fun FilterErrorContent(onRetry: () -> Unit, isLoading: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(RString.common_error_message),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = onRetry,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(stringResource(RString.retry))
        }
    }
}

@Composable
private fun SelectedFiltersContent(selected: FiltersDataDTO, onSelect: (FiltersDataDTO) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        selected.types.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(types = selected.types - it)
                    )
                }
            )
        }

        selected.severities.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(
                            severities = selected.severities - it
                        )
                    )
                }
            )
        }

        selected.priorities.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(
                            priorities = selected.priorities - it
                        )
                    )
                }
            )
        }

        selected.statuses.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(
                            statuses = selected.statuses - it
                        )
                    )
                }
            )
        }

        selected.tags.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(tags = selected.tags - it)
                    )
                }
            )
        }

        selected.assignees.forEach {
            FilterChip(
                filter = it,
                noNameId = RString.unassigned,
                onRemoveClick = {
                    onSelect(
                        selected.copy(
                            assignees = selected.assignees - it
                        )
                    )
                }
            )
        }

        selected.roles.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(roles = selected.roles - it)
                    )
                }
            )
        }

        selected.createdBy.forEach {
            FilterChip(
                filter = it,
                onRemoveClick = {
                    onSelect(
                        selected.copy(
                            createdBy = selected.createdBy - it
                        )
                    )
                }
            )
        }

        selected.epics.forEach {
            FilterChip(
                filter = it,
                noNameId = RString.not_in_an_epic,
                onRemoveClick = {
                    onSelect(
                        selected.copy(epics = selected.epics - it)
                    )
                }
            )
        }
    }
}

@Composable
private fun FilterNormalContent(
    unselectedFilters: FiltersDataDTO,
    selected: FiltersDataDTO,
    onSelect: (FiltersDataDTO) -> Unit
) {
    val space = 6.dp
    Column(modifier = Modifier.fillMaxWidth()) {
        SelectedFiltersContent(selected, onSelect)

        if (selected.filtersNumber > 0) {
            Spacer(Modifier.height(space))
        }

        val sectionsSpace = 6.dp

        unselectedFilters.types.ifHasData {
            Section(
                titleId = RString.type_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(types = selected.types + it)
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.severities.ifHasData {
            Section(
                titleId = RString.severity_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(
                            severities = selected.severities + it
                        )
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.priorities.ifHasData {
            Section(
                titleId = RString.priority_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(
                            priorities = selected.priorities + it
                        )
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.statuses.ifHasData {
            Section(
                titleId = RString.status_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(statuses = selected.statuses + it)
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.tags.ifHasData {
            Section(
                titleId = RString.tags_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(tags = selected.tags + it)
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.assignees.ifHasData {
            Section(
                titleId = RString.assignees_title,
                noNameId = RString.unassigned,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(
                            assignees = selected.assignees + it
                        )
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.roles.ifHasData {
            Section(
                titleId = RString.role_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(roles = selected.roles + it)
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.createdBy.ifHasData {
            Section(
                titleId = RString.created_by_title,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(
                            createdBy = selected.createdBy + it
                        )
                    )
                }
            )
            Spacer(Modifier.height(sectionsSpace))
        }

        unselectedFilters.epics.ifHasData {
            Section(
                titleId = RString.epic_title,
                noNameId = RString.not_in_an_epic,
                filters = it,
                onSelect = {
                    onSelect(
                        selected.copy(epics = selected.epics + it)
                    )
                }
            )
        }
    }

    Spacer(Modifier.height(space))
}
