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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.uikit.generated.resources.default_avatar
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewUtils
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.formatter.datetime.platformFormatMediumDateTime
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource

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
                    text = platformFormatMediumDateTime(it),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserItemPreview() = TaigaMobilePreviewTheme {
    UserItem(
        displayName = "Full Name",
        avatarUrl = null,
        dateTime = PreviewUtils.getNowDateTime()
    )
}
