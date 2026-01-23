package com.grappim.taigamobile.feature.filters.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.filters.domain.hasData
import com.grappim.taigamobile.feature.filters.domain.model.filters.Filters
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.uikit.widgets.badge.Badge
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldStringWithHint
import com.grappim.taigamobile.uikit.widgets.editor.searchFieldHorizontalPadding
import com.grappim.taigamobile.uikit.widgets.editor.searchFieldVerticalPadding
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.coroutines.launch

/**
 * Filters for tasks (like status, assignees etc.).
 * Filters are placed in bottom sheet dialog as expandable options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilters(
    selected: FiltersData,
    onSelect: (FiltersData) -> Unit,
    data: FiltersData,
    searchQuery: String,
    setSearchQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFiltersError: Boolean = false,
    onRetryFilters: () -> Unit = {},
    isFiltersLoading: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        TextFieldStringWithHint(
            hintId = RString.tasks_search_hint,
            value = searchQuery,
            onValueChange = {
                setSearchQuery(it)
            },
            onSearchClick = {
                onSelect(selected)
            },
            horizontalPadding = searchFieldHorizontalPadding,
            verticalPadding = searchFieldVerticalPadding,
            hasBorder = true
        )

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
            }
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(RDrawable.ic_filter),
                    contentDescription = null
                )

                Spacer(Modifier.width(space))

                Text(stringResource(RString.show_filters))

                selected.filtersNumber.takeIf { it > 0 }?.let {
                    Spacer(Modifier.width(space))
                    Badge(it.toString())
                }

                if (isFiltersError) {
                    Spacer(Modifier.width(space))
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Filter error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
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
            onSelect = onSelect,
            isFiltersError = isFiltersError,
            onRetryFilters = onRetryFilters,
            isFiltersLoading = isFiltersLoading
        )
    }
}

internal inline fun <T : Filters> List<T>.ifHasData(action: (List<T>) -> Unit) = takeIf { it.hasData() }?.let(action)

@Composable
internal fun <T : Filters> Section(
    @StringRes titleId: Int,
    filters: List<T>,
    onSelect: (T) -> Unit,
    @StringRes noNameId: Int? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        var isExpanded by remember { mutableStateOf(false) }

        val transitionState = remember { MutableTransitionState(isExpanded) }
        transitionState.targetState = isExpanded

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        ) {
            val arrowRotation by rememberTransition(
                transitionState,
                "arrow"
            ).animateFloat { if (it) 0f else -90f }
            Icon(
                painter = painterResource(RDrawable.ic_arrow_down),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(arrowRotation),
                contentDescription = null
            )

            Text(
                text = stringResource(titleId),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            FlowRow(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                filters.forEach {
                    FilterChip(
                        filter = it,
                        noNameId = noNameId,
                        onClick = { onSelect(it) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun FilterChip(
    filter: Filters,
    @StringRes noNameId: Int? = null,
    onClick: () -> Unit = {},
    onRemoveClick: (() -> Unit)? = null
) {
    Chip(
        onClick = onClick,
        color = filter.color?.toColor() ?: MaterialTheme.colorScheme.outline
    ) {
        val space = 6.dp

        Row(verticalAlignment = Alignment.CenterVertically) {
            onRemoveClick?.let {
                IconButton(
                    onClick = it,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_remove),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(space))
            }

            Text(
                modifier = Modifier.weight(1f, fill = false),
                text = filter.name.takeIf { it.isNotEmpty() } ?: stringResource(noNameId!!),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(space))

            Badge(
                text = filter.count.toString(),
                isActive = false
            )
        }
    }
}

// @PreviewDarkLight
// @Composable
// private fun TaskFiltersPreview() = TaigaMobileTheme {
//    var selected by remember { mutableStateOf(FiltersDataDTO()) }
//
//    Column {
//        Text("test")
//
//        TaskFilters(
//            selected = selected,
//            onSelect = { selected = it },
//            data = FiltersDataDTO(
//                assignees = listOf(
//                    UsersFilterDTO(null, "", 2)
//                ) + List(10) { UsersFilterDTO(it.toLong(), "Human $it", it % 3L) },
//                roles = listOf(
//                    RolesFilterDTO(0, "UX", 1),
//                    RolesFilterDTO(1, "Developer", 4),
//                    RolesFilterDTO(2, "Stakeholder", 0)
//                ),
//                tags = List(10) {
//                    listOf(
//                        TagsFilterDTO("#7E57C2", "tag ${it * 3}", 3),
//                        TagsFilterDTO("#F57C00", "tag ${it * 3 + 1}", 4),
//                        TagsFilterDTO("#C62828", "tag ${it * 3 + 2}", 0)
//                    )
//                }.flatten(),
//                statuses = listOf(
//                    StatusesFilterDTO(0, "#B0BEC5", "Backlog", 2),
//                    StatusesFilterDTO(1, "#1E88E5", "In progress", 1),
//                    StatusesFilterDTO(2, "#43A047", "Done", 3)
//                ),
//                priorities = listOf(
//                    StatusesFilterDTO(0, "#29B6F6", "Low", 2),
//                    StatusesFilterDTO(1, "#43A047", "Normal", 1),
//                    StatusesFilterDTO(2, "#FBC02D", "High", 2)
//                ),
//                severities = listOf(
//                    StatusesFilterDTO(0, "#29B6F6", "Minor", 2),
//                    StatusesFilterDTO(1, "#43A047", "Normal", 1),
//                    StatusesFilterDTO(2, "#FBC02D", "Major", 2),
//                    StatusesFilterDTO(0, "#29B6F6", "Minor", 2),
//                    StatusesFilterDTO(1, "#43A047", "Normal", 1),
//                    StatusesFilterDTO(2, "#FBC02D", "Major", 2)
//                ),
//                types = listOf(
//                    StatusesFilterDTO(0, "#F44336", "Bug", 2),
//                    StatusesFilterDTO(1, "#C8E6C9", "Question", 1),
//                    StatusesFilterDTO(2, "#C8E6C9", "Enhancement", 2)
//                )
//            )
//        )
//
//        Text("Text")
//    }
// }
