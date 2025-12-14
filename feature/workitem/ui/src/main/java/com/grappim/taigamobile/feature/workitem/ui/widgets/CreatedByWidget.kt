package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import java.time.LocalDateTime

@Composable
fun CreatedByWidget(
    creator: User?,
    createdDateTime: LocalDateTime?,
    goToProfile: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (creator != null) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text(
                text = stringResource(RString.created_by),
                style = MaterialTheme.typography.titleMedium
            )
            TaigaHeightSpacer(8.dp)

            TeamUserWidget(
                goToProfile = goToProfile,
                user = creator,
                createdDateTime = createdDateTime
            )
        }
    }
}
