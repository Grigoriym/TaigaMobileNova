package com.grappim.taigamobile.feature.kanban.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.Statuses
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddUserStory
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
            val project = async { projectsRepository.getCurrentProjectSimple() }
            val userStories = async { userStoriesRepository.getUserStories() }
            val teamMembers = async { usersRepository.getTeamMembers(false) }
            val filters = async { filtersRepository.getStatuses(CommonTaskType.UserStory) }
            val swimlanes = swimlanesRepository.getSwimlanes()
            val defaultSwimlane = swimlanes.find { it.id == project.await().defaultSwimlane }
                ?: swimlanes.firstOrNull()

            val stories = userStories.await().sortedBy { it.kanbanOrder }.toImmutableList()
            val statuses = filters.await()
            val members = teamMembers.await()

            val storiesByStatus = computeStoriesByStatus(
                stories = stories,
                statuses = statuses,
                teamMembers = members,
                swimlane = defaultSwimlane
            )

            KanbanData(
                stories = stories,
                swimlanes = swimlanes,
                statuses = statuses,
                teamMembers = members,
                canAddUserStory = projectsRepository.getPermissions().canAddUserStory(),
                defaultSwimlane = defaultSwimlane,
                storiesByStatus = storiesByStatus
            )
        }
    }

    suspend fun computeStoriesByStatus(
        stories: ImmutableList<UserStory>,
        statuses: ImmutableList<Statuses>,
        teamMembers: ImmutableList<TeamMember>,
        swimlane: Swimlane?
    ): ImmutableMap<Statuses, ImmutableList<KanbanUserStory>> = withContext(Dispatchers.Default) {
        val teamMembersById = teamMembers.associateBy { it.id }
        val filteredStories = stories.filter { it.swimlane == swimlane?.id }

        statuses.associateWith { status ->
            filteredStories
                .filter { it.status == status }
                .map { story ->
                    KanbanUserStory(
                        userStory = story,
                        assignees = story.assignedUserIds
                            .mapNotNull { id -> teamMembersById[id] }
                            .toImmutableList()
                    )
                }
                .toImmutableList()
        }.toImmutableMap()
    }
}
