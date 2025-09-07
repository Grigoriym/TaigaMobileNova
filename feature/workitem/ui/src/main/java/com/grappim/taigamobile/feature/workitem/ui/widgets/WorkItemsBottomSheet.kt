@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WorkItemsBottomSheet(
    activeBadge: SelectableWorkItemBadgeState?,
    bottomSheetState: SheetState,
    onBottomSheetItemSelect: (SelectableWorkItemBadgeState, StatusUI) -> Unit,
    onDismiss: () -> Unit
) {
    if (activeBadge != null) {
        WorkItemBottomSheetContent(
            bottomSheetState = bottomSheetState,
            onDismiss = onDismiss,
            activeBadge = activeBadge,
            onBottomSheetItemSelect = onBottomSheetItemSelect
        )
    }
}

@Composable
private fun WorkItemBottomSheetContent(
    activeBadge: SelectableWorkItemBadgeState,
    bottomSheetState: SheetState,
    onBottomSheetItemSelect: (SelectableWorkItemBadgeState, StatusUI) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.background,
        sheetState = bottomSheetState,
        onDismissRequest = {
            onDismiss()
        },
        content = {
            OptionsList(
                options = activeBadge.options,
                onOptionSelect = {
                    onBottomSheetItemSelect(activeBadge, it)
                },
                displayTransform = {
                    it.title.asString(context)
                },
                colorProvider = {
                    it.color.asColor()
                },
                isOptionSelect = {
                    activeBadge.currentValue.id == it.id
                }
            )
        }
    )
}

@Composable
private fun <T> OptionsList(
    options: ImmutableList<T>,
    colorProvider: @Composable (T) -> Color,
    onOptionSelect: (T) -> Unit,
    displayTransform: (T) -> String,
    isOptionSelect: (T) -> Boolean
) {
    LazyColumn {
        itemsIndexed(options) { index, option ->
            ListItem(
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.clickable {
                    onOptionSelect(option)
                },
                headlineContent = {
                    Text(
                        text = displayTransform(option),
                        color = colorProvider.invoke(option)
                    )
                },
                trailingContent = {
                    if (isOptionSelect(option)) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "")
                    }
                }
            )

            if (index < options.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}
