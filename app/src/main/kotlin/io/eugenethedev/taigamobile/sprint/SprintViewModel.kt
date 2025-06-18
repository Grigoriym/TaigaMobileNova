package io.eugenethedev.taigamobile.sprint

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.Sprint
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.state.postUpdate
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.NothingResult
import io.eugenethedev.taigamobile.ui.utils.loadOrError
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
    private val tasksRepository: ITasksRepository,
    private val sprintsRepository: ISprintsRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<SprintNavDestination>()

    private var _sprintId: Long = route.sprintId
    val sprintId
        get() = _sprintId

    val sprint = MutableResultFlow<Sprint>()
    val statuses = MutableResultFlow<List<Status>>()
    val storiesWithTasks = MutableResultFlow<Map<CommonTask, List<CommonTask>>>()
    val storylessTasks = MutableResultFlow<List<CommonTask>>()
    val issues = MutableResultFlow<List<CommonTask>>()

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
                                    .map { it to async { tasksRepository.getUserStoryTasks(it.id) } }
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

    val editResult = MutableResultFlow<Unit>()
    fun editSprint(name: String, start: LocalDate, end: LocalDate) = viewModelScope.launch {
        editResult.loadOrError(R.string.permission_error) {
            sprintsRepository.editSprint(_sprintId, name, start, end)
            session.sprintEdit.postUpdate()
            loadData().join()
        }
    }

    val deleteResult = MutableResultFlow<Unit>()
    fun deleteSprint() = viewModelScope.launch {
        deleteResult.loadOrError(R.string.permission_error) {
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
