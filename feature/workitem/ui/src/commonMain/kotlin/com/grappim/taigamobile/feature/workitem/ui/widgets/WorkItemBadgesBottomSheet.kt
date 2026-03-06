@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.uikit.generated.resources.ic_check
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource

@Composable
fun WorkItemBadgesBottomSheet(
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
                    it.title.asString()
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
    displayTransform: @Composable (T) -> String,
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
                        Icon(painter = painterResource(RDrawable.ic_check), contentDescription = "")
                    }
                }
            )

            if (index < options.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

private val previewStatuses = persistentListOf(
    StatusUI(id = 1L, title = NativeText.Simple("New"), color = StaticColor(Color(0xFF70728F))),
    StatusUI(id = 2L, title = NativeText.Simple("In Progress"), color = StaticColor(Color(0xFFE47C40))),
    StatusUI(id = 3L, title = NativeText.Simple("Ready for Test"), color = StaticColor(Color(0xFFE8B800))),
    StatusUI(id = 4L, title = NativeText.Simple("Done"), color = StaticColor(Color(0xFF2ECC71))),
)

@PreviewTaigaDarkLight
@Composable
private fun OptionsListPreview() {
    TaigaMobilePreviewTheme {
        OptionsList(
            options = previewStatuses,
            onOptionSelect = {},
            displayTransform = { it.title.asString() },
            colorProvider = { it.color.asColor() },
            isOptionSelect = { it.id == 2L }
        )
    }
}
