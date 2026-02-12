package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.ExpandableMarkdownText

@Composable
fun WorkItemDescriptionWidget(
    isOffline: Boolean,
    descriptionState: WorkItemDescriptionState,
    description: String?,
    onDescriptionClick: () -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (canModify && !isOffline) {
                    Modifier.clickable {
                        onDescriptionClick()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        TaigaHeightSpacer(8.dp)

        Text(
            text = stringResource(RString.description_hint),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(4.dp)

        if (description?.isNotEmpty() == true) {
            ExpandableMarkdownText(
                text = description
            )
        } else {
            Text(
                text = stringResource(
                    if (canModify) {
                        RString.add_description
                    } else {
                        RString.no_description_yet
                    }
                ),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (descriptionState.isDescriptionLoading) {
            CircularLoaderWidget(modifier = Modifier.size(40.dp))
        }

        TaigaHeightSpacer(8.dp)
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemDescriptionWidgetWithDescriptionPreview() {
    TaigaMobileTheme {
        Surface {
            WorkItemDescriptionWidget(
                descriptionState = WorkItemDescriptionState(),
                description = "This is a **sample description** with some markdown content.\n\n- Item 1\n- Item 2",
                onDescriptionClick = {},
                canModify = true,
                isOffline = false
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemDescriptionWidgetEmptyCanModifyPreview() {
    TaigaMobileTheme {
        Surface {
            WorkItemDescriptionWidget(
                descriptionState = WorkItemDescriptionState(),
                description = null,
                onDescriptionClick = {},
                canModify = true,
                isOffline = false
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemDescriptionWidgetEmptyCannotModifyPreview() {
    TaigaMobileTheme {
        Surface {
            WorkItemDescriptionWidget(
                descriptionState = WorkItemDescriptionState(),
                description = null,
                onDescriptionClick = {},
                canModify = false,
                isOffline = false
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemDescriptionWidgetLoadingPreview() {
    TaigaMobileTheme {
        Surface {
            WorkItemDescriptionWidget(
                descriptionState = WorkItemDescriptionState(isDescriptionLoading = true),
                description = "Loading description...",
                onDescriptionClick = {},
                canModify = true,
                isOffline = false
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemDescriptionWidgetOfflinePreview() {
    TaigaMobileTheme {
        Surface {
            WorkItemDescriptionWidget(
                descriptionState = WorkItemDescriptionState(),
                description = "This description cannot be edited while offline.",
                onDescriptionClick = {},
                canModify = true,
                isOffline = true
            )
        }
    }
}
