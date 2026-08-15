package com.grappim.taigamobile.feature.projectselector.ui

import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.cleaner.FakeDataCleaner
import com.grappim.taigamobile.testing.models.getProject
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ProjectSelectorViewModelTest {

    private val mainDispatcherRule = MainDispatcherRule()

    private val projectsRepository = FakeProjectsRepository()
    private val filtersStorage = FakeFiltersStorage()
    private val taigaSessionStorage = FakeTaigaSessionStorage()
    private val dataCleaner = FakeDataCleaner()

    private lateinit var sut: ProjectSelectorViewModel

    private fun createViewModel(isFromLogin: Boolean = false): ProjectSelectorViewModel = ProjectSelectorViewModel(
        projectsRepository = projectsRepository,
        session = filtersStorage,
        taigaSessionStorage = taigaSessionStorage,
        dataCleaner = dataCleaner,
        route = ProjectSelectorNavDestination(isFromLogin = isFromLogin)
    )

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        sut = createViewModel()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    @Test
    fun `initial state has isFromLogin false when not from login`() {
        assertFalse(sut.state.value.isFromLogin)
    }

    @Test
    fun `initial state has isFromLogin true when from login`() {
        sut = createViewModel(isFromLogin = true)

        assertTrue(sut.state.value.isFromLogin)
    }

    @Test
    fun `initial state reflects current project id from session storage`() {
        val projectId = getRandomLong()
        val sessionStorage = FakeTaigaSessionStorage(currentProjectId = projectId)
        val vm = ProjectSelectorViewModel(
            projectsRepository = projectsRepository,
            session = filtersStorage,
            taigaSessionStorage = sessionStorage,
            dataCleaner = dataCleaner,
            route = ProjectSelectorNavDestination(isFromLogin = false)
        )

        assertEquals(projectId, vm.state.value.currentProjectId)
    }

    @Test
    fun `setProjectsQuery updates searchQuery`() {
        val query = "myproject"

        sut.state.value.setProjectsQuery(query)

        assertEquals(query, sut.searchQuery.value)
    }

    @Test
    fun `setProject saves project updates session and resets filters`() = runTest {
        val project = getProject()

        sut.state.value.setProject(project)

        assertTrue(projectsRepository.saveProjectCalled)
        assertEquals(project, projectsRepository.saveProjectCalledWith)
        assertEquals(project.id, taigaSessionStorage.currentProjectId)
        assertTrue(filtersStorage.resetFiltersCalled)
    }

    @Test
    fun `setProject updates currentProjectId in state`() = runTest {
        val project = getProject()

        sut.state.value.setProject(project)

        assertEquals(project.id, sut.state.value.currentProjectId)
    }

    @Test
    fun `onGoingBackAfterLogIn calls dataCleaner`() = runTest {
        sut.state.value.onGoingBackAfterLogIn()

        assertTrue(dataCleaner.cleanOnGoingBackAfterLoginCalled)
    }

    @Test
    fun `currentProjectId updates when session storage changes`() = runTest {
        val newId = getRandomLong()

        taigaSessionStorage.setCurrentProjectId(newId)

        assertEquals(newId, sut.state.value.currentProjectId)
    }
}
