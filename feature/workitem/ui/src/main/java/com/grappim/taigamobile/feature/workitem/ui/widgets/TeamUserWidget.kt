package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.uikit.utils.RDrawable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TeamUserWidget(
    user: User,
    goToProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
    createdDateTime: LocalDateTime? = null
) {
    Row(
        modifier = modifier.clickable {
            goToProfile(user.actualId)
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val dateTimeFormatter =
            remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
        val imageSize = if (createdDateTime != null) 46.dp else 40.dp

        AsyncImage(
            modifier = Modifier
                .size(imageSize)
                .clip(CircleShape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(RDrawable.default_avatar),
            error = painterResource(RDrawable.default_avatar),
            model = user.avatarUrl
        )

        Spacer(Modifier.width(6.dp))

        Column {
            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleMedium
            )

            createdDateTime?.let {
                Text(
                    text = it.format(dateTimeFormatter),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun TeamUserWithActionWidget(
    user: User,
    goToProfile: (Long) -> Unit,
    onRemoveUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        TeamUserWidget(
            goToProfile = goToProfile,
            user = user
        )

        IconButton(onClick = onRemoveUserClick) {
            Icon(
                painter = painterResource(RDrawable.ic_remove),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}
