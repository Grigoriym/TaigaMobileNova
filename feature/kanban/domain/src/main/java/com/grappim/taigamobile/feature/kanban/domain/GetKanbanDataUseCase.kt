package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetKanbanDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val projectsRepository: ProjectsRepository
) {
    suspend fun getData(): Result<KanbanData> = resultOf {
        coroutineScope {
            val userStories = async { userStoriesRepository.getUserStories() }
            val teamMembers = async { usersRepository.getTeamMembers(false) }
            val filters = async { filtersRepository.getStatuses(CommonTaskType.UserStory) }
            val swimlanes = swimlanesRepository.getSwimlanes()

            KanbanData(
                stories = userStories.await(),
                swimlanes = swimlanes,
                statuses = filters.await(),
                teamMembers = teamMembers.await(),
                canAddUserStory = projectsRepository.getPermissions().canAddUserStory()
            )
        }
    }
}
