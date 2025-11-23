package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.grappim.taigamobile.core.domain.CommentDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.EditActions
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.MarkdownTextWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitle

@Suppress("FunctionName")
fun LazyListScope.CommonTaskComments(
    commentDTOS: List<CommentDTO>,
    editActions: EditActions,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        SectionTitle(stringResource(RString.comments_template).format(commentDTOS.size))
    }

    itemsIndexed(commentDTOS) { index, item ->
        CommentItem(
            commentDTO = item,
            onDeleteClick = { editActions.editComments.remove(item) },
            navigateToProfile = navigateToProfile
        )

        if (index < commentDTOS.lastIndex) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }

    item {
        if (editActions.editComments.isLoading) {
            DotsLoaderWidget()
        }
    }
}

@Composable
private fun CommentItem(commentDTO: CommentDTO, onDeleteClick: () -> Unit, navigateToProfile: (userId: Long) -> Unit) {
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
                userDTO = commentDTO.author,
                dateTime = commentDTO.postDateTime,
                onUserItemClick = { navigateToProfile(commentDTO.author.actualId) }
            )

            if (commentDTO.canDelete) {
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
            text = commentDTO.text,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
