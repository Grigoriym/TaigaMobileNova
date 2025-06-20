package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
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
import com.grappim.taigamobile.commontask.EditActions
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.MarkdownText
import com.grappim.taigamobile.uikit.widgets.text.SectionTitle

@Suppress("FunctionName")
fun LazyListScope.CommonTaskComments(
    comments: List<Comment>,
    editActions: EditActions,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        SectionTitle(stringResource(RString.comments_template).format(comments.size))
    }

    itemsIndexed(comments) { index, item ->
        CommentItem(
            comment = item,
            onDeleteClick = { editActions.editComments.remove(item) },
            navigateToProfile = navigateToProfile
        )

        if (index < comments.lastIndex) {
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    item {
        if (editActions.editComments.isLoading) {
            DotsLoader()
        }
    }
}

@Composable
private fun CommentItem(
    comment: Comment,
    onDeleteClick: () -> Unit,
    navigateToProfile: (userId: Long) -> Unit
) = Column {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.delete_comment_title),
            text = stringResource(RString.delete_comment_text),
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
            user = comment.author,
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

    MarkdownText(
        text = comment.text,
        modifier = Modifier.padding(start = 4.dp)
    )
}
