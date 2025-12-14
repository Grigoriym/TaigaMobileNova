package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.feature.projects.domain.Project
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProjectMapper @Inject constructor(@IoDispatcher private val ioDispatcher: CoroutineDispatcher) {
    suspend fun toProject(dto: ProjectDTO): Project = withContext(ioDispatcher) {
        Project(
            id = dto.id,
            name = dto.name,
            slug = dto.slug,
            isMember = dto.isMember,
            isAdmin = dto.isAdmin,
            isOwner = dto.isOwner,
            description = dto.description,
            avatarUrl = dto.avatarUrl,
            members = dto.members,
            fansCount = dto.fansCount,
            watchersCount = dto.watchersCount,
            isPrivate = dto.isPrivate
        )
    }

    suspend fun toListDomain(dto: List<ProjectDTO>): ImmutableList<Project> =
        dto.map { toProject(it) }.toImmutableList()
}
