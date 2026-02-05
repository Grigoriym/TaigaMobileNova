package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget

@Composable
fun WorkItemTitleWidget(
    isOffline: Boolean,
    titleState: WorkItemTitleState,
    onTitleSave: () -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopStart
    ) {
        if (titleState.isTitleEditable) {
            Column {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = titleState.currentTitle,
                    onValueChange = titleState.onTitleChange,
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        disabledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = titleState.onCancelClick) {
                        Text(stringResource(RString.cancel))
                    }
                    TextButton(
                        onClick = {
                            onTitleSave()
                        }
                    ) {
                        Text(stringResource(RString.save))
                    }
                    if (titleState.isTitleLoading) {
                        CircularLoaderWidget(modifier = Modifier.size(40.dp))
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (canModify && !isOffline) {
                                Modifier.clickable {
                                    titleState.setIsTitleEditable(true)
                                }
                            } else {
                                Modifier
                            }
                        ),
                    text = titleState.currentTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemTitleWidgetDisplayPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTitleWidget(
            titleState = WorkItemTitleState(
                currentTitle = "Implement user authentication flow",
                isTitleEditable = false
            ),
            onTitleSave = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemTitleWidgetEditablePreview() {
    TaigaMobilePreviewTheme {
        WorkItemTitleWidget(
            titleState = WorkItemTitleState(
                currentTitle = "Implement user authentication flow",
                isTitleEditable = true,
                onTitleChange = {},
                onCancelClick = {},
                setIsTitleEditable = {}
            ),
            onTitleSave = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemTitleWidgetLoadingPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTitleWidget(
            titleState = WorkItemTitleState(
                currentTitle = "Implement user authentication flow",
                isTitleEditable = true,
                isTitleLoading = true,
                onTitleChange = {},
                onCancelClick = {},
                setIsTitleEditable = {}
            ),
            onTitleSave = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemTitleWidgetOfflinePreview() {
    TaigaMobilePreviewTheme {
        WorkItemTitleWidget(
            titleState = WorkItemTitleState(
                currentTitle = "Implement user authentication flow",
                isTitleEditable = false
            ),
            onTitleSave = {},
            canModify = true,
            isOffline = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun WorkItemTitleWidgetNoModifyPreview() {
    TaigaMobilePreviewTheme {
        WorkItemTitleWidget(
            titleState = WorkItemTitleState(
                currentTitle = "Implement user authentication flow",
                isTitleEditable = false
            ),
            onTitleSave = {},
            canModify = false,
            isOffline = false
        )
    }
}
