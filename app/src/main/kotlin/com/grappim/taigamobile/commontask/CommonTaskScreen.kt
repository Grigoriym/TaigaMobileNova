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
import androidx.compose.runtime.CompositionLocalProvider
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
import com.grappim.taigamobile.commontask.components.CommonTaskAssignees
import com.grappim.taigamobile.commontask.components.CommonTaskBelongsTo
import com.grappim.taigamobile.commontask.components.CommonTaskComments
import com.grappim.taigamobile.commontask.components.CommonTaskCreatedBy
import com.grappim.taigamobile.commontask.components.CommonTaskCustomFields
import com.grappim.taigamobile.commontask.components.CommonTaskDueDate
import com.grappim.taigamobile.commontask.components.CommonTaskHeader
import com.grappim.taigamobile.commontask.components.CommonTaskTags
import com.grappim.taigamobile.commontask.components.CommonTaskWatchers
import com.grappim.taigamobile.commontask.components.SelectorEntry
import com.grappim.taigamobile.commontask.components.Selectors
import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.CommentDTO
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.CustomField
import com.grappim.taigamobile.core.domain.CustomFieldValue
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.feature.workitem.ui.widgets.BlockDialog
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.Attachments
import com.grappim.taigamobile.uikit.EditAction
import com.grappim.taigamobile.uikit.EditActions
import com.grappim.taigamobile.uikit.EmptyEditAction
import com.grappim.taigamobile.uikit.FilePickerOld
import com.grappim.taigamobile.uikit.LocalFilePickerOld
import com.grappim.taigamobile.uikit.NavigationActions
import com.grappim.taigamobile.uikit.SimpleEditAction
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.CreateCommentBar
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.editor.Editor
import com.grappim.taigamobile.uikit.widgets.list.Description
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import com.grappim.taigamobile.utils.ui.SuccessResult
import java.time.LocalDateTime

@Composable
@Deprecated("a god class must be removed")
fun CommonTaskScreen(
    goToProfile: (userId: Long) -> Unit,
    goToUserStory: (Long, CommonTaskType, Int) -> Unit,
    goBack: () -> Unit,
    navigateToCreateTask: (CommonTaskType, Long) -> Unit,
    navigateToTask: (Long, CommonTaskType, Int) -> Unit,
    viewModel: CommonTaskViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {}
) {
    val topBarController = LocalTopBarConfig.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                showBackButton = true,
                actions = listOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
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
        LaunchedEffect(goBack) {
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
        attachmentDTOS = attachments.data.orEmpty(),
        assignees = assignees.data.orEmpty(),
        watchers = watchers.data.orEmpty(),
        isAssignedToMe = isAssignedToMe,
        isWatchedByMe = isWatchedByMe,
        userStories = userStories.data.orEmpty(),
        tasks = tasks.data.orEmpty(),
        commentDTOS = comments.data.orEmpty(),
        editActions = EditActions(
            editStatusOld = createEditStatusAction(StatusType.Status),
            editType = createEditStatusAction(StatusType.Type),
            editSeverity = createEditStatusAction(StatusType.Severity),
            editPriority = createEditStatusAction(StatusType.Priority),
            editSwimlaneDTO = SimpleEditAction(
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
                select = { viewModel.addAssignee(it.actualId) },
                isLoading = assignees is LoadingResult,
                remove = { viewModel.removeAssignee(it.actualId) }
            ),
            editWatchers = SimpleEditAction(
                items = teamSearched,
                searchItems = viewModel::searchTeam,
                select = { viewModel.addWatcher(it.actualId) },
                isLoading = watchers is LoadingResult,
                remove = { viewModel.removeWatcher(it.actualId) }
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
                isLoading = assignees is LoadingResult
            ),
            editWatch = EmptyEditAction(
                select = { viewModel.addWatcher() },
                remove = { viewModel.removeWatcher() },
                isLoading = watchers is LoadingResult
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
    modifier: Modifier = Modifier,
    commonTask: CommonTaskExtended? = null,
    creator: UserDTO? = null,
    isLoading: Boolean = false,
    customFields: List<CustomField> = emptyList(),
    attachmentDTOS: List<AttachmentDTO> = emptyList(),
    assignees: List<UserDTO> = emptyList(),
    watchers: List<UserDTO> = emptyList(),
    isAssignedToMe: Boolean = false,
    isWatchedByMe: Boolean = false,
    userStories: List<CommonTask> = emptyList(),
    tasks: List<CommonTask> = emptyList(),
    commentDTOS: List<CommentDTO> = emptyList(),
    editActions: EditActions = EditActions(),
    navigationActions: NavigationActions = NavigationActions(),
    navigateToProfile: (userId: Long) -> Unit = { _ -> },
    showMessage: (message: Int) -> Unit = {}
) {
    Box(modifier.fillMaxSize()) {
        if (state.isDeleteAlertVisible) {
            ConfirmActionDialog(
                title = stringResource(RString.delete_task_title),
                description = stringResource(RString.delete_task_text),
                onConfirm = {
                    state.setDeleteAlertVisible(false)
                    editActions.deleteTask.select(Unit)
                },
                onDismiss = { state.setDeleteAlertVisible(false) },
                iconId = RDrawable.ic_delete
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
                title = stringResource(RString.promote_title),
                description = stringResource(RString.promote_text),
                onConfirm = {
                    state.setPromoteAlertVisible(false)
                    editActions.promoteTask.select(Unit)
                },
                onDismiss = { state.setPromoteAlertVisible(false) },
                iconId = RDrawable.ic_arrow_upward
            )
        }

        CommonTaskDropdownMenuWidget(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.TopEnd),
            state = state,
            editActions = editActions,
            showMessage = showMessage,
            url = commonTask?.url ?: "",
            isBlocked = commonTask?.blockedNote != null
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
            customFields.associate {
                it.id to (if (it.id in customFieldsValues) customFieldsValues[it.id] else it.value)
            }

        Column(Modifier.fillMaxSize()) {
            if (isLoading || creator == null || commonTask == null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularLoaderWidget()
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
                            attachmentDTOS = attachmentDTOS,
                            editAttachments = editActions.editAttachments
                        )

                        item {
                            Spacer(Modifier.height(sectionsPadding))
                        }

                        // user stories
                        if (state.commonTaskType == CommonTaskType.Epic) {
                            simpleTasksListWithTitle(
                                titleText = RString.userstories,
                                bottomPadding = sectionsPadding,
                                commonTasks = userStories,
                                navigateToTask = navigationActions.navigateToTask
                            )
                        }

                        // tasks
                        if (state.commonTaskType == CommonTaskType.UserStory) {
                            simpleTasksListWithTitle(
                                titleText = RString.tasks,
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
                            commentDTOS = commentDTOS,
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
            statusOldEntry = SelectorEntry(
                edit = editActions.editStatusOld,
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
            swimlaneDTOEntry = SelectorEntry(
                edit = editActions.editSwimlaneDTO,
                isVisible = isSwimlaneSelectorVisible,
                hide = { isSwimlaneSelectorVisible = false }
            )
        )

        if (state.isTaskEditorVisible || editActions.editBasicInfo.isLoading) {
            Editor(
                toolbarText = stringResource(RString.edit),
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
                .any { it.isLoading }
        ) {
            LoadingDialog()
        }
    }
}

@PreviewDarkLight
@Composable
private fun CommonTaskScreenPreview() {
    TaigaMobileTheme {
        CompositionLocalProvider(
            LocalFilePickerOld provides object : FilePickerOld() {}
        ) {
            CommonTaskScreenContent(
                state = CommonTaskState(
                    commonTaskType = CommonTaskType.UserStory,
                    url = "https://duckduckgo.com/?q=facilisis",
//                    editActions = EditActions(),
                    toolbarTitle = NativeText.Resource(RString.issues),
                    projectName = "Arnold Bolton",
                    setDropdownMenuExpanded = {},
                    setTaskEditorVisible = {},
                    setDeleteAlertVisible = {},
                    setPromoteAlertVisible = {},
                    setBlockDialogVisible = {}
                ),
                // TODO left it null for now since I do not really use this preview
                commonTask = null,
                creator = UserDTO(
                    id = 0L,
                    fullName = "Full Name",
                    photo = null,
                    bigPhoto = null,
                    username = "username"
                ),
                assignees = List(1) {
                    UserDTO(
                        id = 0L,
                        fullName = "Full Name",
                        photo = null,
                        bigPhoto = null,
                        username = "username"
                    )
                },
                watchers = List(2) {
                    UserDTO(
                        id = 0L,
                        fullName = "Full Name",
                        photo = null,
                        bigPhoto = null,
                        username = "username"
                    )
                },
                tasks = List(1) {
                    CommonTask(
                        id = it.toLong(),
                        createdDate = LocalDateTime.now(),
                        title = "Very cool story",
                        ref = 100,
                        statusOld = StatusOld(
                            id = 1,
                            name = "In progress",
                            color = "#729fcf",
                            type = StatusType.Status
                        ),
                        assignee = null,
                        projectDTOInfo = ProjectDTO(0, "", ""),
                        taskType = CommonTaskType.UserStory,
                        isClosed = false
                    )
                },
                commentDTOS = List(1) {
                    CommentDTO(
                        id = "",
                        author = UserDTO(
                            id = 0L,
                            fullName = "Full Name",
                            photo = null,
                            bigPhoto = null,
                            username = "username"
                        ),
                        text = "This is comment text",
                        postDateTime = LocalDateTime.now(),
                        deleteDate = null
                    )
                }
            )
        }
    }
}
