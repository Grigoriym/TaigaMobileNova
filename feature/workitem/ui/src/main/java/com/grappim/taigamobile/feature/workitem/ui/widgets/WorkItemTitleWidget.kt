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
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget

@Composable
fun WorkItemTitleWidget(titleState: WorkItemTitleState, onTitleSave: () -> Unit, modifier: Modifier = Modifier) {
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
                        .clickable {
                            titleState.setIsTitleEditable(true)
                        },
                    text = titleState.currentTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2
                )
            }
        }
    }
}
