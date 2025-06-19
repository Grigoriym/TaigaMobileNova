package com.grappim.taigamobile.commontask.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.ui.components.lists.UserItem

@Suppress("FunctionName")
fun LazyListScope.CommonTaskCreatedBy(
    creator: User,
    commonTask: CommonTaskExtended,
    navigateToProfile: (userId: Long) -> Unit
) {
    item {
        Text(
            text = stringResource(RString.created_by),
            style = MaterialTheme.typography.titleMedium
        )

        UserItem(
            user = creator,
            dateTime = commonTask.createdDateTime,
            onUserItemClick = { navigateToProfile(creator.actualId) }
        )
    }
}
