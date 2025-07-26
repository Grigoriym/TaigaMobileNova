package com.grappim.taigamobile.feature.workitem.ui.widgets.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.button.AddButton
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WorkItemTagsWidget(
    tags: ImmutableList<TagUI>,
    goToEditTags: () -> Unit,
    areTagsLoading: Boolean,
    onTagRemoveClick: (TagUI) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        FlowRow(
            itemVerticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                TagItemWidget(
                    tag = tag,
                    onRemoveClick = {
                        onTagRemoveClick(tag)
                    }
                )
            }

            if (areTagsLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                AddButton(
                    text = stringResource(RString.add_tag),
                    onClick = {
                        goToEditTags()
                    }
                )
            }
        }
    }
}
