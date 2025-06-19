package com.grappim.taigamobile.uikit.widgets.badge

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.utils.ui.textColor

@Composable
fun Badge(text: String, modifier: Modifier = Modifier, isActive: Boolean = true) {
    val color =
        if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraSmall,
        color = color,
        contentColor = color.textColor(),
        tonalElevation = 2.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Preview
@Composable
private fun BadgePreview() = TaigaMobileTheme {
    Row(modifier = Modifier.padding(10.dp)) {
        Badge("1", isActive = false)
        Spacer(Modifier.width(4.dp))
        Badge("12", isActive = true)
    }
}
