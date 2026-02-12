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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.asColor
import com.grappim.taigamobile.utils.ui.asString
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

@Composable
fun WorkItemBadgesWidget(
    isOffline: Boolean,
    badgeState: WorkItemBadgeState,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
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
                    isClickable = canModify,
                    isOffline = isOffline
                )
            }
        }
    }
}

private fun previewBadges() = persistentSetOf(
    SelectableWorkItemBadgeStatus(
        options = persistentListOf(),
        currentValue = StatusUI(
            id = 1L,
            title = NativeText.Simple("In Progress"),
            color = StaticColor(Color(0xFF4CAF50))
        )
    ),
    SelectableWorkItemBadgeType(
        options = persistentListOf(),
        currentValue = StatusUI(
            id = 2L,
            title = NativeText.Simple("Bug"),
            color = StaticColor(Color(0xFFE53935))
        )
    ),
    SelectableWorkItemBadgePriority(
        options = persistentListOf(),
        currentValue = StatusUI(
            id = 3L,
            title = NativeText.Simple("High"),
            color = StaticColor(Color(0xFFFF9800))
        )
    )
)

@Composable
@PreviewTaigaDarkLight
private fun WorkItemBadgesWidgetPreview() {
    TaigaMobileTheme {
        WorkItemBadgesWidget(
            badgeState = WorkItemBadgeState(
                workItemBadges = previewBadges()
            ),
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemBadgesWidgetLoadingPreview() {
    val badges = previewBadges()
    TaigaMobileTheme {
        WorkItemBadgesWidget(
            badgeState = WorkItemBadgeState(
                workItemBadges = badges,
                updatingBadges = persistentSetOf(badges.first())
            ),
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemBadgesWidgetOfflinePreview() {
    TaigaMobileTheme {
        WorkItemBadgesWidget(
            badgeState = WorkItemBadgeState(
                workItemBadges = previewBadges()
            ),
            canModify = true,
            isOffline = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemBadgesWidgetReadOnlyPreview() {
    TaigaMobileTheme {
        WorkItemBadgesWidget(
            badgeState = WorkItemBadgeState(
                workItemBadges = previewBadges()
            ),
            canModify = false,
            isOffline = false
        )
    }
}
