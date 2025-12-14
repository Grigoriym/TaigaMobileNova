package com.grappim.taigamobile.feature.profile.domain

import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class GetProfileDataUseCase @Inject constructor(
    private val usersRepository: UsersRepository,
    private val projectsRepository: ProjectsRepository
) {

    suspend fun getProfileData(userId: Long): Result<ProfileData> = resultOf {
        coroutineScope {
            val user = async { usersRepository.getUser(userId) }
            val userStats = async { usersRepository.getUserStats(userId) }
            val userProjects = async { projectsRepository.getUserProjects(userId) }

            ProfileData(
                user = user.await(),
                userStats = userStats.await(),
                projects = userProjects.await()
            )
        }
    }
}
