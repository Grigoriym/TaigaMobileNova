package com.grappim.taigamobile.viewmodels

import com.grappim.taigamobile.domain.entities.TeamMember
import com.grappim.taigamobile.team.TeamViewModel
import com.grappim.taigamobile.ui.utils.ErrorResult
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.viewmodels.utils.assertResultEquals
import com.grappim.taigamobile.viewmodels.utils.notFoundException
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs


class TeamViewModelTest : BaseViewModelTest() {
    private lateinit var viewModel: TeamViewModel

    @BeforeTest
    fun setup() {
        viewModel = TeamViewModel(mockAppComponent)
    }

    @Test
    fun `test on open`(): Unit = runBlocking {
        val listTeamMember = mockk<List<TeamMember>>(relaxed = true)

        coEvery { mockUsersRepository.getTeam() } returns listTeamMember
        viewModel.onOpen()

        assertResultEquals(SuccessResult(listTeamMember), viewModel.team.value)

    }

    @Test
    fun `test on open error`(): Unit = runBlocking {
        coEvery { mockUsersRepository.getTeam() } throws notFoundException
        viewModel.onOpen()

        assertIs<ErrorResult<List<TeamMember>>>(viewModel.team.value)

    }
}
