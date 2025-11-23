package com.grappim.taigamobile.feature.workitem.ui.models

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticStringColor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class StatusUIMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {
    suspend fun toUI(statuses: Statuses): StatusUI = withContext(ioDispatcher) {
        StatusUI(
            id = statuses.id,
            title = NativeText.Simple(statuses.name),
            color = StaticStringColor(statuses.color)
        )
    }
}
