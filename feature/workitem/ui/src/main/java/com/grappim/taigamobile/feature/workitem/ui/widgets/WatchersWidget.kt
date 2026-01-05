package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.AddButtonWidget
import com.grappim.taigamobile.uikit.widgets.button.TaigaTextButtonWidget
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget

@Composable
fun WatchersWidget(
    watchersState: WorkItemWatchersState,
    goToProfile: (Long) -> Unit,
    onAddWatcherClick: () -> Unit,
    onAddMeToWatchersClick: () -> Unit,
    onRemoveMeFromWatchersClick: () -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(RString.watchers),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(8.dp)

        watchersState.watchers.forEachIndexed { index, item ->
            TeamUserWithActionWidget(
                user = item,
                goToProfile = {
                    goToProfile(item.actualId)
                },
                onRemoveUserClick = {
                    watchersState.onRemoveWatcherClick(item.actualId)
                },
                canModify = canModify
            )

            if (index < watchersState.watchers.lastIndex) {
                Spacer(Modifier.height(6.dp))
            }
        }

        if (watchersState.areWatchersLoading) {
            DotsLoaderWidget()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            if (canModify) {
                AddButtonWidget(
                    text = stringResource(RString.add_watcher),
                    onClick = onAddWatcherClick
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            val (@StringRes buttonText: Int, @DrawableRes buttonIcon: Int) = if (watchersState.isWatchedByMe) {
                RString.unwatch to RDrawable.ic_unwatch
            } else {
                RString.watch to RDrawable.ic_watch
            }

            TaigaTextButtonWidget(
                text = stringResource(buttonText),
                icon = buttonIcon,
                onClick = {
                    if (watchersState.isWatchedByMe) {
                        onRemoveMeFromWatchersClick()
                    } else {
                        onAddMeToWatchersClick()
                    }
                }
            )
        }
    }
}
