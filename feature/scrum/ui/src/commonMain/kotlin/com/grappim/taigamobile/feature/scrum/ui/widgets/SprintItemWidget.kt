package com.grappim.taigamobile.feature.scrum.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.sprint_dates_template
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.PreviewUtils
import com.grappim.taigamobile.utils.formatter.datetime.platformFormatMediumDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SprintItemWidget(sprint: Sprint, goToSprint: (Sprint) -> Unit = {}) {
    ListItem(
        modifier = Modifier.clickable {
            goToSprint(sprint)
        },
        headlineContent = {
            Text(
                text = sprint.name,
                style = MaterialTheme.typography.titleMedium
            )
        },
        supportingContent = {
            Text(
                stringResource(
                    RString.sprint_dates_template,
                    platformFormatMediumDate(sprint.start),
                    platformFormatMediumDate(sprint.end)
                )
            )
        }
    )
}

@PreviewTaigaDarkLight
@Composable
private fun SprintPreviewWidget() = TaigaMobilePreviewTheme {
    SprintItemWidget(
        Sprint(
            id = 0L,
            name = "1 sprint",
            order = 0,
            start = PreviewUtils.getNowDate(),
            end = PreviewUtils.getNowDate().plus(14, DateTimeUnit.DAY),
            storiesCount = 4,
            isClosed = true
        )
    )
}
