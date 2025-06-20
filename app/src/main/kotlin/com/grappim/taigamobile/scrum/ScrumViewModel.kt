package com.grappim.taigamobile.scrum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.sprint.domain.ISprintsRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NothingResult
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
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
    private val tasksRepository: TasksRepository,
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

    val filters = mutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.scrumFilters }
    val stories: Flow<PagingData<CommonTask>> = activeFilters.flatMapLatest { filters ->
        userStoriesRepository.getUserStories(filters)
    }.cachedIn(viewModelScope)

    val createSprintResult = mutableResultFlow<Unit>(NothingResult())

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
        createSprintResult.loadOrError(RString.permission_error) {
            sprintsRepository.createSprint(name, start, end)
//            openSprints.refresh()
        }
    }
}
