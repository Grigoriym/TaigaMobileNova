package io.eugenethedev.taigamobile.ui.components.buttons

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.R

/**
 * Text button with plus icon on the left
 */
@Composable
fun AddButton(
    text: String,
    onClick: () -> Unit
) = FilledTonalButton(onClick = onClick) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = null
        )

        Spacer(Modifier.width(6.dp))

        Text(text)
    }
}

