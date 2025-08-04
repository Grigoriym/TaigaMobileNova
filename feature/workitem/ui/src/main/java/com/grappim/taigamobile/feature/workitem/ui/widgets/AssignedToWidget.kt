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
import com.grappim.taigamobile.uikit.widgets.button.AddButton
import com.grappim.taigamobile.uikit.widgets.button.TaigaTextButton
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import kotlinx.collections.immutable.ImmutableList

@Composable
fun AssignedToWidget(
    assignees: ImmutableList<User>,
    isAssigneesLoading: Boolean,
    goToProfile: (Long) -> Unit,
    isAssignedToMe: Boolean,
    onRemoveAssigneeClick: () -> Unit,
    onUnassign: () -> Unit,
    onAssignToMe: () -> Unit,
    onAddAssigneeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(RString.assigned_to),
            style = MaterialTheme.typography.titleMedium
        )

        assignees.forEachIndexed { index, item ->
            TeamUserWithActionWidget(
                user = item,
                goToProfile = {
                    goToProfile(item.actualId)
                },
                onRemoveUserClick = {
                    onRemoveAssigneeClick()
                }
            )

            if (index < assignees.lastIndex) {
                Spacer(Modifier.height(6.dp))
            }
        }

        if (isAssigneesLoading) {
            DotsLoader()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            AddButton(
                text = stringResource(RString.add_assignee),
                onClick = onAddAssigneeClick
            )

            Spacer(modifier = Modifier.width(16.dp))

            val (@StringRes buttonText: Int, @DrawableRes buttonIcon: Int) = if (isAssignedToMe) {
                RString.unassign to RDrawable.ic_unassigned
            } else {
                RString.assign_to_me to RDrawable.ic_assignee_to_me
            }

            TaigaTextButton(
                text = stringResource(buttonText),
                icon = buttonIcon,
                onClick = {
                    if (isAssignedToMe) {
                        onUnassign()
                    } else {
                        onAssignToMe()
                    }
                }
            )
        }
    }
}
