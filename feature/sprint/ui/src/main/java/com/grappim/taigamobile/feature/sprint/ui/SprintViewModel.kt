package com.grappim.taigamobile.feature.sprint.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.TasksRepository
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.postUpdate
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NothingResult
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class SprintViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val sprintsRepository: SprintsRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SprintNavDestination>()

    private var _sprintId: Long = route.sprintId
    val sprintId
        get() = _sprintId

    val sprint = mutableResultFlow<Sprint>()
    val statuses = mutableResultFlow<List<Status>>()
    val storiesWithTasks = mutableResultFlow<Map<CommonTask, List<CommonTask>>>()
    val storylessTasks = mutableResultFlow<List<CommonTask>>()
    val issues = mutableResultFlow<List<CommonTask>>()

    private var shouldReload = true

    fun onOpen() {
        if (!shouldReload) return
        loadData(isReloading = false)
        shouldReload = false
    }

    private fun loadData(isReloading: Boolean = true) = viewModelScope.launch {
        sprint.loadOrError(showLoading = !isReloading) {
            sprintsRepository.getSprint(_sprintId).also {
                joinAll(
                    launch {
                        statuses.loadOrError(showLoading = false) {
                            tasksRepository.getStatuses(
                                CommonTaskType.Task
                            )
                        }
                    },
                    launch {
                        storiesWithTasks.loadOrError(showLoading = false) {
                            coroutineScope {
                                sprintsRepository.getSprintUserStories(_sprintId)
                                    .map {
                                        it to async { tasksRepository.getUserStoryTasks(it.id) }
                                    }
                                    .associate { (story, tasks) -> story to tasks.await() }
                            }
                        }
                    },
                    launch {
                        issues.loadOrError(showLoading = false) {
                            sprintsRepository.getSprintIssues(
                                _sprintId
                            )
                        }
                    },
                    launch {
                        storylessTasks.loadOrError(showLoading = false) {
                            sprintsRepository.getSprintTasks(
                                _sprintId
                            )
                        }
                    }
                )
            }
        }
    }

    val editResult = mutableResultFlow<Unit>()
    fun editSprint(name: String, start: LocalDate, end: LocalDate) = viewModelScope.launch {
        editResult.loadOrError(RString.permission_error) {
            sprintsRepository.editSprint(_sprintId, name, start, end)
            session.sprintEdit.postUpdate()
            loadData().join()
        }
    }

    val deleteResult = mutableResultFlow<Unit>()
    fun deleteSprint() = viewModelScope.launch {
        deleteResult.loadOrError(RString.permission_error) {
            sprintsRepository.deleteSprint(_sprintId)
            session.sprintEdit.postUpdate()
        }
    }

    init {
        session.taskEdit.onEach {
            _sprintId = -1
            sprint.value = NothingResult()
            statuses.value = NothingResult()
            storiesWithTasks.value = NothingResult()
            storylessTasks.value = NothingResult()
            issues.value = NothingResult()
            shouldReload = true
        }.launchIn(viewModelScope)
    }
}
