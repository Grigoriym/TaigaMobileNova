package com.grappim.taigamobile.scrum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.R
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.sprint.ISprintsRepository
import com.grappim.taigamobile.state.Session
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.NothingResult
import com.grappim.taigamobile.ui.utils.loadOrError
import com.grappim.taigamobile.userstories.UserStoriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScrumViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val sprintsRepository: ISprintsRepository,
    private val session: Session,
    private val userStoriesRepository: UserStoriesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScrumState(
            setIsCreateSprintDialogVisible = ::setIsCreateSprintDialogVisible
        )
    )
    val state = _state.asStateFlow()

    private var shouldReload = true

    val openSprints = sprintsRepository.getSprints(isClosed = false)
        .cachedIn(viewModelScope)

    val closedSprints = sprintsRepository.getSprints(isClosed = true)
        .cachedIn(viewModelScope)

    val filters = MutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.scrumFilters }
    val stories: Flow<PagingData<CommonTask>> = activeFilters.flatMapLatest { filters ->
        userStoriesRepository.getUserStories(filters)
    }.cachedIn(viewModelScope)

    val createSprintResult = MutableResultFlow<Unit>(NothingResult())

    // TODO handle refresh
    init {
        session.currentProjectId.onEach {
            createSprintResult.value = NothingResult()
//            stories.refresh()
//            openSprints.refresh()
//            closedSprints.refresh()
            shouldReload = true
        }.launchIn(viewModelScope)

        session.taskEdit.onEach {
//            stories.refresh()
//            openSprints.refresh()
//            closedSprints.refresh()
        }.launchIn(viewModelScope)

        session.sprintEdit.onEach {
//            openSprints.refresh()
//            closedSprints.refresh()
        }.launchIn(viewModelScope)
    }

    private fun setIsCreateSprintDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(
                isCreateSprintDialogVisible = newValue
            )
        }
    }

    fun onOpen() {
        if (!shouldReload) return
        viewModelScope.launch {
            filters.loadOrError {
                tasksRepository.getFiltersData(
                    commonTaskType = CommonTaskType.UserStory,
                    isCommonTaskFromBacklog = true
                )
            }

            filters.value.data?.let {
                session.changeScrumFilters(activeFilters.value.updateData(it))
            }
        }
        shouldReload = false
    }

    fun selectFilters(filters: FiltersData) {
        session.changeScrumFilters(filters)
    }

    fun createSprint(name: String, start: LocalDate, end: LocalDate) = viewModelScope.launch {
        createSprintResult.loadOrError(R.string.permission_error) {
            sprintsRepository.createSprint(name, start, end)
//            openSprints.refresh()
        }
    }
}
