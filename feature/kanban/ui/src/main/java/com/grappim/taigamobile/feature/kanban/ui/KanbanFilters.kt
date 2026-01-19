@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.ui.FilterModalBottomSheet
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.badge.Badge
import kotlinx.coroutines.launch

@Composable
fun KanbanFilters(
    selected: FiltersData,
    data: FiltersData,
    onSelect: (FiltersData) -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedFilters = data - selected
    val space = 6.dp

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    FilledTonalButton(
        onClick = {
            coroutineScope.launch {
                if (!bottomSheetState.isVisible) {
                    isBottomSheetVisible = true
                }
            }
        },
        enabled = data.assignees.isNotEmpty(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(RDrawable.ic_filter),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.width(space))

            Text(stringResource(RString.show_filters))

            selected.filtersNumber.takeIf { it > 0 }?.let {
                Spacer(Modifier.width(space))
                Badge(it.toString())
            }
        }
    }

    Spacer(Modifier.height(space))

    FilterModalBottomSheet(
        bottomSheetState = bottomSheetState,
        unselectedFilters = unselectedFilters,
        isBottomSheetVisible = isBottomSheetVisible,
        setBottomSheetVisible = { isBottomSheetVisible = it },
        selected = selected,
        onSelect = onSelect
    )
}
