package com.grappim.taigamobile.testing.usecases

import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.kanban.domain.GetKanbanDataUseCase
import com.grappim.taigamobile.feature.kanban.domain.KanbanData
import com.grappim.taigamobile.feature.kanban.domain.KanbanUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

class FakeGetKanbanDataUseCase : GetKanbanDataUseCase {

    var getDataResult: Result<KanbanData> = Result.failure(IllegalStateException("getDataResult not set"))
    var computeStoriesByStatusResult: ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> = persistentMapOf()
    var getDataCallCount = 0

    override suspend fun getData(storageSwimlane: Long?): Result<KanbanData> {
        getDataCallCount++
        return getDataResult
    }

    override suspend fun computeStoriesByStatus(
        stories: ImmutableList<UserStory>,
        statuses: ImmutableList<Statuses>,
        teamMembers: ImmutableList<TeamMember>,
        swimlane: Swimlane?
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> = computeStoriesByStatusResult
}
