package com.grappim.taigamobile.feature.scrum.ui.closed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScrumClosedSprintsViewModel(sprintsRepository: SprintsRepository) : ViewModel() {

    val closedSprints = sprintsRepository.getSprintsPaging(isClosed = true)
        .cachedIn(viewModelScope)
}
