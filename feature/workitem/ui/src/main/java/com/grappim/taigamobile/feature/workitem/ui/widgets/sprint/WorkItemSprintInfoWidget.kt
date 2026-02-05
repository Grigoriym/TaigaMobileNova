package com.grappim.taigamobile.feature.workitem.ui.widgets.sprint

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import java.time.LocalDate

@Composable
fun WorkItemSprintInfoWidget(
    sprint: Sprint?,
    isOffline: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSprintLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isOffline) {
                    Modifier.clickable {
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        TaigaHeightSpacer(4.dp)

        Text(
            text = stringResource(RString.sprint),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(4.dp)
        Row {
            if (sprint != null) {
                Chip(
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                ) {
                    Text(sprint.name)
                }
            } else {
                Chip(
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp)
                ) {
                    Text(stringResource(RString.no_sprint))
                }
            }

            if (isSprintLoading) {
                CircularLoaderWidget(modifier = Modifier.size(40.dp))
            }
        }
        TaigaHeightSpacer(4.dp)
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemSprintInfoWidgetWithSprintPreview() {
    TaigaMobilePreviewTheme {
        WorkItemSprintInfoWidget(
            sprint = Sprint(
                id = 1L,
                name = "Sprint 1",
                order = 1,
                start = LocalDate.now(),
                end = LocalDate.now().plusDays(14),
                storiesCount = 5,
                isClosed = false
            ),
            onClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemSprintInfoWidgetNoSprintPreview() {
    TaigaMobilePreviewTheme {
        WorkItemSprintInfoWidget(
            sprint = null,
            onClick = {},
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemSprintInfoWidgetLoadingPreview() {
    TaigaMobilePreviewTheme {
        WorkItemSprintInfoWidget(
            sprint = Sprint(
                id = 1L,
                name = "Sprint 1",
                order = 1,
                start = LocalDate.now(),
                end = LocalDate.now().plusDays(14),
                storiesCount = 5,
                isClosed = false
            ),
            onClick = {},
            isSprintLoading = true,
            isOffline = false
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WorkItemSprintInfoWidgetOfflinePreview() {
    TaigaMobilePreviewTheme {
        WorkItemSprintInfoWidget(
            sprint = Sprint(
                id = 1L,
                name = "Sprint 1",
                order = 1,
                start = LocalDate.now(),
                end = LocalDate.now().plusDays(14),
                storiesCount = 5,
                isClosed = false
            ),
            onClick = {},
            isOffline = true
        )
    }
}
