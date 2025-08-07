package com.grappim.taigamobile.feature.workitem.ui.widgets

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.LocalFilePicker
import com.grappim.taigamobile.uikit.R
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.SectionTitleExpandable
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber

@Composable
fun AttachmentsSectionWidget(
    isAttachmentsLoading: Boolean,
    attachments: ImmutableList<Attachment>,
    onAttachmentAdd: (uri: Uri?) -> Unit,
    onAttachmentRemove: (Attachment) -> Unit,
    setAreAttachmentsExpanded: (Boolean) -> Unit,
    areAttachmentsExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val filePicker = LocalFilePicker.current
    Column(modifier = modifier) {
        SectionTitleExpandable(
            text = stringResource(RString.attachments_template).format(attachments.size),
            isExpanded = areAttachmentsExpanded,
            onExpandClick = {
                setAreAttachmentsExpanded(!areAttachmentsExpanded)
            }
        )

        if (areAttachmentsExpanded) {
            Spacer(Modifier.height(10.dp))

            attachments.forEachIndexed { index, item ->
                AttachmentItem(
                    attachment = item,
                    onRemoveClick = {
                        onAttachmentRemove(item)
                    }
                )

                if (index < attachments.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            if (isAttachmentsLoading) {
                DotsLoader()
            } else {
                Button(
                    onClick = {
                        filePicker.requestFile { uri ->
                            onAttachmentAdd(uri)
                        }
                    }
                ) {
                    Text(text = stringResource(RString.add_attachment))
                }
            }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
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
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
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

@Composable
@PreviewDarkLight
private fun AttachmentItemPreview() {
    TaigaMobileTheme {
        AttachmentItem(
            attachment = Attachment(
                id = 1L,
                name = "file 1 rqwr eqw qw fw ert",
                sizeInBytes = 123235L,
                url = ""
            ),
            onRemoveClick = {}
        )
    }
}
