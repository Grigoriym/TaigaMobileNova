package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.api.handle404
import com.grappim.taigamobile.core.api.toCommonTask
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TasksRepositoryImpl @Inject constructor(
    private val tasksApi: TasksApi,
    private val taigaStorage: TaigaStorage
) : TasksRepository {
    override suspend fun getUserStoryTasks(storyId: Long) = handle404 {
        tasksApi.getTasks(userStory = storyId, project = taigaStorage.currentProjectIdFlow.first())
            .map { it.toCommonTask(CommonTaskType.Task) }
    }

    override suspend fun getTasks(
        assignedId: Long?,
        isClosed: Boolean?,
        watcherId: Long?
    ): List<CommonTask> = tasksApi.getTasks(
        assignedId = assignedId,
        isClosed = isClosed,
        watcherId = watcherId
    ).map { it.toCommonTask(CommonTaskType.Task) }

//    override suspend fun getCommonTask(commonTaskId: Long, type: CommonTaskType) = withIO {
//        val filters = async { filtersRepository.getFiltersDataOld(type) }
//        val swimlanes = async { swimlanesRepository.getSwimlanes() }
//
//        val task = taigaApi.getCommonTask(CommonTaskPathPlural(type), commonTaskId)
//        val sprint = task.milestone?.let {
//            sprintApi.getSprint(it).toSprint()
//        }
//        task.toCommonTaskExtended(
//            commonTaskType = type,
//            filters = filters.await(),
//            swimlanes = swimlanes.await(),
//            sprint = sprint,
//            tags = task.tags.orEmpty()
//                .map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
//            url = "${serverStorage.server}/project/${task.projectExtraInfo.slug}/${
//                transformTaskTypeForCopyLink(type)
//            }/${task.ref}"
//        )
//    }
}
