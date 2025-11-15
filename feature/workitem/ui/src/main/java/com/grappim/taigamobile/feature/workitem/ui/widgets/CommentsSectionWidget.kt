package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.MarkdownTextWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitleExpandable
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CommentsSectionWidget(
    comments: ImmutableList<Comment>,
    isCommentsWidgetExpanded: Boolean,
    setIsCommentsWidgetExpanded: (Boolean) -> Unit,
    isCommentsLoading: Boolean,
    onCommentRemove: (Comment) -> Unit,
    goToProfile: (userId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionTitleExpandable(
            text = stringResource(RString.comments_template).format(comments.size),
            isExpanded = isCommentsWidgetExpanded,
            onExpandClick = {
                setIsCommentsWidgetExpanded(!isCommentsWidgetExpanded)
            }
        )

        if (isCommentsWidgetExpanded) {
            comments.forEachIndexed { index, item ->
                CommentItem(
                    comment = item,
                    onDeleteClick = {
                        onCommentRemove(item)
                    },
                    navigateToProfile = goToProfile
                )

                if (index < comments.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isCommentsLoading) {
                DotsLoaderWidget()
            }
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onDeleteClick: () -> Unit,
    navigateToProfile: (userId: Long) -> Unit
) {
    Column {
        var isAlertVisible by remember { mutableStateOf(false) }

        if (isAlertVisible) {
            ConfirmActionDialog(
                title = stringResource(RString.delete_comment_title),
                description = stringResource(RString.delete_comment_text),
                onConfirm = {
                    isAlertVisible = false
                    onDeleteClick()
                },
                onDismiss = { isAlertVisible = false },
                iconId = RDrawable.ic_delete
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            UserItem(
                userDTO = comment.author,
                dateTime = comment.postDateTime,
                onUserItemClick = { navigateToProfile(comment.author.actualId) }
            )

            if (comment.canDelete) {
                IconButton(onClick = { isAlertVisible = true }) {
                    Icon(
                        painter = painterResource(RDrawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        MarkdownTextWidget(
            text = comment.text,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
