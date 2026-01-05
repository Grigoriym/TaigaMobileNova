package com.grappim.taigamobile.feature.dashboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.TaigaWidthSpacer
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun DashboardScreen(
    navigateToTaskScreen: (Long, CommonTaskType, Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.updateInternalProjectData()
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.dashboard),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    DashboardScreenContent(
        state = state,
        navigateToTask = {
            navigateToTaskScreen(it.id, it.taskType, it.ref)
        }
    )
}

@Composable
private fun DashboardScreenContent(
    state: DashboardState,
    modifier: Modifier = Modifier,
    navigateToTask: (WorkItem) -> Unit = {}
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val titleColor = MaterialTheme.colorScheme.onSurfaceVariant

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            DashboardInfoBanner()
        }

        item {
            DashboardSectionCard(
                titleRes = RString.dashboard_watching_title,
                subtitleRes = RString.dashboard_watching_subtitle,
                containerColor = containerColor,
                titleColor = titleColor,
                sectionState = state.watchingSection,
                navigateToTask = navigateToTask,
                showTimestamp = false,
                showCheckmark = false,
                onToggle = state.onToggleWatching,
                onRetry = state.onRetryWatching
            )
        }

        item {
            DashboardSectionCard(
                titleRes = RString.dashboard_my_work_title,
                subtitleRes = RString.dashboard_my_work_subtitle,
                containerColor = containerColor,
                titleColor = titleColor,
                sectionState = state.myWorkSection,
                navigateToTask = navigateToTask,
                showTimestamp = false,
                showCheckmark = false,
                onToggle = state.onToggleMyWork,
                onRetry = state.onRetryMyWork
            )
        }

        item {
            DashboardSectionCard(
                titleRes = RString.dashboard_recent_activity_title,
                subtitleRes = RString.dashboard_recent_activity_subtitle,
                containerColor = containerColor,
                titleColor = titleColor,
                sectionState = state.recentActivitySection,
                navigateToTask = navigateToTask,
                showTimestamp = true,
                showCheckmark = false,
                onToggle = state.onToggleRecentActivity,
                onRetry = state.onRetryRecentActivity
            )
        }

        item {
            DashboardSectionCard(
                titleRes = RString.dashboard_completed_title,
                subtitleRes = RString.dashboard_completed_subtitle,
                containerColor = containerColor,
                titleColor = titleColor,
                sectionState = state.recentlyCompletedSection,
                navigateToTask = navigateToTask,
                showTimestamp = false,
                showCheckmark = true,
                onToggle = state.onToggleRecentlyCompleted,
                onRetry = state.onRetryRecentlyCompleted
            )
        }

        item {
            TaigaHeightSpacer(16.dp)
        }
    }
}

@Composable
private fun DashboardSectionCard(
    titleRes: Int,
    subtitleRes: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    titleColor: androidx.compose.ui.graphics.Color,
    sectionState: DashboardSectionState,
    navigateToTask: (WorkItem) -> Unit,
    onToggle: () -> Unit,
    onRetry: () -> Unit,
    showTimestamp: Boolean = false,
    showCheckmark: Boolean = false
) {
    val title = stringResource(titleRes)
    val subtitle = stringResource(subtitleRes)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onToggle,
                    enabled = !sectionState.isLoading && sectionState.error.isEmpty()
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (sectionState.items.isNotEmpty()) "$title (${sectionState.items.size})" else title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                TaigaHeightSpacer(4.dp)
                Text(
                    text = if (sectionState.isLoading) {
                        stringResource(RString.dashboard_loading)
                    } else {
                        subtitle
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = titleColor.copy(alpha = 0.7f)
                )
            }
            when {
                sectionState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp),
                        strokeWidth = 2.dp,
                        color = titleColor
                    )
                }

                sectionState.error.isEmpty() -> {
                    Icon(
                        imageVector = if (sectionState.isExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                        contentDescription = if (sectionState.isExpanded) "Collapse" else "Expand",
                        tint = titleColor
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = sectionState.error.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(RString.dashboard_error_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                TaigaHeightSpacer(8.dp)
                Button(
                    onClick = onRetry,
                    enabled = !sectionState.isRetrying,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = titleColor
                    )
                ) {
                    if (sectionState.isRetrying) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .width(16.dp)
                                .height(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(RString.retry))
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = sectionState.isExpanded && sectionState.items.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                sectionState.items.forEach { item ->
                    DashboardWorkItemCard(
                        item = item,
                        onClick = { navigateToTask(item) },
                        showTimestamp = showTimestamp,
                        showCheckmark = showCheckmark
                    )
                    if (item != sectionState.items.last()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardInfoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Information",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(RString.dashboard_info_banner_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                TaigaHeightSpacer(4.dp)
                Text(
                    text = stringResource(RString.dashboard_info_banner_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun DashboardWorkItemCard(
    item: WorkItem,
    onClick: () -> Unit,
    showTimestamp: Boolean = false,
    showCheckmark: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (showTimestamp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${item.ref}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TaigaWidthSpacer(8.dp)
                    Text(
                        text = item.taskType.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(RString.dashboard_updated_today),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TaigaHeightSpacer(4.dp)
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else if (showCheckmark) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "#${item.ref}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TaigaWidthSpacer(8.dp)
                        Text(
                            text = item.taskType.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TaigaHeightSpacer(4.dp)
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "#${item.ref}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TaigaWidthSpacer(8.dp)
                Text(
                    text = item.taskType.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (item.isBlocked) {
                    TaigaWidthSpacer(8.dp)
                    Text(
                        text = stringResource(RString.blocked).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            TaigaHeightSpacer(4.dp)
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            TaigaHeightSpacer(4.dp)
            Text(
                text = item.status.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
