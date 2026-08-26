package com.grappim.taigamobile.uikit.widgets.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight

// Material3's expanded window-size-class breakpoint — a no-op below it.
private val MaxContentWidth = 840.dp

@Composable
fun TaigaAdaptiveContent(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier.widthIn(max = MaxContentWidth),
            content = content
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun TaigaAdaptiveContentPreview() = TaigaMobilePreviewTheme {
    TaigaAdaptiveContent {
        Text(
            text = "Capped and centered beyond 840dp",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
