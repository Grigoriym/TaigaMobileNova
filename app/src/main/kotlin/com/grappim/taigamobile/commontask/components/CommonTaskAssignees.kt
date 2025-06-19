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
import com.grappim.taigamobile.commontask.EditActions
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.ui.components.lists.UserItemWithAction
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.button.AddButton
import com.grappim.taigamobile.uikit.widgets.button.TextButton
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader

@Suppress("FunctionName")
fun LazyListScope.CommonTaskAssignees(
    assignees: List<User>,
    isAssignedToMe: Boolean,
    editActions: EditActions,
    showAssigneesSelector: () -> Unit,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        // assigned to
        Text(
            text = stringResource(RString.assigned_to),
            style = MaterialTheme.typography.titleMedium
        )
    }

    itemsIndexed(assignees) { index, item ->
        UserItemWithAction(
            user = item,
            onRemoveClick = { editActions.editAssignees.remove(item) },
            onUserItemClick = { navigateToProfile(item.id) }
        )

        if (index < assignees.lastIndex) {
            Spacer(Modifier.height(6.dp))
        }
    }

    // add assignee & loader
    item {
        if (editActions.editAssignees.isLoading) {
            DotsLoader()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            AddButton(
                text = stringResource(RString.add_assignee),
                onClick = { showAssigneesSelector() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            val (@StringRes buttonText: Int, @DrawableRes buttonIcon: Int) = if (isAssignedToMe) {
                RString.unassign to RDrawable.ic_unassigned
            } else {
                RString.assign_to_me to RDrawable.ic_assignee_to_me
            }

            TextButton(
                text = stringResource(buttonText),
                icon = buttonIcon,
                onClick = {
                    if (isAssignedToMe) {
                        editActions.editAssign.remove(Unit)
                    } else {
                        editActions.editAssign.select(Unit)
                    }
                }
            )
        }
    }
}

