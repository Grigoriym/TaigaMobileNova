package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.AddButtonWidget
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.ImmutableList

@Composable
fun WorkItemEpicInfoWidget(
    canModifyRelatedEpic: Boolean,
    areUserStoryEpicsLoading: Boolean,
    userStoryEpics: ImmutableList<UserStoryEpic>,
    modifier: Modifier = Modifier,
    onEpicClick: (UserStoryEpic) -> Unit = {},
    onEpicRemoveClick: (UserStoryEpic) -> Unit = {},
    onLinkToEpicClick: () -> Unit = {},
    isOffline: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        TaigaHeightSpacer(4.dp)

        if (userStoryEpics.isNotEmpty()) {
            Text(
                text = stringResource(RString.user_story_belongs_to),
                style = MaterialTheme.typography.bodySmall
            )

            TaigaHeightSpacer(4.dp)

            userStoryEpics.forEachIndexed { _, epic ->
                EpicInfoWidget(
                    canModifyRelatedEpic = canModifyRelatedEpic,
                    userStoryEpic = epic,
                    onEpicClick = onEpicClick,
                    onEpicRemoveClick = onEpicRemoveClick
                )
            }

            TaigaHeightSpacer(2.dp)
        }

        if (areUserStoryEpicsLoading) {
            DotsLoaderWidget()
            TaigaHeightSpacer(2.dp)
        }

        if (canModifyRelatedEpic) {
            AddButtonWidget(
                text = stringResource(RString.link_to_epic),
                onClick = onLinkToEpicClick,
                isOffline = isOffline
            )
        }
    }
}

@Composable
private fun EpicInfoWidget(
    canModifyRelatedEpic: Boolean,
    userStoryEpic: UserStoryEpic,
    onEpicClick: (UserStoryEpic) -> Unit = {},
    onEpicRemoveClick: (UserStoryEpic) -> Unit = {}
) {
    var isAlertVisible by remember { mutableStateOf(false) }

    ConfirmActionDialog(
        title = stringResource(RString.unlink_epic_title),
        description = stringResource(RString.unlink_epic_text),
        onConfirm = {
            isAlertVisible = false
            onEpicRemoveClick(userStoryEpic)
        },
        onDismiss = { isAlertVisible = false },
        isVisible = isAlertVisible
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        color = MaterialTheme.colorScheme.surface,
        onClick = {
            onEpicClick(userStoryEpic)
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .weight(1f),
                text = buildAnnotatedString {
                    append(
                        stringResource(RString.title_with_ref_pattern).format(
                            userStoryEpic.ref,
                            userStoryEpic.title
                        )
                    )

                    append(" ")
                    pushStyle(SpanStyle(color = userStoryEpic.color.toColor()))
                    append("⬤")
                    pop()
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            if (canModifyRelatedEpic) {
                IconButton(
                    onClick = { isAlertVisible = true }
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_remove),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
