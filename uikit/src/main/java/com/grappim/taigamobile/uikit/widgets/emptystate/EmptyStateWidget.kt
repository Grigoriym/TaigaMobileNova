package com.grappim.taigamobile.uikit.widgets.emptystate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString

@Composable
fun EmptyStateWidget(
    modifier: Modifier = Modifier,
    message: NativeText = NativeText.Resource(RString.no_items_yet),
    icon: ImageVector? = null,
    action: EmptyStateAction? = null
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
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = message.asString(context),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline
            )

            if (action != null) {
                Button(
                    onClick = action.onClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(text = action.label.asString(context))
                }
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun EmptyStateWidgetPreview() = TaigaMobileTheme {
    EmptyStateWidget()
}

@PreviewTaigaDarkLight
@Composable
private fun EmptyStateWidgetWithActionPreview() = TaigaMobileTheme {
    EmptyStateWidget(
        message = NativeText.Simple("No team members"),
        action = EmptyStateAction(
            label = NativeText.Simple("Add member"),
            onClick = {}
        )
    )
}
