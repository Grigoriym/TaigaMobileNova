package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.text.MarkdownTextWidget

@Composable
fun WorkItemDescriptionWidget(
    currentDescription: String?,
    onDescriptionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onDescriptionClick()
            }
    ) {
        TaigaHeightSpacer(8.dp)

        Text(
            text = stringResource(RString.description_hint),
            style = MaterialTheme.typography.bodySmall
        )

        TaigaHeightSpacer(4.dp)

        if (currentDescription?.isNotEmpty() == true) {
            MarkdownTextWidget(
                modifier = Modifier
                    .fillMaxSize(),
                text = currentDescription,
                isSelectable = false
            )
        } else {
            Text(
                text = stringResource(RString.add_description),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        TaigaHeightSpacer(8.dp)
    }
}
