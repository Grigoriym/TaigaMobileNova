package com.grappim.taigamobile.feature.workitem.ui.widgets.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeState
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import kotlinx.collections.immutable.ImmutableSet

@Composable
fun WorkItemBadgesWidget(badgeState: WorkItemBadgeState, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        shape = RoundedCornerShape(size = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            badgeState.workItemBadges.forEach { item ->
                WorkItemClickableBadgeWidget(
                    title = item.currentValue.title.asString(context),
                    color = item.currentValue.color.asColor(),
                    onClick = {
                        badgeState.onBadgeClick(item)
                    },
                    isLoading = item in badgeState.updatingBadges,
                    isClickable = true
                )
            }
        }
    }
}
