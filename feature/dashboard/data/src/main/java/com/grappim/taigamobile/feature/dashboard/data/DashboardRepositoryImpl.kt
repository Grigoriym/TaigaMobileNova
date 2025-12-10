package com.grappim.taigamobile.feature.dashboard.data

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.dashboard.domain.DashboardData
import com.grappim.taigamobile.feature.dashboard.domain.DashboardRepository
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val epicsRepository: EpicsRepository,
    private val userStoriesRepository: UserStoriesRepository,
    private val issuesRepository: IssuesRepository,
    private val session: Session,
    private val tasksRepository: TasksRepository
) : DashboardRepository {

    override suspend fun getData(): Result<DashboardData> = resultOf {
        coroutineScope {
            val workingOn = async { getWorkingOn() }
            val watching = async { getWatching() }
            val myProjects = async { projectsRepository.getMyProjects() }

            DashboardData(
                workingOn = workingOn.await(),
                watching = watching.await(),
                myProjectDTOS = myProjects.await()
            )
        }
    }

    private suspend fun getWorkingOn(): List<CommonTask> = coroutineScope {
        val epics = async {
            epicsRepository.getEpicsOld(
                assignedId = session.userId,
                isClosed = false
            )
        }

        val stories = async {
            userStoriesRepository.getUserStoriesOld(
                assignedId = session.userId,
                isClosed = false,
                isDashboard = true
            )
        }

        val tasks = async {
            tasksRepository.getTasks(assignedId = session.userId, isClosed = false)
        }

        val issues = async {
            issuesRepository.getIssues(
                isClosed = false,
                assignedIds = session.userId.toString()
            )
        }
        epics.await() + stories.await() + tasks.await() + issues.await()
    }

    private suspend fun getWatching(): List<CommonTask> = coroutineScope {
        val epics = async {
            epicsRepository.getEpicsOld(
                watcherId = session.userId,
                isClosed = false
            )
        }

        val stories = async {
            userStoriesRepository.getUserStoriesOld(
                watcherId = session.userId,
                isClosed = false,
                isDashboard = true
            )
        }

        val tasks = async {
            tasksRepository.getTasks(
                watcherId = session.userId,
                isClosed = false
            )
        }

        val issues = async {
            issuesRepository.getIssues(watcherId = session.userId, isClosed = false)
        }

        epics.await() + stories.await() + tasks.await() + issues.await()
    }
}
