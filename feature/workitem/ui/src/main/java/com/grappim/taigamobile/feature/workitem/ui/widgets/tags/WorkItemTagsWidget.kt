package com.grappim.taigamobile.feature.workitem.ui.widgets.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsState
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.AddButtonWidget
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WorkItemTagsWidget(
    isOffline: Boolean,
    tagsState: WorkItemTagsState,
    goToEditTags: () -> Unit,
    onTagRemoveClick: (SelectableTagUI) -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(RString.tags_title),
            style = MaterialTheme.typography.bodySmall
        )
        TaigaHeightSpacer(4.dp)
        FlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tagsState.tags.forEach { tag ->
                TagItemWidget(
                    tag = tag,
                    onRemoveClick = {
                        onTagRemoveClick(tag)
                    },
                    canModify = canModify,
                    isOffline = isOffline
                )
            }

            if (tagsState.areTagsLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (canModify) {
                AddButtonWidget(
                    text = stringResource(RString.edit_tags),
                    isOffline = isOffline,
                    onClick = {
                        goToEditTags()
                    }
                )
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemTagsWidgetWithTagsPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTagsWidget(
            tagsState = WorkItemTagsState(
                tags = persistentListOf(
                    SelectableTagUI(name = "Bug", color = Color.Red),
                    SelectableTagUI(name = "Feature", color = Color.Blue),
                    SelectableTagUI(name = "Enhancement", color = Color.Green)
                )
            ),
            goToEditTags = {},
            onTagRemoveClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemTagsWidgetEmptyPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTagsWidget(
            tagsState = WorkItemTagsState(),
            goToEditTags = {},
            onTagRemoveClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemTagsWidgetLoadingPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTagsWidget(
            tagsState = WorkItemTagsState(
                tags = persistentListOf(
                    SelectableTagUI(name = "Bug", color = Color.Red)
                ),
                areTagsLoading = true
            ),
            goToEditTags = {},
            onTagRemoveClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemTagsWidgetReadOnlyPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTagsWidget(
            tagsState = WorkItemTagsState(
                tags = persistentListOf(
                    SelectableTagUI(name = "Bug", color = Color.Red),
                    SelectableTagUI(name = "Feature", color = Color.Blue)
                )
            ),
            goToEditTags = {},
            onTagRemoveClick = {},
            canModify = false,
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemTagsWidgetOfflinePreview() {
    TaigaMobilePreviewTheme {
        WorkItemTagsWidget(
            tagsState = WorkItemTagsState(
                tags = persistentListOf(
                    SelectableTagUI(name = "Bug", color = Color.Red)
                )
            ),
            goToEditTags = {},
            onTagRemoveClick = {},
            isOffline = true
        )
    }
}
