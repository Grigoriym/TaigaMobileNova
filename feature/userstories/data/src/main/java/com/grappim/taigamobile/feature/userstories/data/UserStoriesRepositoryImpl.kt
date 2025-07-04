package com.grappim.taigamobile.feature.userstories.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.grappim.taigamobile.core.api.fixNullColor
import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.api.withIO
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.core.domain.commaString
import com.grappim.taigamobile.core.domain.tagsCommaString
import com.grappim.taigamobile.core.domain.toCommonTaskExtended
import com.grappim.taigamobile.core.domain.transformTaskTypeForCopyLink
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UserStoriesRepositoryImpl @Inject constructor(
    private val userStoriesApi: UserStoriesApi,
    private val taigaStorage: TaigaStorage,
    private val filtersRepository: FiltersRepository,
    private val swimlanesRepository: SwimlanesRepository,
    private val serverStorage: ServerStorage
) : UserStoriesRepository {
    override fun getUserStories(filters: FiltersData): Flow<PagingData<CommonTask>> = Pager(
        PagingConfig(
            pageSize = 20,
            enablePlaceholders = false
        )
    ) {
        UserStoriesPagingSource(userStoriesApi, filters, taigaStorage)
    }.flow

    override suspend fun getAllUserStories() = withIO {
        val filters = async { filtersRepository.getFiltersData(CommonTaskType.UserStory) }
        val swimlanes = async { swimlanesRepository.getSwimlanes() }

        userStoriesApi.getUserStories(project = taigaStorage.currentProjectIdFlow.first())
            .map { response ->
                response.toCommonTaskExtended(
                    commonTaskType = CommonTaskType.UserStory,
                    filters = filters.await(),
                    swimlanes = swimlanes.await(),
                    tags = response.tags.orEmpty()
                        .map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
                    url = "${serverStorage.server}/project/${response.projectExtraInfo.slug}/${
                        transformTaskTypeForCopyLink(
                            CommonTaskType.UserStory
                        )
                    }/${response.ref}"
                )
            }
    }

    override suspend fun getBacklogUserStories(page: Int, filters: FiltersData) = handle404 {
        userStoriesApi.getUserStories(
            project = taigaStorage.currentProjectIdFlow.first(),
            sprint = "null",
            page = page,
            query = filters.query,
            assignedIds = filters.assignees.commaString(),
            ownerIds = filters.createdBy.commaString(),
            roles = filters.roles.commaString(),
            statuses = filters.statuses.commaString(),
            epics = filters.epics.commaString(),
            tags = filters.tags.tagsCommaString()
        ).map { it.toCommonTask(CommonTaskType.UserStory) }
    }

    override suspend fun getUserStories(
        assignedId: Long?,
        isClosed: Boolean?,
        isDashboard: Boolean?,
        watcherId: Long?,
        epicId: Long?
    ): List<CommonTask> = userStoriesApi.getUserStories(
        assignedId = assignedId,
        isClosed = isClosed,
        isDashboard = isDashboard,
        epic = epicId
    ).map { it.toCommonTask(CommonTaskType.UserStory) }

    override suspend fun createUserStory(
        project: Long,
        subject: String,
        description: String,
        status: Long?,
        swimlane: Long?
    ): CommonTaskResponse = userStoriesApi.createUserStory(
        createUserStoryRequest = CreateUserStoryRequest(
            project = project,
            subject = subject,
            description = description,
            status = status,
            swimlane = swimlane
        )
    )

    override suspend fun getUserStoryByRef(projectId: Long, ref: Int): CommonTask =
        userStoriesApi.getUserStoryByRef(
            projectId = projectId,
            ref = ref
        ).toCommonTask(CommonTaskType.UserStory)
}
