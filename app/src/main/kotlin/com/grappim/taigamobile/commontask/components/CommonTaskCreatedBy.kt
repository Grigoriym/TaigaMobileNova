package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.list.UserItem

@Suppress("FunctionName")
fun LazyListScope.CommonTaskCreatedBy(
    creator: UserDTO,
    commonTask: CommonTaskExtended,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        Text(
            text = stringResource(RString.created_by),
            style = MaterialTheme.typography.titleMedium
        )

        UserItem(
            displayName = creator.displayName,
            avatarUrl = creator.photo,
            dateTime = commonTask.createdDateTime,
            onUserItemClick = { navigateToProfile(creator.actualId) }
        )
    }
}
