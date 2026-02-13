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
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple.WorkItemMultipleAssigneesState
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.AddButtonWidget
import com.grappim.taigamobile.uikit.widgets.button.TaigaTextButtonWidget
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MultipleAssignedToWidget(
    isOffline: Boolean,
    assigneesState: WorkItemMultipleAssigneesState,
    goToProfile: (Long) -> Unit,
    onAssignToMe: () -> Unit,
    onAddAssigneeClick: () -> Unit,
    canModify: Boolean = true
) {
    AssignedToWidget(
        assignees = assigneesState.assignees,
        goToProfile = goToProfile,
        isAssigneesLoading = assigneesState.isAssigneesLoading,
        onRemoveAssigneeClick = { user ->
            assigneesState.onRemoveAssigneeClick(user)
        },
        isAssignedToMe = assigneesState.isAssignedToMe,
        onAssignToMe = onAssignToMe,
        onAddAssigneeClick = onAddAssigneeClick,
        isPlural = true,
        canModify = canModify,
        isOffline = isOffline
    )
}

@Composable
fun SingleAssignedToWidget(
    isOffline: Boolean,
    assigneeState: WorkItemSingleAssigneeState,
    goToProfile: (Long) -> Unit,
    onAssignToMe: () -> Unit,
    onAddAssigneeClick: () -> Unit,
    onUnassign: (() -> Unit)? = null,
    canModify: Boolean = true
) {
    AssignedToWidget(
        assignees = assigneeState.assignees,
        goToProfile = goToProfile,
        isAssigneesLoading = assigneeState.isAssigneesLoading,
        onRemoveAssigneeClick = {
            assigneeState.onRemoveAssigneeClick()
        },
        isAssignedToMe = assigneeState.isAssignedToMe,
        onUnassign = onUnassign,
        onAssignToMe = onAssignToMe,
        onAddAssigneeClick = onAddAssigneeClick,
        canModify = canModify,
        isOffline = isOffline
    )
}

@Composable
private fun AssignedToWidget(
    isOffline: Boolean,
    assignees: ImmutableList<User>,
    isAssigneesLoading: Boolean,
    goToProfile: (Long) -> Unit,
    isAssignedToMe: Boolean,
    onRemoveAssigneeClick: (User) -> Unit,
    onAssignToMe: () -> Unit,
    onAddAssigneeClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlural: Boolean = false,
    onUnassign: (() -> Unit)? = null,
    canModify: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(RString.assigned_to),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(8.dp)

        assignees.forEachIndexed { index, item ->
            TeamUserWithActionWidget(
                user = item,
                goToProfile = {
                    goToProfile(item.actualId)
                },
                onRemoveUserClick = {
                    onRemoveAssigneeClick(item)
                },
                canModify = canModify,
                isOffline = isOffline
            )

            if (index < assignees.lastIndex) {
                Spacer(Modifier.height(6.dp))
            }
        }

        if (isAssigneesLoading) {
            DotsLoaderWidget()
        }

        if (canModify) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                val addText = if (isPlural) {
                    RString.add_assignees
                } else {
                    RString.add_assignee
                }
                AddButtonWidget(
                    text = stringResource(addText),
                    onClick = onAddAssigneeClick,
                    isOffline = isOffline
                )

                if (onUnassign != null || !isAssignedToMe) {
                    Spacer(modifier = Modifier.width(16.dp))

                    val (
                        @StringRes buttonText: Int,
                        @DrawableRes buttonIcon: Int
                    ) = if (isAssignedToMe) {
                        RString.unassign to RDrawable.ic_unassigned
                    } else {
                        RString.assign_to_me to RDrawable.ic_assignee_to_me
                    }

                    TaigaTextButtonWidget(
                        text = stringResource(buttonText),
                        icon = buttonIcon,
                        isOffline = isOffline,
                        onClick = {
                            if (isAssignedToMe) {
                                onUnassign?.invoke()
                            } else {
                                onAssignToMe()
                            }
                        }
                    )
                }
            }
        }
    }
}
