package com.grappim.taigamobile.uikit.widgets.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.RDrawable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * User info (name and avatar).
 */
@Composable
fun UserItem(
    displayName: String,
    avatarUrl: Any?,
    modifier: Modifier = Modifier,
    dateTime: LocalDateTime? = null,
    onUserItemClick: () -> Unit = { }
) {
    Row(
        modifier = modifier.clickable { onUserItemClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dateTimeFormatter =
            remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
        val imageSize = if (dateTime != null) 46.dp else 40.dp

        AsyncImage(
            modifier = Modifier
                .size(imageSize)
                .clip(CircleShape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(RDrawable.default_avatar),
            error = painterResource(RDrawable.default_avatar),
            model = avatarUrl
        )

        Spacer(Modifier.width(6.dp))

        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium
            )

            dateTime?.let {
                Text(
                    text = it.format(dateTimeFormatter),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserItemPreview() = TaigaMobileTheme {
    UserItem(
        displayName = "Full Name",
        avatarUrl = null,
        dateTime = LocalDateTime.now()
    )
}
