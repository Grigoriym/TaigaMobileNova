package io.eugenethedev.taigamobile.ui.components.lists

import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.ui.components.dialogs.ConfirmActionDialog
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.utils.clickableUnindicated
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * User info (name and avatar).
 */
@OptIn(ExperimentalCoilApi::class)
@Composable
fun UserItem(
    user: User,
    dateTime: LocalDateTime? = null,
    onUserItemClick: () -> Unit = { }
) = Row(
    modifier = Modifier.clickableUnindicated { onUserItemClick() },
    verticalAlignment = Alignment.CenterVertically
) {
    val dateTimeFormatter = remember { DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM) }
    val imageSize = if (dateTime != null) 46.dp else 40.dp

    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
            .data(user.avatarUrl ?: R.drawable.default_avatar).apply(
            fun ImageRequest.Builder.() {
                error(R.drawable.default_avatar)
                crossfade(true)
            }).build()
        ),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(imageSize)
            .clip(CircleShape)
    )

    Spacer(Modifier.width(6.dp))

    Column {
        Text(
            text = user.displayName,
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

@Composable
fun UserItemWithAction(
    user: User,
    onRemoveClick: () -> Unit,
    onUserItemClick: () -> Unit = { }
) {
    var isAlertVisible by remember { mutableStateOf(false) }

    if (isAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(R.string.remove_user_title),
            text = stringResource(R.string.remove_user_text),
            onConfirm = {
                isAlertVisible = false
                onRemoveClick()
            },
            onDismiss = { isAlertVisible = false },
            iconId = R.drawable.ic_remove
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        UserItem(
            user = user,
            onUserItemClick = onUserItemClick
        )

        IconButton(onClick = { isAlertVisible = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_remove),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserItemPreview() = TaigaMobileTheme {
    UserItem(
        user = User(
            _id = 0L,
            fullName = "Full Name",
            photo = null,
            bigPhoto = null,
            username = "username"
        )
    )
}