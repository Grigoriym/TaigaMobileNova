package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetKanbanDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val userStoriesRepository: UserStoriesRepository
) {
    suspend fun getData(): Result<KanbanData> = resultOf {
        coroutineScope {
            val userStories = async { userStoriesRepository.getUserStories() }
            val teamMembers = async { usersRepository.getTeamMembers(false) }
            val filters = async { filtersRepository.getFiltersData(CommonTaskType.UserStory) }
            val swimlanes = swimlanesRepository.getSwimlanes()

            KanbanData(
                stories = userStories.await(),
                swimlanes = swimlanes,
                statuses = filters.await().statuses.filter {
                    it is Status
                }.toImmutableList(),
                teamMembers = teamMembers.await()
            )
        }
    }
}
