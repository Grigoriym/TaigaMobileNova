package com.grappim.taigamobile.feature.epics.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class EpicDetailsDataUseCase @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val epicsRepository: EpicsRepository,
    private val historyRepository: HistoryRepository,
    private val workItemRepository: WorkItemRepository,
    private val usersRepository: UsersRepository
) {

    suspend fun getEpicData(epicId: Long): Result<EpicDetailsData> = resultOf {
        coroutineScope {
            val taskType = CommonTaskType.Epic
            val filtersData = filtersRepository.getFiltersData(taskType)

            val epicDeferred = async {
                epicsRepository.getEpic(epicId)
            }

            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = epicId,
                    commonTaskType = taskType
                )
            }

            val customFields = async {
                workItemRepository.getCustomFields(
                    workItemId = epicId,
                    commonTaskType = taskType
                )
            }
            val commentsDeferred = async {
                historyRepository.getComments(
                    commonTaskId = epicId,
                    type = taskType
                )
            }

            val epic = epicDeferred.await()

            val creator = async { usersRepository.getUser(epic.creatorId) }

            val assigneesDeferred =
                async { usersRepository.getUsersList(epic.assignedUserIds) }
            val watchersDeferred = async { usersRepository.getUsersList(epic.watcherUserIds) }

            val assignees = assigneesDeferred.await()
            val watchers = watchersDeferred.await()

            val isAssignedToMe = async { usersRepository.isAnyAssignedToMe(assignees) }
            val isWatchedByMe = async { usersRepository.isAnyAssignedToMe(watchers) }

            EpicDetailsData(
                epic = epic,
                attachments = attachments.await(),
                customFields = customFields.await(),
                comments = commentsDeferred.await(),
                filtersData = filtersData,
                creator = creator.await(),
                assignees = assignees,
                watchers = watchers,
                isAssignedToMe = isAssignedToMe.await(),
                isWatchedByMe = isWatchedByMe.await()
            )
        }
    }
}
