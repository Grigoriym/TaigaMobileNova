package com.grappim.taigamobile.uikit

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.SectionTitle
import com.grappim.taigamobile.utils.ui.activity
import java.io.InputStream

@Suppress("FunctionName")
fun LazyListScope.Attachments(
    attachmentDTOS: List<AttachmentDTO>,
    editAttachments: EditAction<Pair<String, InputStream>, AttachmentDTO>
) {
    item {
        val filePicker = LocalFilePickerOld.current
        SectionTitle(
            text = stringResource(RString.attachments_template).format(attachmentDTOS.size),
            onAddClick = {
                filePicker.requestFile { file, stream -> editAttachments.select(file to stream) }
            }
        )
    }

    items(attachmentDTOS) {
        AttachmentItem(
            attachmentDTO = it,
            onRemoveClick = { editAttachments.remove(it) }
        )
    }

    item {
        if (editAttachments.isLoading) {
            DotsLoaderWidget()
        }
    }
}

@Composable
private fun AttachmentItem(attachmentDTO: AttachmentDTO, onRemoveClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        var isAlertVisible by remember { mutableStateOf(false) }

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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 4.dp)
        ) {
            val activity = LocalContext.current.activity
            Icon(
                painter = painterResource(R.drawable.ic_attachment),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(end = 2.dp)
            )

            Text(
                text = attachmentDTO.name,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    activity.startActivity(Intent(Intent.ACTION_VIEW, attachmentDTO.url.toUri()))
                }
            )
        }

        IconButton(
            onClick = { isAlertVisible = true },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
