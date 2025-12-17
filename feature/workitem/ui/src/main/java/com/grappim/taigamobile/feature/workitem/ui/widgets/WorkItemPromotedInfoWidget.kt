package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.domain.PromotedUserStoryInfo
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WorkItemPromotedInfoWidget(
    infos: ImmutableList<PromotedUserStoryInfo>,
    modifier: Modifier = Modifier,
    onInfoClick: (PromotedUserStoryInfo) -> Unit = {}
) {
    if (infos.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
        ) {
            TaigaHeightSpacer(4.dp)

            Text(
                text = stringResource(RString.issue_was_promoted_to),
                style = MaterialTheme.typography.bodySmall
            )

            TaigaHeightSpacer(4.dp)

            infos.forEach { info ->
                UserStoryInfoWidget(
                    info = info,
                    onInfoClick = onInfoClick
                )
            }

            TaigaHeightSpacer(2.dp)
        }
    }
}

@Composable
private fun UserStoryInfoWidget(info: PromotedUserStoryInfo, onInfoClick: (PromotedUserStoryInfo) -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        color = MaterialTheme.colorScheme.surface,
        onClick = {
            onInfoClick(info)
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                text = info.titleToDisplay,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
