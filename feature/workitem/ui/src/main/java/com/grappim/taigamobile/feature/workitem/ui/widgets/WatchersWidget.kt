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
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.AddButton
import com.grappim.taigamobile.uikit.widgets.button.TaigaTextButton
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WatchersWidget(
    watchers: ImmutableList<User>,
    goToProfile: (Long) -> Unit,
    onRemoveWatcherClick: (watcherId: Long) -> Unit,
    onAddWatcherClick: () -> Unit,
    isWatchersLoading: Boolean,
    isWatchedByMe: Boolean,
    onAddMeToWatchersClick: () -> Unit,
    onRemoveMeFromWatchersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(RString.watchers),
            style = MaterialTheme.typography.titleMedium
        )

        TaigaHeightSpacer(8.dp)

        watchers.forEachIndexed { index, item ->
            TeamUserWithActionWidget(
                user = item,
                goToProfile = {
                    goToProfile(item.actualId)
                },
                onRemoveUserClick = {
                    onRemoveWatcherClick(item.actualId)
                }
            )

            if (index < watchers.lastIndex) {
                Spacer(Modifier.height(6.dp))
            }
        }

        if (isWatchersLoading) {
            DotsLoader()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            AddButton(
                text = stringResource(RString.add_watcher),
                onClick = onAddWatcherClick
            )

            Spacer(modifier = Modifier.width(16.dp))

            val (@StringRes buttonText: Int, @DrawableRes buttonIcon: Int) = if (isWatchedByMe) {
                RString.unwatch to RDrawable.ic_unwatch
            } else {
                RString.watch to RDrawable.ic_watch
            }

            TaigaTextButton(
                text = stringResource(buttonText),
                icon = buttonIcon,
                onClick = {
                    if (isWatchedByMe) {
                        onRemoveMeFromWatchersClick()
                    } else {
                        onAddMeToWatchersClick()
                    }
                }
            )
        }
    }
}
