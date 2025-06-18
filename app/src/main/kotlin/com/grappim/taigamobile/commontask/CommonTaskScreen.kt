@file:OptIn(ExperimentalLayoutApi::class)

package com.grappim.taigamobile.commontask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.R
import com.grappim.taigamobile.commontask.components.BlockDialog
import com.grappim.taigamobile.commontask.components.CommonTaskAssignees
import com.grappim.taigamobile.commontask.components.CommonTaskBelongsTo
import com.grappim.taigamobile.commontask.components.CommonTaskComments
import com.grappim.taigamobile.commontask.components.CommonTaskCreatedBy
import com.grappim.taigamobile.commontask.components.CommonTaskCustomFields
import com.grappim.taigamobile.commontask.components.CommonTaskDueDate
import com.grappim.taigamobile.commontask.components.CommonTaskHeader
import com.grappim.taigamobile.commontask.components.CommonTaskTags
import com.grappim.taigamobile.commontask.components.CommonTaskWatchers
import com.grappim.taigamobile.commontask.components.CreateCommentBar
import com.grappim.taigamobile.commontask.components.SelectorEntry
import com.grappim.taigamobile.commontask.components.Selectors
import com.grappim.taigamobile.domain.entities.Attachment
import com.grappim.taigamobile.domain.entities.Comment
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskExtended
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.CustomField
import com.grappim.taigamobile.domain.entities.CustomFieldValue
import com.grappim.taigamobile.domain.entities.StatusType
import com.grappim.taigamobile.domain.entities.User
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.main.topbar.TopBarActionResource
import com.grappim.taigamobile.main.topbar.TopBarConfig
import com.grappim.taigamobile.ui.components.dialogs.ConfirmActionDialog
import com.grappim.taigamobile.ui.components.dialogs.LoadingDialog
import com.grappim.taigamobile.ui.components.editors.Editor
import com.grappim.taigamobile.ui.components.lists.Attachments
import com.grappim.taigamobile.ui.components.lists.Description
import com.grappim.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import com.grappim.taigamobile.ui.components.loaders.CircularLoader
import com.grappim.taigamobile.ui.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.SubscribeOnError
import com.grappim.taigamobile.ui.utils.SuccessResult

@Composable
fun CommonTaskScreen(
    viewModel: CommonTaskViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {},
    goToProfile: (userId: Long) -> Unit,
    goToUserStory: (Long, CommonTaskType, Int) -> Unit,
    goBack: () -> Unit,
    navigateToCreateTask: (CommonTaskType, Long) -> Unit,
    navigateToTask: (Long, CommonTaskType, Int) -> Unit
) {
    val topBarController = LocalTopBarConfig.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                showBackButton = true,
                actions = listOf(
                    TopBarActionResource(
                        drawable = R.drawable.ic_options,
                        contentDescription = "Task options",
                        onClick = {
                            state.setDropdownMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    val commonTask by viewModel.commonTask.collectAsState()
    commonTask.SubscribeOnError(showMessage)

    val creator by viewModel.creator.collectAsState()
    creator.SubscribeOnError(showMessage)

    val assignees by viewModel.assignees.collectAsState()
    assignees.SubscribeOnError(showMessage)

    val watchers by viewModel.watchers.collectAsState()
    watchers.SubscribeOnError(showMessage)

    val userStories by viewModel.userStories.collectAsState()
    userStories.SubscribeOnError(showMessage)

    val tasks by viewModel.tasks.collectAsState()
    tasks.SubscribeOnError(showMessage)

    val comments by viewModel.comments.collectAsState()
    comments.SubscribeOnError(showMessage)

    val editBasicInfoResult by viewModel.editBasicInfoResult.collectAsState()
    editBasicInfoResult.SubscribeOnError(showMessage)

    val statuses by viewModel.statuses.collectAsState()
    statuses.SubscribeOnError(showMessage)
    val editStatusResult by viewModel.editStatusResult.collectAsState()
    editStatusResult.SubscribeOnError(showMessage)

    val swimlanes by viewModel.swimlanes.collectAsState()
    swimlanes.SubscribeOnError(showMessage)

    val sprints = viewModel.sprints.collectAsLazyPagingItems()
    sprints.SubscribeOnError(showMessage)
    val editSprintResult by viewModel.editSprintResult.collectAsState()
    editSprintResult.SubscribeOnError(showMessage)

    val epics = viewModel.epics.collectAsLazyPagingItems()
    epics.SubscribeOnError(showMessage)
    val linkToEpicResult by viewModel.linkToEpicResult.collectAsState()
    linkToEpicResult.SubscribeOnError(showMessage)

    val team by viewModel.team.collectAsState()
    team.SubscribeOnError(showMessage)
    val teamSearched by viewModel.teamSearched.collectAsState()

    val customFields by viewModel.customFields.collectAsState()
    customFields.SubscribeOnError(showMessage)

    val attachments by viewModel.attachments.collectAsState()
    attachments.SubscribeOnError(showMessage)

    val tags by viewModel.tags.collectAsState()
    tags.SubscribeOnError(showMessage)
    val tagsSearched by viewModel.tagsSearched.collectAsState()

    val editEpicColorResult by viewModel.editEpicColorResult.collectAsState()
    editEpicColorResult.SubscribeOnError(showMessage)

    val editBlockedResult by viewModel.editBlockedResult.collectAsState()
    editBlockedResult.SubscribeOnError(showMessage)

    val editDueDateResult by viewModel.editDueDateResult.collectAsState()
    editDueDateResult.SubscribeOnError(showMessage)

    val deleteResult by viewModel.deleteResult.collectAsState()
    deleteResult.SubscribeOnError(showMessage)
    deleteResult.takeIf { it is SuccessResult }?.let {
        LaunchedEffect(Unit) {
            goBack()
        }
    }

    val promoteResult by viewModel.promoteResult.collectAsState()
    promoteResult.SubscribeOnError(showMessage)
    promoteResult.takeIf { it is SuccessResult }?.data?.let {
        LaunchedEffect(Unit) {
            goToUserStory(it.id, CommonTaskType.UserStory, it.ref)
        }
    }

    val isAssignedToMe by viewModel.isAssignedToMe.collectAsState()
    val isWatchedByMe by viewModel.isWatchedByMe.collectAsState()

    fun createEditStatusAction(statusType: StatusType) = SimpleEditAction(
        items = statuses.data?.get(statusType).orEmpty(),
        select = viewModel::editStatus,
        isLoading = (editStatusResult as? LoadingResult)?.data == statusType
    )

    CommonTaskScreenContent(
        state = state,
        commonTask = commonTask.data,
        creator = creator.data,
        isLoading = commonTask is LoadingResult,
        customFields = customFields.data?.fields.orEmpty(),
        attachments = attachments.data.orEmpty(),
        assignees = assignees.data.orEmpty(),
        watchers = watchers.data.orEmpty(),
        isAssignedToMe = isAssignedToMe,
        isWatchedByMe = isWatchedByMe,
        userStories = userStories.data.orEmpty(),
        tasks = tasks.data.orEmpty(),
        comments = comments.data.orEmpty(),
        editActions = EditActions(
            editStatus = createEditStatusAction(StatusType.Status),
            editType = createEditStatusAction(StatusType.Type),
            editSeverity = createEditStatusAction(StatusType.Severity),
            editPriority = createEditStatusAction(StatusType.Priority),
            editSwimlane = SimpleEditAction(
                items = swimlanes.data.orEmpty(),
                select = viewModel::editSwimlane,
                isLoading = swimlanes is LoadingResult
            ),
            editSprint = SimpleEditAction(
                itemsLazy = sprints,
                select = viewModel::editSprint,
                isLoading = editSprintResult is LoadingResult
            ),
            editEpics = EditAction(
                itemsLazy = epics,
                searchItems = viewModel::searchEpics,
                select = viewModel::linkToEpic,
                isLoading = linkToEpicResult is LoadingResult,
                remove = viewModel::unlinkFromEpic
            ),
            editAttachments = EditAction(
                select = { (file, stream) -> viewModel.addAttachment(file, stream) },
                remove = viewModel::deleteAttachment,
                isLoading = attachments is LoadingResult
            ),
            editAssignees = SimpleEditAction(
                items = teamSearched,
                searchItems = viewModel::searchTeam,
                select = { viewModel.addAssignee(it.id) },
                isLoading = assignees is LoadingResult,
                remove = { viewModel.removeAssignee(it.id) }
            ),
            editWatchers = SimpleEditAction(
                items = teamSearched,
                searchItems = viewModel::searchTeam,
                select = { viewModel.addWatcher(it.id) },
                isLoading = watchers is LoadingResult,
                remove = { viewModel.removeWatcher(it.id) }
            ),
            editComments = EditAction(
                select = viewModel::createComment,
                remove = viewModel::deleteComment,
                isLoading = comments is LoadingResult
            ),
            editBasicInfo = SimpleEditAction(
                select = { (title, description) -> viewModel.editBasicInfo(title, description) },
                isLoading = editBasicInfoResult is LoadingResult
            ),
            deleteTask = EmptyEditAction(
                select = { viewModel.deleteTask() },
                isLoading = deleteResult is LoadingResult
            ),
            promoteTask = EmptyEditAction(
                select = { viewModel.promoteToUserStory() },
                isLoading = promoteResult is LoadingResult
            ),
            editCustomField = SimpleEditAction(
                select = { (field, value) -> viewModel.editCustomField(field, value) },
                isLoading = customFields is LoadingResult
            ),
            editTags = EditAction(
                items = tagsSearched,
                searchItems = viewModel::searchTags,
                select = viewModel::addTag,
                remove = viewModel::deleteTag,
                isLoading = tags is LoadingResult
            ),
            editDueDate = EditAction(
                select = viewModel::editDueDate,
                remove = { viewModel.editDueDate(null) },
                isLoading = editDueDateResult is LoadingResult
            ),
            editEpicColor = SimpleEditAction(
                select = viewModel::editEpicColor,
                isLoading = editEpicColorResult is LoadingResult
            ),
            editAssign = EmptyEditAction(
                select = { viewModel.addAssignee() },
                remove = { viewModel.removeAssignee() },
                isLoading = assignees is LoadingResult,
            ),
            editWatch = EmptyEditAction(
                select = { viewModel.addWatcher() },
                remove = { viewModel.removeWatcher() },
                isLoading = watchers is LoadingResult,
            ),
            editBlocked = EditAction(
                select = { viewModel.editBlocked(it) },
                remove = { viewModel.editBlocked(null) },
                isLoading = editBlockedResult is LoadingResult
            )
        ),
        navigationActions = NavigationActions(
            navigateBack = goBack,
            navigateToCreateTask = {
                navigateToCreateTask(CommonTaskType.Task, viewModel.commonTaskId)
            },
            navigateToTask = navigateToTask
        ),
        navigateToProfile = goToProfile,
        showMessage = showMessage
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonTaskScreenContent(
    state: CommonTaskState,
    commonTask: CommonTaskExtended? = null,
    creator: User? = null,
    isLoading: Boolean = false,
    customFields: List<CustomField> = emptyList(),
    attachments: List<Attachment> = emptyList(),
    assignees: List<User> = emptyList(),
    watchers: List<User> = emptyList(),
    isAssignedToMe: Boolean = false,
    isWatchedByMe: Boolean = false,
    userStories: List<CommonTask> = emptyList(),
    tasks: List<CommonTask> = emptyList(),
    comments: List<Comment> = emptyList(),
    editActions: EditActions = EditActions(),
    navigationActions: NavigationActions = NavigationActions(),
    navigateToProfile: (userId: Long) -> Unit = { _ -> },
    showMessage: (message: Int) -> Unit = {}
) = Box(Modifier.fillMaxSize()) {
    if (state.isDeleteAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(R.string.delete_task_title),
            text = stringResource(R.string.delete_task_text),
            onConfirm = {
                state.setDeleteAlertVisible(false)
                editActions.deleteTask.select(Unit)
            },
            onDismiss = { state.setDeleteAlertVisible(false) },
            iconId = R.drawable.ic_delete
        )
    }

    if (state.isBlockDialogVisible) {
        BlockDialog(
            onConfirm = {
                editActions.editBlocked.select(it)
                state.setBlockDialogVisible(false)
            },
            onDismiss = {
                state.setBlockDialogVisible(false)
            }
        )
    }

    if (state.isPromoteAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(R.string.promote_title),
            text = stringResource(R.string.promote_text),
            onConfirm = {
                state.setPromoteAlertVisible(false)
                editActions.promoteTask.select(Unit)
            },
            onDismiss = { state.setPromoteAlertVisible(false) },
            iconId = R.drawable.ic_arrow_upward
        )
    }

    CommonTaskDropdownMenu(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state,
        showMessage = showMessage,
        url = commonTask?.url ?: ""
    )

    var isStatusSelectorVisible by remember { mutableStateOf(false) }
    var isTypeSelectorVisible by remember { mutableStateOf(false) }
    var isSeveritySelectorVisible by remember { mutableStateOf(false) }
    var isPrioritySelectorVisible by remember { mutableStateOf(false) }
    var isSprintSelectorVisible by remember { mutableStateOf(false) }
    var isAssigneesSelectorVisible by remember { mutableStateOf(false) }
    var isWatchersSelectorVisible by remember { mutableStateOf(false) }
    var isEpicsSelectorVisible by remember { mutableStateOf(false) }
    var isSwimlaneSelectorVisible by remember { mutableStateOf(false) }

    var customFieldsValues by remember { mutableStateOf(emptyMap<Long, CustomFieldValue?>()) }
    customFieldsValues =
        customFields.associate { it.id to (if (it.id in customFieldsValues) customFieldsValues[it.id] else it.value) }

    Column(Modifier.fillMaxSize()) {
        if (isLoading || creator == null || commonTask == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularLoader()
            }
        } else {
            val sectionsPadding = 16.dp

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.BottomCenter
            ) {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = mainHorizontalScreenPadding)
                ) {

                    item {
                        Spacer(Modifier.height(sectionsPadding / 2))
                    }

                    CommonTaskHeader(
                        commonTask = commonTask,
                        editActions = editActions,
                        showStatusSelector = { isStatusSelectorVisible = true },
                        showSprintSelector = { isSprintSelectorVisible = true },
                        showTypeSelector = { isTypeSelectorVisible = true },
                        showSeveritySelector = { isSeveritySelectorVisible = true },
                        showPrioritySelector = { isPrioritySelectorVisible = true },
                        showSwimlaneSelector = { isSwimlaneSelectorVisible = true }
                    )

                    CommonTaskBelongsTo(
                        commonTask = commonTask,
                        navigationActions = navigationActions,
                        editActions = editActions,
                        showEpicsSelector = { isEpicsSelectorVisible = true }
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    Description(commonTask.description)

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    CommonTaskTags(
                        commonTask = commonTask,
                        editActions = editActions
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    if (state.commonTaskType != CommonTaskType.Epic) {
                        CommonTaskDueDate(
                            commonTask = commonTask,
                            editActions = editActions
                        )

                        item {
                            Spacer(Modifier.height(sectionsPadding))
                        }
                    }

                    CommonTaskCreatedBy(
                        creator = creator,
                        commonTask = commonTask,
                        navigateToProfile = navigateToProfile
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    CommonTaskAssignees(
                        assignees = assignees,
                        isAssignedToMe = isAssignedToMe,
                        editActions = editActions,
                        showAssigneesSelector = { isAssigneesSelectorVisible = true },
                        navigateToProfile = navigateToProfile
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    CommonTaskWatchers(
                        watchers = watchers,
                        isWatchedByMe = isWatchedByMe,
                        editActions = editActions,
                        showWatchersSelector = { isWatchersSelectorVisible = true },
                        navigateToProfile = navigateToProfile
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding * 2))
                    }

                    if (customFields.isNotEmpty()) {
                        CommonTaskCustomFields(
                            customFields = customFields,
                            customFieldsValues = customFieldsValues,
                            onValueChange = { itemId, value ->
                                customFieldsValues =
                                    customFieldsValues - itemId + Pair(itemId, value)
                            },
                            editActions = editActions
                        )

                        item {
                            Spacer(Modifier.height(sectionsPadding * 3))
                        }
                    }

                    Attachments(
                        attachments = attachments,
                        editAttachments = editActions.editAttachments
                    )

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    // user stories
                    if (state.commonTaskType == CommonTaskType.Epic) {
                        SimpleTasksListWithTitle(
                            titleText = R.string.userstories,
                            bottomPadding = sectionsPadding,
                            commonTasks = userStories,
                            navigateToTask = navigationActions.navigateToTask
                        )
                    }

                    // tasks
                    if (state.commonTaskType == CommonTaskType.UserStory) {
                        SimpleTasksListWithTitle(
                            titleText = R.string.tasks,
                            bottomPadding = sectionsPadding,
                            commonTasks = tasks,
                            navigateToTask = navigationActions.navigateToTask,
                            navigateToCreateCommonTask = navigationActions.navigateToCreateTask
                        )
                    }

                    item {
                        Spacer(Modifier.height(sectionsPadding))
                    }

                    CommonTaskComments(
                        comments = comments,
                        editActions = editActions,
                        navigateToProfile = navigateToProfile
                    )

                    item {
                        Spacer(
                            Modifier
                                .navigationBarsPadding()
                                .imePadding()
                                .height(72.dp)
                        )
                    }
                }

                CreateCommentBar(editActions.editComments.select)
            }
        }
    }

    // Bunch of list selectors
    Selectors(
        statusEntry = SelectorEntry(
            edit = editActions.editStatus,
            isVisible = isStatusSelectorVisible,
            hide = { isStatusSelectorVisible = false }
        ),
        typeEntry = SelectorEntry(
            edit = editActions.editType,
            isVisible = isTypeSelectorVisible,
            hide = { isTypeSelectorVisible = false }
        ),
        severityEntry = SelectorEntry(
            edit = editActions.editSeverity,
            isVisible = isSeveritySelectorVisible,
            hide = { isSeveritySelectorVisible = false }
        ),
        priorityEntry = SelectorEntry(
            edit = editActions.editPriority,
            isVisible = isPrioritySelectorVisible,
            hide = { isPrioritySelectorVisible = false }
        ),
        sprintEntry = SelectorEntry(
            edit = editActions.editSprint,
            isVisible = isSprintSelectorVisible,
            hide = { isSprintSelectorVisible = false }
        ),
        epicsEntry = SelectorEntry(
            edit = editActions.editEpics,
            isVisible = isEpicsSelectorVisible,
            hide = { isEpicsSelectorVisible = false }
        ),
        assigneesEntry = SelectorEntry(
            edit = editActions.editAssignees,
            isVisible = isAssigneesSelectorVisible,
            hide = { isAssigneesSelectorVisible = false }
        ),
        watchersEntry = SelectorEntry(
            edit = editActions.editWatchers,
            isVisible = isWatchersSelectorVisible,
            hide = { isWatchersSelectorVisible = false }
        ),
        swimlaneEntry = SelectorEntry(
            edit = editActions.editSwimlane,
            isVisible = isSwimlaneSelectorVisible,
            hide = { isSwimlaneSelectorVisible = false }
        )
    )

    // Editor
    if (state.isTaskEditorVisible || editActions.editBasicInfo.isLoading) {
        Editor(
            toolbarText = stringResource(R.string.edit),
            title = commonTask?.title.orEmpty(),
            description = commonTask?.description.orEmpty(),
            onSaveClick = { title, description ->
                state.setTaskEditorVisible(false)
                editActions.editBasicInfo.select(Pair(title, description))
            },
            navigateBack = { state.setTaskEditorVisible(false) }
        )
    }

    if (editActions.run { listOf(editBasicInfo, promoteTask, deleteTask, editBlocked) }
            .any { it.isLoading }) {
        LoadingDialog()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun CommonTaskScreenPreview() = TaigaMobileTheme {
//    CompositionLocalProvider(
//        LocalFilePicker provides object : FilePicker() {}
//    ) {
//        CommonTaskScreenContent(
//            commonTaskType = CommonTaskType.UserStory,
//            toolbarTitle = "Userstory #99",
//            toolbarSubtitle = "Project #228",
//            commonTask = null, // TODO left it null for now since I do not really use this preview
//            creator = User(
//                _id = 0L,
//                fullName = "Full Name",
//                photo = null,
//                bigPhoto = null,
//                username = "username"
//            ),
//            assignees = List(1) {
//                User(
//                    _id = 0L,
//                    fullName = "Full Name",
//                    photo = null,
//                    bigPhoto = null,
//                    username = "username"
//                )
//            },
//            watchers = List(2) {
//                User(
//                    _id = 0L,
//                    fullName = "Full Name",
//                    photo = null,
//                    bigPhoto = null,
//                    username = "username"
//                )
//            },
//            tasks = List(1) {
//                CommonTask(
//                    id = it.toLong(),
//                    createdDate = LocalDateTime.now(),
//                    title = "Very cool story",
//                    ref = 100,
//                    status = Status(
//                        id = 1,
//                        name = "In progress",
//                        color = "#729fcf",
//                        type = StatusType.Status
//                    ),
//                    assignee = null,
//                    projectInfo = Project(0, "", ""),
//                    taskType = CommonTaskType.UserStory,
//                    isClosed = false
//                )
//            },
//            comments = List(1) {
//                Comment(
//                    id = "",
//                    author = User(
//                        _id = 0L,
//                        fullName = "Full Name",
//                        photo = null,
//                        bigPhoto = null,
//                        username = "username"
//                    ),
//                    text = "This is comment text",
//                    postDateTime = LocalDateTime.now(),
//                    deleteDate = null
//                )
//            }
//        )
//    }
//}
