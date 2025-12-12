package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString

@Composable
fun ErrorStateWidget(
    modifier: Modifier = Modifier,
    message: NativeText = NativeText.Resource(RString.error_loading_data),
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = message.asString(context),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = onRetry
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = NativeText.Resource(RString.retry).asString(context),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@PreviewDarkLight
@Composable
private fun ErrorStateWidgetPreview() = TaigaMobileTheme {
    ErrorStateWidget()
}
