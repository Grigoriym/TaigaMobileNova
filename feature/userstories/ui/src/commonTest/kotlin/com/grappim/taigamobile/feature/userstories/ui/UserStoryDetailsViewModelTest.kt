@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.userstories.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryEpic
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.common_error_message
import com.grappim.taigamobile.strings.generated.resources.userstory_slug
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.models.getUserStory
import com.grappim.taigamobile.testing.models.getUserStoryDetailsData
import com.grappim.taigamobile.testing.repo.EpicLinkCall
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.usecases.FakeUserStoryDetailsDataUseCase
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.createTestPlatformFile
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.formatter.decimal.createDecimalFormatter
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class UserStoryDetailsViewModelTest {
    private val userStoryId = getRandomLong()
    private val ref = getRandomLong()

    private val type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)

    private val savedStateHandle = SavedStateHandle(
        mapOf("userStoryId" to userStoryId, "ref" to ref)
    )

    private val mainDispatcherRule = MainDispatcherRule()

    private val statusUIMapper = StatusUIMapper()
    private val tagUIMapper = TagUIMapper()
    private val workItemsGenerator = WorkItemsGenerator(
        dispatcher = UnconfinedTestDispatcher(),
        statusUIMapper = statusUIMapper
    )
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()
    private val userStoryDetailsDataUseCase = FakeUserStoryDetailsDataUseCase()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val historyRepository: HistoryRepository = FakeHistoryRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage()
    private val usersRepository = FakeUsersRepository()
    private val epicsRepository = FakeEpicsRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val customFieldsUIMapper = CustomFieldsUIMapper(dfSimple = createDecimalFormatter())

    private lateinit var sut: UserStoryDetailsViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = UserStoryDetailsViewModel(
            savedStateHandle = savedStateHandle,
            userStoryDetailsDataUseCase = userStoryDetailsDataUseCase,
            workItemsGenerator = workItemsGenerator,
            workItemEditStateRepository = workItemEditStateRepository,
            patchDataGenerator = patchDataGenerator,
            statusUIMapper = statusUIMapper,
            tagUIMapper = tagUIMapper,
            dateTimeUtils = dateTimeUtils,
            customFieldsUIMapper = customFieldsUIMapper,
            historyRepository = historyRepository,
            workItemRepository = workItemRepository,
            taigaSessionStorage = taigaSessionStorage,
            usersRepository = usersRepository,
            epicsRepository = epicsRepository
        )
    }

    private fun setupSuccessfulLoad(userStory: UserStory = getUserStory(id = userStoryId)) {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.success(
            getUserStoryDetailsData(userStory = userStory)
        )
        userStoryDetailsDataUseCase.getUserStoryResult = userStory
    }

    private fun setupFailedLoad() {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.failure(testException)
    }

    private fun userStoryWithEpics(vararg epicIds: Long): UserStory = getUserStory(id = userStoryId)
        .copy(
            userStoryEpics = epicIds.map { epicId ->
                UserStoryEpic(
                    id = epicId,
                    title = getRandomString(),
                    ref = getRandomLong(),
                    color = getRandomString()
                )
            }.toPersistentList()
        )

    @Test
    fun `initial state should have correct toolbar title`() {
        setupSuccessfulLoad()

        createViewModel()

        val state = sut.state.value
        val toolbarTitle = state.toolbarTitle as NativeText.Arguments
        assertEquals(RString.userstory_slug, toolbarTitle.stringResource)
        assertEquals(listOf(ref), toolbarTitle.args)
    }

    @Test
    fun `loadUserStory success should update state correctly`() {
        val userStory = getUserStory(id = userStoryId)
        val userStoryDetailsData = getUserStoryDetailsData(userStory = userStory)
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.success(userStoryDetailsData)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentUserStory)
        assertNotNull(state.originalUserStory)
        assertEquals(userStoryDetailsData.sprint, state.sprint)
        assertEquals(userStoryDetailsData.creator, state.creator)
        assertEquals(userStoryDetailsData.filtersData, state.filtersData)
        assertTrue(state.canEditUserStory)
        assertTrue(state.canDeleteUserStory)
        assertTrue(state.canComment)
        assertTrue(state.canModifyRelatedEpic)
    }

    @Test
    fun `loadUserStory failure should update state with error`() {
        userStoryDetailsDataUseCase.getUserStoryDataResult = Result.failure(testException)

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertTrue(state.initialLoadError !is NativeText.Empty)
    }

    @Test
    fun `setDropdownMenuExpanded should update state`() {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(true)

        assertTrue(sut.state.value.isDropdownMenuExpanded)

        sut.state.value.setDropdownMenuExpanded(false)

        assertFalse(sut.state.value.isDropdownMenuExpanded)
    }

    @Test
    fun `retryLoadUserStory should reload data`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.retryLoadUserStory()

        assertEquals(2, userStoryDetailsDataUseCase.getUserStoryDataCallCount)
    }

    @Test
    fun `setIsDeleteDialogVisible should update state`() {
        setupSuccessfulLoad()
        createViewModel()

        assertFalse(sut.state.value.isDeleteDialogVisible)

        sut.state.value.setIsDeleteDialogVisible(true)

        assertTrue(sut.state.value.isDeleteDialogVisible)

        sut.state.value.setIsDeleteDialogVisible(false)

        assertFalse(sut.state.value.isDeleteDialogVisible)
    }

    @Test
    fun `onDelete success should emit delete trigger`() = runTest {
        setupSuccessfulLoad()
        userStoryDetailsDataUseCase.deleteUserStoryResult = Result.success(Unit)

        createViewModel()

        sut.deleteTrigger.test {
            sut.state.value.onDelete()

            assertTrue(awaitItem())
        }

        assertEquals(1, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `onDelete failure should update state and not emit trigger`() {
        setupSuccessfulLoad()
        userStoryDetailsDataUseCase.deleteUserStoryResult = Result.failure(testException)

        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(1, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `onEditTags should set tags in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onEditTags()

        assertTrue(workItemEditStateRepository.getCurrentTags(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditWatchers should set current watchers in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditWatchers()

        assertTrue(workItemEditStateRepository.getCurrentWatchers(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditAssignees should set current assignees in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditAssignees()

        assertTrue(workItemEditStateRepository.getCurrentAssignees(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditEpics should set current epics in repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onGoingToEditEpics()

        assertTrue(workItemEditStateRepository.getCurrentEpics(userStoryId, type).isNotEmpty())
    }

    @Test
    fun `onGoingToEditEpics without a loaded user story should set no epics`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onGoingToEditEpics()

        assertTrue(workItemEditStateRepository.getCurrentEpics(userStoryId, type).isEmpty())
    }

    @Test
    fun `loadUserStory with a null status should still populate the state`() {
        setupSuccessfulLoad(getUserStory(id = userStoryId).copy(status = null))

        createViewModel()

        val state = sut.state.value
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.initialLoadError)
        assertNotNull(state.currentUserStory)
    }

    @Test
    fun `onAttachmentAdd with a null file should set the error and not call the repository`() {
        setupSuccessfulLoad()
        createViewModel()

        sut.state.value.onAttachmentAdd(null)

        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertTrue(workItemRepository.addAttachmentCalls.isEmpty())
    }

    /**
     * [io.github.vinceglb.filekit.PlatformFile.readBytes] runs on a real IO dispatcher, so the
     * `viewModelScope.launch` does not complete before the call returns the way every other
     * handler here does. The state has to be awaited rather than read.
     */
    @Test
    fun `onAttachmentAdd with a file should add the attachment`() = runTest {
        setupSuccessfulLoad()
        val attachment = getAttachment()
        workItemRepository.addAttachmentResult = attachment
        createViewModel()

        sut.attachmentsState.test {
            val initial = awaitItem()

            sut.state.value.onAttachmentAdd(
                createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3))
            )

            assertTrue(awaitItem().areAttachmentsLoading)

            val loaded = awaitItem()
            assertFalse(loaded.areAttachmentsLoading)
            assertEquals(initial.attachments + attachment, loaded.attachments)
        }

        assertEquals(1, workItemRepository.addAttachmentCalls.size)
        assertEquals(userStoryId, workItemRepository.addAttachmentCalls.first().workItemId)
    }

    @Test
    fun `onDelete without a loaded user story should set the error and not delete`() {
        setupFailedLoad()
        createViewModel()

        sut.state.value.onDelete()

        assertFalse(sut.state.value.isLoading)
        assertEquals(NativeText.Resource(RString.common_error_message), sut.state.value.error)
        assertEquals(0, userStoryDetailsDataUseCase.deleteUserStoryCallCount)
    }

    @Test
    fun `single assignee update should be ignored`() = runTest {
        setupSuccessfulLoad()
        createViewModel()

        workItemEditStateRepository.updateAssignee(userStoryId, type, getRandomLong())

        assertTrue(workItemRepository.patchDataCalls.isEmpty())
    }

    @Test
    fun `assignees update should patch the assignees and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.patchDataResult = PatchedData(
            newVersion = newVersion,
            dueDateStatus = null
        )
        usersRepository.getUsersListResult = persistentListOf(getUser())
        createViewModel()

        workItemEditStateRepository.updateAssignees(
            userStoryId,
            type,
            persistentListOf(getRandomLong())
        )

        assertEquals(1, workItemRepository.patchDataCalls.size)
        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
    }

    @Test
    fun `watchers update should update the watchers and bump the version`() = runTest {
        val newVersion = getRandomLong()
        setupSuccessfulLoad()
        workItemRepository.updateWatchersDataResult = WatchersListUpdateData(
            version = newVersion,
            isWatchedByMe = true,
            watchers = persistentListOf(getUser())
        )
        createViewModel()

        workItemEditStateRepository.updateWatchers(
            userStoryId,
            type,
            persistentListOf(getRandomLong())
        )

        assertEquals(newVersion, sut.state.value.currentUserStory?.version)
        assertTrue(sut.watchersState.value.isWatchedByMe)
    }

    @Test
    fun `onEpicRemoveClick success should unlink the epic and reload the user story`() {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        val reloaded = userStoryWithEpics()
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        sut.state.value.onEpicRemoveClick(epicId)

        assertEquals(
            listOf(EpicLinkCall(epicId = epicId, userStoryId = userStoryId)),
            epicsRepository.unlinkFromEpicCalls
        )
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertEquals(reloaded, sut.state.value.originalUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicRemoveClick failure should show a snackbar and stop loading`() = runTest {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        epicsRepository.unlinkFromEpicThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            sut.state.value.onEpicRemoveClick(epicId)

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with unchanged ids should do nothing`() = runTest {
        val epicId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(epicId))
        createViewModel()

        workItemEditStateRepository.updateEpics(userStoryId, type, persistentListOf(epicId))

        assertTrue(epicsRepository.linkToEpicCalls.isEmpty())
        assertTrue(epicsRepository.unlinkFromEpicCalls.isEmpty())
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with an added id should link the epic and reload`() = runTest {
        val keptId = getRandomLong()
        val addedId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(keptId))
        val reloaded = userStoryWithEpics(keptId, addedId)
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        workItemEditStateRepository.updateEpics(
            userStoryId,
            type,
            persistentListOf(keptId, addedId)
        )

        assertEquals(
            listOf(EpicLinkCall(epicId = addedId, userStoryId = userStoryId)),
            epicsRepository.linkToEpicCalls
        )
        assertTrue(epicsRepository.unlinkFromEpicCalls.isEmpty())
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate with only a removed id should unlink the epic and reload`() = runTest {
        val removedId = getRandomLong()
        setupSuccessfulLoad(userStoryWithEpics(removedId))
        val reloaded = userStoryWithEpics()
        userStoryDetailsDataUseCase.getUserStoryResult = reloaded
        createViewModel()

        workItemEditStateRepository.updateEpics(userStoryId, type, persistentListOf())

        assertEquals(
            listOf(EpicLinkCall(epicId = removedId, userStoryId = userStoryId)),
            epicsRepository.unlinkFromEpicCalls
        )
        assertTrue(epicsRepository.linkToEpicCalls.isEmpty())
        assertEquals(reloaded, sut.state.value.currentUserStory)
        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }

    @Test
    fun `onEpicsUpdate failure should show a snackbar and stop loading`() = runTest {
        setupSuccessfulLoad(userStoryWithEpics())
        epicsRepository.linkToEpicThrows = testException
        createViewModel()

        sut.snackBarMessage.test {
            workItemEditStateRepository.updateEpics(
                userStoryId,
                type,
                persistentListOf(getRandomLong())
            )

            assertTrue(awaitItem() !is NativeText.Empty)
        }

        assertFalse(sut.state.value.areUserStoryEpicsLoading)
    }
}
