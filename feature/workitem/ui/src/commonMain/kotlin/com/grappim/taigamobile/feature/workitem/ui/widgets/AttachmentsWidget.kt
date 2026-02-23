package com.grappim.taigamobile.feature.workitem.ui.widgets

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.add_attachment
import com.grappim.taigamobile.strings.generated.resources.attachments_template
import com.grappim.taigamobile.strings.generated.resources.remove_attachment_text
import com.grappim.taigamobile.strings.generated.resources.remove_attachment_title
import com.grappim.taigamobile.uikit.generated.resources.ic_attachment
import com.grappim.taigamobile.uikit.generated.resources.ic_delete
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitleExpandable
import com.grappim.taigamobile.utils.ui.formatStringKmp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AttachmentsSectionWidget(
    isOffline: Boolean,
    attachmentsState: WorkItemAttachmentsState,
    onAttachmentAdd: (file: PlatformFile?) -> Unit,
    onAttachmentRemove: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = false
) {
    val singleLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Single,
        type = FileKitType.File()
    ) { file ->
        onAttachmentAdd(file)
    }

    Column(modifier = modifier) {
        SectionTitleExpandable(
            text = stringResource(RString.attachments_template).formatStringKmp(attachmentsState.attachments.size),
            isExpanded = attachmentsState.areAttachmentsExpanded,
            onExpandClick = {
                attachmentsState.setAreAttachmentsExpanded(!attachmentsState.areAttachmentsExpanded)
            }
        )

        if (attachmentsState.areAttachmentsExpanded) {
            Spacer(Modifier.height(10.dp))

            attachmentsState.attachments.forEachIndexed { index, item ->
                AttachmentItem(
                    attachment = item,
                    onRemoveClick = {
                        onAttachmentRemove(item)
                    },
                    canModify = canModify,
                    isOffline = isOffline
                )

                if (index < attachmentsState.attachments.lastIndex) {
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            if (attachmentsState.areAttachmentsLoading) {
                DotsLoaderWidget()
            } else if (canModify) {
                Button(
                    enabled = !isOffline,
                    onClick = {
                        singleLauncher.launch()
                    }
                ) {
                    Text(text = stringResource(RString.add_attachment))
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    attachment: Attachment,
    isOffline: Boolean,
    onRemoveClick: () -> Unit,
    canModify: Boolean = false
) {
    val uriHandler = LocalUriHandler.current

    var isAlertVisible by rememberSaveable { mutableStateOf(false) }

    ConfirmActionDialog(
        title = stringResource(RString.remove_attachment_title),
        description = stringResource(RString.remove_attachment_text),
        onConfirm = {
            isAlertVisible = false
            onRemoveClick()
        },
        onDismiss = { isAlertVisible = false },
        isVisible = isAlertVisible
    )

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
                painter = painterResource(RDrawable.ic_attachment),
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
                        logcat(throwable = e) {
                            "Can't open uri ${attachment.url}"
                        }
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

            if (canModify) {
                IconButton(
                    enabled = !isOffline,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    onClick = { isAlertVisible = true }
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
@PreviewTaigaDarkLight
private fun AttachmentsSectionWidgetExpandedPreview() {
    TaigaMobilePreviewTheme {
        AttachmentsSectionWidget(
            attachmentsState = WorkItemAttachmentsState(
                areAttachmentsExpanded = true,
                attachments = persistentListOf(
                    Attachment(
                        id = 2965,
                        name = "Giovanni Nixon",
                        sizeInBytes = 5351,
                        url = "https://search.yahoo.com/search?p=consul"
                    ),
                    Attachment(
                        id = 5175,
                        name = "Priscilla Garner",
                        sizeInBytes = 9863,
                        url = "https://duckduckgo.com/?q=detracto"
                    )
                )
            ),
            onAttachmentAdd = {},
            onAttachmentRemove = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun AttachmentsSectionWidgetPreview() {
    TaigaMobilePreviewTheme {
        AttachmentsSectionWidget(
            attachmentsState = WorkItemAttachmentsState(
                areAttachmentsExpanded = false,
                attachments = persistentListOf()
            ),
            onAttachmentAdd = {},
            onAttachmentRemove = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun AttachmentItemPreview() {
    TaigaMobilePreviewTheme {
        AttachmentItem(
            attachment = Attachment(
                id = 1L,
                name = "file 1 rqwr eqw qw fw ert",
                sizeInBytes = 123235L,
                url = ""
            ),
            onRemoveClick = {},
            isOffline = false
        )
    }
}
