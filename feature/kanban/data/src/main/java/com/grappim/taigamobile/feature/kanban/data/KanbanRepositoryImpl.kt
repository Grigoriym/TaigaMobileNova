package com.grappim.taigamobile.feature.kanban.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.kanban.domain.KanbanData
import com.grappim.taigamobile.feature.kanban.domain.KanbanRepository
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.collections.map

class KanbanRepositoryImpl @Inject constructor(
    private val usersRepository: UsersRepository,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val userStoriesRepository: UserStoriesRepository
) : KanbanRepository {

    override suspend fun getData(): Result<KanbanData> = resultOf {
        coroutineScope {
            val userStories = async { userStoriesRepository.getAllUserStories() }
            val users = async { usersRepository.getTeamSimpleOld() }
            val filters = async { filtersRepository.getStatuses(CommonTaskType.UserStory) }
            val swimlanes = async { swimlanesRepository.getSwimlanes() }

            KanbanData(
                stories = userStories.await(),
                swimlaneDTOS = swimlanes.await(),
                statusOlds = filters.await(),
                team = users.await().map { it.toUser() }
            )
        }
    }
}
