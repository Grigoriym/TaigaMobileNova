package io.eugenethedev.taigamobile.ui.screens.scrum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.domain.paging.CommonPagingSource
import io.eugenethedev.taigamobile.domain.repositories.ISprintsRepository
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.NothingResult
import io.eugenethedev.taigamobile.ui.utils.asLazyPagingItems
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScrumViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val sprintsRepository: ISprintsRepository,
    private val session: Session
) : ViewModel() {
    val projectName by lazy { session.currentProjectName }

    private var shouldReload = true

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

    // stories

    val filters = MutableResultFlow<FiltersData>()
    val activeFilters by lazy { session.scrumFilters }

    @OptIn(ExperimentalCoroutinesApi::class)
    val stories by lazy {
        activeFilters.flatMapLatest { filters ->
            Pager(PagingConfig(CommonPagingSource.PAGE_SIZE, enablePlaceholders = false)) {
                CommonPagingSource { tasksRepository.getBacklogUserStories(it, filters) }
            }.flow
        }.asLazyPagingItems(viewModelScope)
    }

    fun selectFilters(filters: FiltersData) {
        session.changeScrumFilters(filters)
    }

    // sprints

    val openSprints by sprints(isClosed = false)
    val closedSprints by sprints(isClosed = true)

    private fun sprints(isClosed: Boolean) = lazy {
        Pager(PagingConfig(CommonPagingSource.PAGE_SIZE)) {
            CommonPagingSource { sprintsRepository.getSprints(it, isClosed) }
        }.flow.asLazyPagingItems(viewModelScope)
    }

    val createSprintResult = MutableResultFlow<Unit>(NothingResult())

    fun createSprint(name: String, start: LocalDate, end: LocalDate) = viewModelScope.launch {
        createSprintResult.loadOrError(R.string.permission_error) {
            sprintsRepository.createSprint(name, start, end)
            openSprints.refresh()
        }
    }

    init {
        session.currentProjectId.onEach {
            createSprintResult.value = NothingResult()
            stories.refresh()
            openSprints.refresh()
            closedSprints.refresh()
            shouldReload = true
        }.launchIn(viewModelScope)

        session.taskEdit.onEach {
            stories.refresh()
            openSprints.refresh()
            closedSprints.refresh()
        }.launchIn(viewModelScope)

        session.sprintEdit.onEach {
            openSprints.refresh()
            closedSprints.refresh()
        }.launchIn(viewModelScope)
    }
}
