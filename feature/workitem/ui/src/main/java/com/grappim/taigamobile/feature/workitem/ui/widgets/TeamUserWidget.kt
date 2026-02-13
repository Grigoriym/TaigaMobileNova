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
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
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
    isOffline: Boolean,
    user: User,
    goToProfile: (Long) -> Unit,
    onRemoveUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    canModify: Boolean = true
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

        if (canModify) {
            IconButton(
                onClick = onRemoveUserClick,
                enabled = !isOffline
            ) {
                Icon(
                    painter = painterResource(RDrawable.ic_remove),
                    contentDescription = null
                )
            }
        }
    }
}

private fun previewUser() = User(
    id = 1L,
    fullName = "John Doe",
    photo = null,
    bigPhoto = null,
    username = "johndoe"
)

@Composable
@PreviewTaigaDarkLight
private fun TeamUserWidgetPreview() {
    TaigaMobilePreviewTheme {
        TeamUserWidget(
            user = previewUser(),
            goToProfile = {}
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun TeamUserWidgetWithDateTimePreview() {
    TaigaMobilePreviewTheme {
        TeamUserWidget(
            user = previewUser(),
            goToProfile = {},
            createdDateTime = LocalDateTime.of(2024, 1, 15, 14, 30)
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun TeamUserWithActionWidgetPreview() {
    TaigaMobilePreviewTheme {
        TeamUserWithActionWidget(
            user = previewUser(),
            goToProfile = {},
            onRemoveUserClick = {},
            canModify = true,
            isOffline = false
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun TeamUserWithActionWidgetOfflinePreview() {
    TaigaMobilePreviewTheme {
        TeamUserWithActionWidget(
            user = previewUser(),
            goToProfile = {},
            onRemoveUserClick = {},
            canModify = true,
            isOffline = true
        )
    }
}

@Composable
@PreviewTaigaDarkLight
private fun TeamUserWithActionWidgetNoModifyPreview() {
    TaigaMobilePreviewTheme {
        TeamUserWithActionWidget(
            user = previewUser(),
            goToProfile = {},
            onRemoveUserClick = {},
            canModify = false,
            isOffline = false
        )
    }
}
