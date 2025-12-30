package com.grappim.taigamobile.feature.scrum.ui.closed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScrumClosedSprintsViewModel @Inject constructor(sprintsRepository: SprintsRepository) : ViewModel() {

    val closedSprints = sprintsRepository.getSprintsPaging(isClosed = true)
        .cachedIn(viewModelScope)
}
