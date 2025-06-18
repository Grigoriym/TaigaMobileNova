package com.grappim.taigamobile.commontask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.grappim.taigamobile.R
import com.grappim.taigamobile.core.ui.NativeText
import com.grappim.taigamobile.domain.entities.Attachment
import com.grappim.taigamobile.domain.entities.Comment
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskExtended
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.CustomField
import com.grappim.taigamobile.domain.entities.CustomFieldValue
import com.grappim.taigamobile.domain.entities.CustomFields
import com.grappim.taigamobile.domain.entities.EpicShortInfo
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.domain.entities.Sprint
import com.grappim.taigamobile.domain.entities.Status
import com.grappim.taigamobile.domain.entities.StatusType
import com.grappim.taigamobile.domain.entities.Swimlane
import com.grappim.taigamobile.domain.entities.Tag
import com.grappim.taigamobile.domain.entities.User
import com.grappim.taigamobile.domain.repositories.ITasksRepository
import com.grappim.taigamobile.domain.repositories.IUsersRepository
import com.grappim.taigamobile.epics.EpicsRepository
import com.grappim.taigamobile.sprint.ISprintsRepository
import com.grappim.taigamobile.state.Session
import com.grappim.taigamobile.state.postUpdate
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.NothingResult
import com.grappim.taigamobile.ui.utils.loadOrError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.InputStream
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CommonTaskViewModel @Inject constructor(
    private val session: Session,
    private val tasksRepository: ITasksRepository,
    private val usersRepository: IUsersRepository,
    private val sprintsRepository: ISprintsRepository,
    private val epicsRepository: EpicsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        val SPRINT_HEADER = Sprint(-1, "HEADER", -1, LocalDate.MIN, LocalDate.MIN, 0, false)
        val SWIMLANE_HEADER = Swimlane(-1, "HEADER", -1)
    }

    private val route = savedStateHandle.toRoute<CommonTaskNavDestination>()
    val commonTaskId: Long = route.taskId
    val ref = route.ref

    private val _state = MutableStateFlow(
        CommonTaskState(
            toolbarTitle = NativeText.Arguments(
                id = when (route.taskType) {
                    CommonTaskType.UserStory -> R.string.userstory_slug
                    CommonTaskType.Task -> R.string.task_slug
                    CommonTaskType.Epic -> R.string.epic_slug
                    CommonTaskType.Issue -> R.string.issue_slug
                },
                args = listOf(ref)
            ),
            url = "",
            projectName = session.currentProjectName.value,
            commonTaskType = route.taskType,
            editActions = EditActions(),
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            setDeleteAlertVisible = ::setDeleteAlertVisible,
            setTaskEditorVisible = ::setTaskEditorVisible,
            setBlockDialogVisible = ::setBlockDialogVisible,
            setPromoteAlertVisible = ::setPromoteAlertVisible
        )
    )
    val state = _state.asStateFlow()

    val commonTask = MutableResultFlow<CommonTaskExtended>()

    val creator = MutableResultFlow<User>()
    val customFields = MutableResultFlow<CustomFields>()
    val attachments = MutableResultFlow<List<Attachment>>()
    val assignees = MutableResultFlow<List<User>>()
    val watchers = MutableResultFlow<List<User>>()
    val userStories = MutableResultFlow<List<CommonTask>>()
    val tasks = MutableResultFlow<List<CommonTask>>()
    val comments = MutableResultFlow<List<Comment>>()

    val team = MutableResultFlow<List<User>>()
    val tags = MutableResultFlow<List<Tag>>()
    val swimlanes = MutableResultFlow<List<Swimlane>>()
    val statuses = MutableResultFlow<Map<StatusType, List<Status>>>()

    val isAssignedToMe =
        assignees.map { session.currentUserId.value in it.data?.map { it.id }.orEmpty() }
            .stateIn(viewModelScope, SharingStarted.Lazily, false)
    val isWatchedByMe =
        watchers.map { session.currentUserId.value in it.data?.map { it.id }.orEmpty() }
            .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val sprints = sprintsRepository.getSprints()
        .map { it.insertHeaderItem(item = SPRINT_HEADER) }
        .cachedIn(viewModelScope)

    val editSprintResult = MutableResultFlow<Unit>(NothingResult())

    private val epicsQuery = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val epics = epicsQuery.flatMapLatest { query ->
        epicsRepository.getEpics(FiltersData(query = query))
    }.cachedIn(viewModelScope)

    init {
        loadData(isReloading = false)
    }

    private fun setDeleteAlertVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteAlertVisible = isVisible)
        }
    }

    private fun setBlockDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isBlockDialogVisible = isVisible)
        }
    }

    private fun setPromoteAlertVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isPromoteAlertVisible = isVisible)
        }
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }

    private fun setTaskEditorVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isTaskEditorVisible = isVisible)
        }
    }

    private fun loadData(isReloading: Boolean = true) = viewModelScope.launch {
        commonTask.loadOrError(showLoading = !isReloading) {
            tasksRepository.getCommonTask(commonTaskId, _state.value.commonTaskType).also {

                suspend fun MutableResultFlow<List<User>>.loadUsersFromIds(ids: List<Long>) =
                    loadOrError(showLoading = false) {
                        coroutineScope {
                            ids.map {
                                async { usersRepository.getUser(it) }
                            }.awaitAll()
                        }
                    }

                val jobsToLoad = arrayOf(
                    launch {
                        creator.loadOrError(showLoading = false) { usersRepository.getUser(it.creatorId) }
                    },
                    launch {
                        customFields.loadOrError(showLoading = false) {
                            tasksRepository.getCustomFields(
                                commonTaskId,
                                _state.value.commonTaskType
                            )
                        }
                    },
                    launch {
                        attachments.loadOrError(showLoading = false) {
                            tasksRepository.getAttachments(
                                commonTaskId,
                                _state.value.commonTaskType
                            )
                        }
                    },
                    launch { assignees.loadUsersFromIds(it.assignedIds) },
                    launch { watchers.loadUsersFromIds(it.watcherIds) },
                    launch {
                        userStories.loadOrError(showLoading = false) {
                            tasksRepository.getEpicUserStories(
                                commonTaskId
                            )
                        }
                    },
                    launch {
                        tasks.loadOrError(showLoading = false) {
                            tasksRepository.getUserStoryTasks(
                                commonTaskId
                            )
                        }
                    },
                    launch {
                        comments.loadOrError(showLoading = false) {
                            tasksRepository.getComments(
                                commonTaskId,
                                _state.value.commonTaskType
                            )
                        }
                    },
                    launch {
                        tags.loadOrError(showLoading = false) {
                            tasksRepository.getAllTags(_state.value.commonTaskType)
                                .also { tagsSearched.value = it }
                        }
                    }
                ) + if (!isReloading) {
                    arrayOf(
                        launch {
                            team.loadOrError(showLoading = false) {
                                usersRepository.getTeam()
                                    .map { it.toUser() }
                                    .also { teamSearched.value = it }
                            }
                        },
                        launch {
                            swimlanes.loadOrError(showLoading = false) {
                                listOf(SWIMLANE_HEADER) + tasksRepository.getSwimlanes() // prepend "unclassified"
                            }
                        },
                        launch {
                            statuses.loadOrError(showLoading = false) {
                                StatusType.values().filter {
                                    if (_state.value.commonTaskType != CommonTaskType.Issue) it == StatusType.Status else true
                                }.associateWith {
                                    tasksRepository.getStatusByType(
                                        _state.value.commonTaskType,
                                        it
                                    )
                                }
                            }
                        }
                    )
                } else {
                    emptyArray()
                }

                joinAll(*jobsToLoad)
            }
        }
    }

    // ================
    // Edit task itself
    // ================

    // Edit task itself (title & description)
    val editBasicInfoResult = MutableResultFlow<Unit>()

    fun editBasicInfo(title: String, description: String) = viewModelScope.launch {
        editBasicInfoResult.loadOrError(R.string.permission_error) {
            tasksRepository.editCommonTaskBasicInfo(commonTask.value.data!!, title, description)
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    // Edit status (and also type, severity, priority)
    val editStatusResult = MutableResultFlow<StatusType>()

    fun editStatus(status: Status) = viewModelScope.launch {
        editStatusResult.value = LoadingResult(status.type)

        editStatusResult.loadOrError(R.string.permission_error) {
            tasksRepository.editStatus(commonTask.value.data!!, status.id, status.type)
            loadData().join()
            session.taskEdit.postUpdate()
            status.type
        }
    }

    fun editSprint(sprint: Sprint) = viewModelScope.launch {
        editSprintResult.loadOrError(R.string.permission_error) {
            tasksRepository.editSprint(
                commonTask.value.data!!,
                sprint.takeIf { it != SPRINT_HEADER }?.id
            )
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    // use team for both assignees and watchers
    val teamSearched = MutableStateFlow(emptyList<User>())

    fun searchTeam(query: String) = viewModelScope.launch {
        val q = query.lowercase()
        teamSearched.value = team.value.data
            .orEmpty()
            .filter { q in it.username.lowercase() || q in it.displayName.lowercase() }
    }

    // Edit assignees

    private fun editAssignees(userId: Long, remove: Boolean) = viewModelScope.launch {
        assignees.loadOrError(R.string.permission_error) {
            teamSearched.value = team.value.data.orEmpty()

            tasksRepository.editAssignees(
                commonTask.value.data!!,
                commonTask.value.data!!.assignedIds.let {
                    if (remove) it - userId
                    else it + userId
                }
            )

            loadData().join()
            session.taskEdit.postUpdate()
            assignees.value.data
        }
    }

    fun addAssignee(userId: Long = session.currentUserId.value) =
        editAssignees(userId, remove = false)

    fun removeAssignee(userId: Long = session.currentUserId.value) =
        editAssignees(userId, remove = true)

    // Edit watchers

    private fun editWatchers(userId: Long, remove: Boolean) = viewModelScope.launch {
        watchers.loadOrError(R.string.permission_error) {
            teamSearched.value = team.value.data.orEmpty()

            tasksRepository.editWatchers(
                commonTask.value.data!!,
                commonTask.value.data?.watcherIds.orEmpty().let {
                    if (remove) it - userId
                    else it + userId
                }
            )

            loadData().join()
            session.taskEdit.postUpdate()
            watchers.value.data
        }
    }

    fun addWatcher(userId: Long = session.currentUserId.value) =
        editWatchers(userId, remove = false)

    fun removeWatcher(userId: Long = session.currentUserId.value) =
        editWatchers(userId, remove = true)

    // Tags
    val tagsSearched = MutableStateFlow(emptyList<Tag>())

    fun searchTags(query: String) = viewModelScope.launch {
        tagsSearched.value =
            tags.value.data.orEmpty().filter { query.isNotEmpty() && query.lowercase() in it.name }
    }

    private fun editTag(tag: Tag, remove: Boolean) = viewModelScope.launch {
        tags.loadOrError(R.string.permission_error) {
            tagsSearched.value = tags.value.data.orEmpty()

            tasksRepository.editTags(
                commonTask.value.data!!,
                commonTask.value.data!!.tags.let { if (remove) it - tag else it + tag },
            )

            loadData().join()
            session.taskEdit.postUpdate()
            tags.value.data
        }
    }

    fun addTag(tag: Tag) = editTag(tag, remove = false)
    fun deleteTag(tag: Tag) = editTag(tag, remove = true)

    // Swimlanes
    fun editSwimlane(swimlane: Swimlane) = viewModelScope.launch {
        swimlanes.loadOrError(R.string.permission_error) {
            tasksRepository.editUserStorySwimlane(
                commonTask.value.data!!,
                swimlane.takeIf { it != SWIMLANE_HEADER }?.id
            )
            loadData().join()
            session.taskEdit.postUpdate()
            swimlanes.value.data
        }
    }

    // Due date
    val editDueDateResult = MutableResultFlow<Unit>()

    fun editDueDate(date: LocalDate?) = viewModelScope.launch {
        editDueDateResult.loadOrError(R.string.permission_error) {
            tasksRepository.editDueDate(commonTask.value.data!!, date)
            loadData().join()
        }
    }

    // Epic color
    val editEpicColorResult = MutableResultFlow<Unit>()

    fun editEpicColor(color: String) = viewModelScope.launch {
        editEpicColorResult.loadOrError(R.string.permission_error) {
            tasksRepository.editEpicColor(commonTask.value.data!!, color)
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    val editBlockedResult = MutableResultFlow<Unit>()

    fun editBlocked(blockedNote: String?) = viewModelScope.launch {
        editBlockedResult.loadOrError(R.string.permission_error) {
            tasksRepository.editBlocked(commonTask.value.data!!, blockedNote)
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    fun searchEpics(query: String) {
        epicsQuery.value = query
    }

    val linkToEpicResult = MutableResultFlow<Unit>(NothingResult())

    fun linkToEpic(epic: CommonTask) = viewModelScope.launch {
        linkToEpicResult.loadOrError(R.string.permission_error) {
            tasksRepository.linkToEpic(epic.id, commonTaskId)
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    fun unlinkFromEpic(epic: EpicShortInfo) = viewModelScope.launch {
        linkToEpicResult.loadOrError(R.string.permission_error) {
            tasksRepository.unlinkFromEpic(epic.id, commonTaskId)
            loadData().join()
            session.taskEdit.postUpdate()
        }
    }

    // Edit comments

    fun createComment(comment: String) = viewModelScope.launch {
        comments.loadOrError(R.string.permission_error) {
            tasksRepository.createComment(
                commonTaskId,
                _state.value.commonTaskType,
                comment,
                commonTask.value.data!!.version
            )
            loadData().join()
            comments.value.data
        }
    }

    fun deleteComment(comment: Comment) = viewModelScope.launch {
        comments.loadOrError(R.string.permission_error) {
            tasksRepository.deleteComment(commonTaskId, _state.value.commonTaskType, comment.id)
            loadData().join()
            comments.value.data
        }
    }


    fun deleteAttachment(attachment: Attachment) = viewModelScope.launch {
        attachments.loadOrError(R.string.permission_error) {
            tasksRepository.deleteAttachment(_state.value.commonTaskType, attachment.id)
            loadData().join()
            attachments.value.data
        }
    }

    fun addAttachment(fileName: String, inputStream: InputStream) = viewModelScope.launch {
        attachments.loadOrError(R.string.permission_error) {
            tasksRepository.addAttachment(
                commonTaskId,
                _state.value.commonTaskType,
                fileName,
                inputStream
            )
            loadData().join()
            attachments.value.data
        }
    }

    // Delete task
    val deleteResult = MutableResultFlow<Unit>()

    fun deleteTask() = viewModelScope.launch {
        deleteResult.loadOrError(R.string.permission_error) {
            tasksRepository.deleteCommonTask(_state.value.commonTaskType, commonTaskId)
            session.taskEdit.postUpdate()
        }
    }

    val promoteResult = MutableResultFlow<CommonTask>()

    fun promoteToUserStory() = viewModelScope.launch {
        promoteResult.loadOrError(R.string.permission_error, preserveValue = false) {
            tasksRepository.promoteCommonTaskToUserStory(commonTaskId, _state.value.commonTaskType)
                .also {
                    session.taskEdit.postUpdate()
                }
        }
    }

    fun editCustomField(customField: CustomField, value: CustomFieldValue?) =
        viewModelScope.launch {
            customFields.loadOrError(R.string.permission_error) {
                tasksRepository.editCustomFields(
                    commonTaskType = _state.value.commonTaskType,
                    commonTaskId = commonTaskId,
                    fields = customFields.value.data?.fields.orEmpty().map {
                        it.id to (if (it.id == customField.id) value else it.value)
                    }.toMap(),
                    version = customFields.value.data?.version ?: 0
                )
                loadData().join()
                customFields.value.data
            }

        }
}
