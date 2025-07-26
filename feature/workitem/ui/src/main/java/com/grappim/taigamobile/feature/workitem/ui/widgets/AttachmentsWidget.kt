package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.R
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.SectionTitle
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber
import java.io.InputStream

fun LazyListScope.attachmentsSectionWidget(
    isAttachmentsLoading: Boolean,
    attachments: ImmutableList<Attachment>,
    onAttachmentAdd: (fileName: String, inputStream: InputStream) -> Unit,
    onAttachmentRemove: (Attachment) -> Unit
) {
    item {
        val filePicker = LocalFilePicker.current
        SectionTitle(
            text = stringResource(RString.attachments_template).format(attachments.size),
            onAddClick = {
                filePicker.requestFile { file, stream ->
                    onAttachmentAdd(file, stream)
                }
            }
        )
    }

    items(attachments) {
        AttachmentItem(
            attachment = it,
            onRemoveClick = {
                onAttachmentRemove(it)
            }
        )
    }

    item {
        if (isAttachmentsLoading) {
            DotsLoader()
        }
    }
}

@Composable
private fun AttachmentItem(attachment: Attachment, onRemoveClick: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    var isAlertVisible by rememberSaveable { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.remove_attachment_title),
            description = stringResource(RString.remove_attachment_text),
            onConfirm = {
                isAlertVisible = false
                onRemoveClick()
            },
            onDismiss = { isAlertVisible = false },
            iconId = R.drawable.ic_remove
        )
    }

    Card {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_attachment),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(end = 2.dp)
            )
            TextButton(
                modifier = Modifier.weight(1f, fill = true),
                onClick = {
                    try {
                        uriHandler.openUri(attachment.url)
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e)
                    }
                }
            ) {
                Text(
                    text = attachment.name,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                onClick = { isAlertVisible = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
