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
import com.grappim.taigamobile.uikit.widgets.Chip
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget

@Composable
fun WorkItemSprintInfoWidget(
    sprint: Sprint?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSprintLoading: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
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
