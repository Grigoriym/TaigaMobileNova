package com.grappim.taigamobile.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.subscribeToAll
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.NothingResult
import com.grappim.taigamobile.ui.utils.loadOrError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val tasksRepository: ITasksRepository,
    private val usersRepository: IUsersRepository,
    session: Session
) : ViewModel() {

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
