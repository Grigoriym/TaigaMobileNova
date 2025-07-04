package com.grappim.taigamobile.commontask.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.EditActions
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.button.AddButton
import com.grappim.taigamobile.uikit.widgets.button.TextButton
import com.grappim.taigamobile.uikit.widgets.list.UserItemWithAction
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader

@Suppress("FunctionName")
fun LazyListScope.CommonTaskWatchers(
    watchers: List<User>,
    isWatchedByMe: Boolean,
    editActions: EditActions,
    showWatchersSelector: () -> Unit,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        // watchers
        Text(
            text = stringResource(RString.watchers),
            style = MaterialTheme.typography.titleMedium
        )
    }

    itemsIndexed(watchers) { index, item ->
        UserItemWithAction(
            user = item,
            onRemoveClick = { editActions.editWatchers.remove(item) },
            onUserItemClick = { navigateToProfile(item.actualId) }
        )

        if (index < watchers.lastIndex) {
            Spacer(Modifier.height(6.dp))
        }
    }

    // add watcher & loader
    item {
        if (editActions.editWatchers.isLoading) {
            DotsLoader()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            AddButton(
                text = stringResource(RString.add_watcher),
                onClick = { showWatchersSelector() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            val (@StringRes buttonText: Int, @DrawableRes buttonIcon: Int) = if (isWatchedByMe) {
                RString.unwatch to RDrawable.ic_unwatch
            } else {
                RString.watch to RDrawable.ic_watch
            }

            TextButton(
                text = stringResource(buttonText),
                icon = buttonIcon,
                onClick = {
                    if (isWatchedByMe) {
                        editActions.editWatch.remove(Unit)
                    } else {
                        editActions.editWatch.select(Unit)
                    }
                }
            )
        }
    }
}
