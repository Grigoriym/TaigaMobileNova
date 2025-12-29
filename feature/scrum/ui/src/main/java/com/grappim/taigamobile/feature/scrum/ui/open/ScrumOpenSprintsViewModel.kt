package com.grappim.taigamobile.feature.scrum.ui.open

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegateImpl
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScrumOpenSprintsViewModel @Inject constructor(
    private val sprintsRepository: SprintsRepository,
    dateTimeUtils: DateTimeUtils
) : ViewModel(),
    WorkItemSprintDelegate by WorkItemSprintDelegateImpl(
        dateTimeUtils = dateTimeUtils,
        sprintsRepository = sprintsRepository
    ) {

    private val _state = MutableStateFlow(
        ScrumOpenSprintsState(
            onCreateSprintConfirm = ::createSprint,
            onCreateSprintClick = ::onCreateSprintClick
        )
    )
    val state = _state.asStateFlow()

    private val _reloadOpenSprints = Channel<Unit>()
    val reloadOpenSprints = _reloadOpenSprints.receiveAsFlow()

    val openSprints = sprintsRepository.getSprints(isClosed = false)
        .cachedIn(viewModelScope)

    private fun onCreateSprintClick() {
        setInitialSprint()
        setSprintDialogVisibility(true)
    }

    private fun createSprint() {
        viewModelScope.launch {
            createSprint(
                doOnPreExecute = {
                    _state.update {
                        it.copy(isLoading = true)
                    }
                },
                doOnSuccess = {
                    _state.update {
                        it.copy(isLoading = false)
                    }
                    _reloadOpenSprints.send(Unit)
                },
                doOnError = {
                    _state.update {
                        it.copy(isLoading = false)
                    }
                }
            )
        }
    }
}

data class ScrumOpenSprintsState(
    val isLoading: Boolean = false,
    val onCreateSprintConfirm: () -> Unit = {},
    val onCreateSprintClick: () -> Unit = {}
)
