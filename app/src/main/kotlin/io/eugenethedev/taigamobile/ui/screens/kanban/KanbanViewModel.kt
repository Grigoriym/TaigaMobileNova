package io.eugenethedev.taigamobile.ui.screens.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.CommonTaskExtended
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.Swimlane
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.domain.repositories.ITasksRepository
import io.eugenethedev.taigamobile.domain.repositories.IUsersRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.state.subscribeToAll
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.NothingResult
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val usersRepository: IUsersRepository,
    private val session: Session
) : ViewModel() {

    val projectName by lazy { session.currentProjectName }

    val statuses = MutableResultFlow<List<Status>>()
    val team = MutableResultFlow<List<User>>()
    val stories = MutableResultFlow<List<CommonTaskExtended>>()
    val swimlanes = MutableResultFlow<List<Swimlane?>>()

    val selectedSwimlane = MutableStateFlow<Swimlane?>(null)

    private var shouldReload = true

    fun onOpen() = viewModelScope.launch {
        if (!shouldReload) return@launch
        joinAll(
            launch {
                statuses.loadOrError(preserveValue = false) {
                    tasksRepository.getStatuses(
                        CommonTaskType.UserStory
                    )
                }
            },
            launch {
                team.loadOrError(preserveValue = false) {
                    usersRepository.getTeam().map { it.toUser() }
                }
            },
            launch {
                stories.loadOrError(preserveValue = false) { tasksRepository.getAllUserStories() }
            },
            launch {
                swimlanes.loadOrError {
                    listOf(null) + tasksRepository.getSwimlanes() // prepend null to show "unclassified" swimlane
                }
            }
        )
        shouldReload = false
    }

    fun selectSwimlane(swimlane: Swimlane?) {
        selectedSwimlane.value = swimlane
    }

    init {
        viewModelScope.subscribeToAll(session.currentProjectId, session.taskEdit) {
            statuses.value = NothingResult()
            team.value = NothingResult()
            stories.value = NothingResult()
            swimlanes.value = NothingResult()
            shouldReload = true
        }
    }
}
