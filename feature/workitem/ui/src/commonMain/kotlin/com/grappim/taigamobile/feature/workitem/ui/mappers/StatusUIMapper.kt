package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticStringColor
import org.koin.core.annotation.Factory

@Factory
class StatusUIMapper {
    fun toUI(statuses: Statuses): StatusUI = StatusUI(
        id = statuses.id,
        title = NativeText.Simple(statuses.name),
        color = StaticStringColor(statuses.color)
    )

    fun toUI(statusFilters: StatusFilters): StatusUI = StatusUI(
        id = statusFilters.id,
        title = NativeText.Simple(statusFilters.name),
        color = StaticStringColor(statusFilters.color)
    )
}
