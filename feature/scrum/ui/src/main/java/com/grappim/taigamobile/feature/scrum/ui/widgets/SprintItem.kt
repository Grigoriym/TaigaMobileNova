package com.grappim.taigamobile.feature.scrum.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun SprintItem(sprint: Sprint, goToSprint: (Sprint) -> Unit = {}) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

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
                stringResource(RString.sprint_dates_template).format(
                    sprint.start.format(dateFormatter),
                    sprint.end.format(dateFormatter)
                )
            )
        }
    )
}

@PreviewTaigaDarkLight
@Composable
private fun SprintPreview() = TaigaMobilePreviewTheme {
    SprintItem(
        Sprint(
            id = 0L,
            name = "1 sprint",
            order = 0,
            start = LocalDate.now(),
            end = LocalDate.now(),
            storiesCount = 4,
            isClosed = true
        )
    )
}
