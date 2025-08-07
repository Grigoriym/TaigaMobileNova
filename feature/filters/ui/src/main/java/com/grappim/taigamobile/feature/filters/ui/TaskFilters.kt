package com.grappim.taigamobile.feature.filters.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.Filter
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.RolesFilter
import com.grappim.taigamobile.core.domain.StatusesFilter
import com.grappim.taigamobile.core.domain.TagsFilter
import com.grappim.taigamobile.core.domain.UsersFilter
import com.grappim.taigamobile.core.domain.hasData
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.utils.clickableUnindicated
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.uikit.widgets.badge.Badge
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.uikit.widgets.editor.searchFieldHorizontalPadding
import com.grappim.taigamobile.uikit.widgets.editor.searchFieldVerticalPadding
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.coroutines.launch

/**
 * TaskFilters which reacts to LazyList scroll state
 */
@Composable
fun TasksFiltersWithLazyList(
    modifier: Modifier = Modifier,
    filters: FiltersDataDTO = FiltersDataDTO(),
    activeFilters: FiltersDataDTO = FiltersDataDTO(),
    selectFilters: (FiltersDataDTO) -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            TaskFilters(
                selected = activeFilters,
                onSelect = selectFilters,
                data = filters
            )
        }

        content()
    }
}

/**
 * Filters for tasks (like status, assignees etc.).
 * Filters are placed in bottom sheet dialog as expandable options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilters(
    selected: FiltersDataDTO,
    onSelect: (FiltersDataDTO) -> Unit,
    data: FiltersDataDTO,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(
                TextFieldValue(
                    selected.query
                )
            )
        }

        TextFieldWithHint(
            hintId = RString.tasks_search_hint,
            value = query,
            onValueChange = { query = it },
            onSearchClick = { onSelect(selected.copy(query = query.text)) },
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
}

internal inline fun <T : Filter> List<T>.ifHasData(action: (List<T>) -> Unit) =
    takeIf { it.hasData() }?.let(action)

@Composable
internal fun <T : Filter> Section(
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
            modifier = Modifier.clickableUnindicated { isExpanded = !isExpanded }
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
    filter: Filter,
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

@PreviewDarkLight
@Composable
private fun TaskFiltersPreview() = TaigaMobileTheme {
    var selected by remember { mutableStateOf(FiltersDataDTO()) }

    Column {
        Text("test")

        TaskFilters(
            selected = selected,
            onSelect = { selected = it },
            data = FiltersDataDTO(
                assignees = listOf(
                    UsersFilter(null, "", 2)
                ) + List(10) { UsersFilter(it.toLong(), "Human $it", it % 3L) },
                roles = listOf(
                    RolesFilter(0, "UX", 1),
                    RolesFilter(1, "Developer", 4),
                    RolesFilter(2, "Stakeholder", 0)
                ),
                tags = List(10) {
                    listOf(
                        TagsFilter("#7E57C2", "tag ${it * 3}", 3),
                        TagsFilter("#F57C00", "tag ${it * 3 + 1}", 4),
                        TagsFilter("#C62828", "tag ${it * 3 + 2}", 0)
                    )
                }.flatten(),
                statuses = listOf(
                    StatusesFilter(0, "#B0BEC5", "Backlog", 2),
                    StatusesFilter(1, "#1E88E5", "In progress", 1),
                    StatusesFilter(2, "#43A047", "Done", 3)
                ),
                priorities = listOf(
                    StatusesFilter(0, "#29B6F6", "Low", 2),
                    StatusesFilter(1, "#43A047", "Normal", 1),
                    StatusesFilter(2, "#FBC02D", "High", 2)
                ),
                severities = listOf(
                    StatusesFilter(0, "#29B6F6", "Minor", 2),
                    StatusesFilter(1, "#43A047", "Normal", 1),
                    StatusesFilter(2, "#FBC02D", "Major", 2),
                    StatusesFilter(0, "#29B6F6", "Minor", 2),
                    StatusesFilter(1, "#43A047", "Normal", 1),
                    StatusesFilter(2, "#FBC02D", "Major", 2)
                ),
                types = listOf(
                    StatusesFilter(0, "#F44336", "Bug", 2),
                    StatusesFilter(1, "#C8E6C9", "Question", 1),
                    StatusesFilter(2, "#C8E6C9", "Enhancement", 2)
                )
            )
        )

        Text("Text")
    }
}
