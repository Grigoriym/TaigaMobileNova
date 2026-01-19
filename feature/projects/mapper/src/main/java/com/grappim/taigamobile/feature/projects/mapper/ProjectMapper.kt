package com.grappim.taigamobile.feature.projects.mapper

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectExtraInfoDTO
import com.grappim.taigamobile.feature.projects.dto.TaigaPermissionDTO
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
            members = dto.members.toImmutableList(),
            fansCount = dto.fansCount,
            watchersCount = dto.watchersCount,
            isPrivate = dto.isPrivate,
            myPermissions = dto.myPermissions.map { it.toDomain() }.toImmutableList(),
            isEpicsActivated = dto.isEpicsActivated,
            isBacklogActivated = dto.isBacklogActivated,
            isKanbanActivated = dto.isKanbanActivated,
            isIssuesActivated = dto.isIssuesActivated,
            isWikiActivated = dto.isWikiActivated,
            defaultSwimlane = dto.defaultSwimlane
        )
    }

    fun toProjectExtraInfo(dto: ProjectExtraInfoDTO): ProjectExtraInfo = ProjectExtraInfo(
        id = dto.id,
        name = dto.name,
        slug = dto.slug,
        logoSmallUrl = dto.logoSmallUrl
    )

    suspend fun toProjectSimple(entity: ProjectEntity): ProjectSimple = withContext(ioDispatcher) {
        ProjectSimple(
            id = entity.id,
            name = entity.name,
            slug = entity.slug,
            myPermissions = entity.myPermissions.toImmutableList(),
            isEpicsActivated = entity.isEpicsActivated,
            isBacklogActivated = entity.isBacklogActivated,
            isKanbanActivated = entity.isKanbanActivated,
            isIssuesActivated = entity.isIssuesActivated,
            isWikiActivated = entity.isWikiActivated,
            defaultSwimlane = entity.defaultSwimlane
        )
    }

    suspend fun toListDomain(dto: List<ProjectDTO>): ImmutableList<Project> =
        dto.map { toProject(it) }.toImmutableList()

    fun toEntity(project: Project): ProjectEntity = ProjectEntity(
        id = project.id,
        name = project.name,
        slug = project.slug,
        myPermissions = project.myPermissions,
        isEpicsActivated = project.isEpicsActivated,
        isBacklogActivated = project.isBacklogActivated,
        isKanbanActivated = project.isKanbanActivated,
        isIssuesActivated = project.isIssuesActivated,
        isWikiActivated = project.isWikiActivated,
        defaultSwimlane = project.defaultSwimlane
    )

    fun toEntity(dto: ProjectDTO): ProjectEntity = ProjectEntity(
        id = dto.id,
        name = dto.name,
        slug = dto.slug,
        myPermissions = dto.myPermissions.map { it.toDomain() },
        isEpicsActivated = dto.isEpicsActivated,
        isBacklogActivated = dto.isBacklogActivated,
        isKanbanActivated = dto.isKanbanActivated,
        isIssuesActivated = dto.isIssuesActivated,
        isWikiActivated = dto.isWikiActivated,
        defaultSwimlane = dto.defaultSwimlane
    )

    private fun TaigaPermissionDTO.toDomain(): TaigaPermission = when (this) {
        TaigaPermissionDTO.VIEW_PROJECT -> TaigaPermission.VIEW_PROJECT
        TaigaPermissionDTO.VIEW_MILESTONES -> TaigaPermission.VIEW_MILESTONES
        TaigaPermissionDTO.ADD_MILESTONE -> TaigaPermission.ADD_MILESTONE
        TaigaPermissionDTO.MODIFY_MILESTONE -> TaigaPermission.MODIFY_MILESTONE
        TaigaPermissionDTO.DELETE_MILESTONE -> TaigaPermission.DELETE_MILESTONE
        TaigaPermissionDTO.VIEW_EPICS -> TaigaPermission.VIEW_EPICS
        TaigaPermissionDTO.ADD_EPIC -> TaigaPermission.ADD_EPIC
        TaigaPermissionDTO.MODIFY_EPIC -> TaigaPermission.MODIFY_EPIC
        TaigaPermissionDTO.COMMENT_EPIC -> TaigaPermission.COMMENT_EPIC
        TaigaPermissionDTO.DELETE_EPIC -> TaigaPermission.DELETE_EPIC
        TaigaPermissionDTO.VIEW_US -> TaigaPermission.VIEW_US
        TaigaPermissionDTO.ADD_US -> TaigaPermission.ADD_US
        TaigaPermissionDTO.MODIFY_US -> TaigaPermission.MODIFY_US
        TaigaPermissionDTO.COMMENT_US -> TaigaPermission.COMMENT_US
        TaigaPermissionDTO.DELETE_US -> TaigaPermission.DELETE_US
        TaigaPermissionDTO.VIEW_TASKS -> TaigaPermission.VIEW_TASKS
        TaigaPermissionDTO.ADD_TASK -> TaigaPermission.ADD_TASK
        TaigaPermissionDTO.MODIFY_TASK -> TaigaPermission.MODIFY_TASK
        TaigaPermissionDTO.COMMENT_TASK -> TaigaPermission.COMMENT_TASK
        TaigaPermissionDTO.DELETE_TASK -> TaigaPermission.DELETE_TASK
        TaigaPermissionDTO.VIEW_ISSUES -> TaigaPermission.VIEW_ISSUES
        TaigaPermissionDTO.ADD_ISSUE -> TaigaPermission.ADD_ISSUE
        TaigaPermissionDTO.MODIFY_ISSUE -> TaigaPermission.MODIFY_ISSUE
        TaigaPermissionDTO.COMMENT_ISSUE -> TaigaPermission.COMMENT_ISSUE
        TaigaPermissionDTO.DELETE_ISSUE -> TaigaPermission.DELETE_ISSUE
        TaigaPermissionDTO.VIEW_WIKI_PAGES -> TaigaPermission.VIEW_WIKI_PAGES
        TaigaPermissionDTO.ADD_WIKI_PAGE -> TaigaPermission.ADD_WIKI_PAGE
        TaigaPermissionDTO.MODIFY_WIKI_PAGE -> TaigaPermission.MODIFY_WIKI_PAGE
        TaigaPermissionDTO.COMMENT_WIKI_PAGE -> TaigaPermission.COMMENT_WIKI_PAGE
        TaigaPermissionDTO.DELETE_WIKI_PAGE -> TaigaPermission.DELETE_WIKI_PAGE
        TaigaPermissionDTO.VIEW_WIKI_LINKS -> TaigaPermission.VIEW_WIKI_LINKS
        TaigaPermissionDTO.ADD_WIKI_LINK -> TaigaPermission.ADD_WIKI_LINK
        TaigaPermissionDTO.MODIFY_WIKI_LINK -> TaigaPermission.MODIFY_WIKI_LINK
        TaigaPermissionDTO.DELETE_WIKI_LINK -> TaigaPermission.DELETE_WIKI_LINK
        TaigaPermissionDTO.MODIFY_PROJECT -> TaigaPermission.MODIFY_PROJECT
        TaigaPermissionDTO.DELETE_PROJECT -> TaigaPermission.DELETE_PROJECT
        TaigaPermissionDTO.ADD_MEMBER -> TaigaPermission.ADD_MEMBER
        TaigaPermissionDTO.REMOVE_MEMBER -> TaigaPermission.REMOVE_MEMBER
        TaigaPermissionDTO.ADMIN_PROJECT_VALUES -> TaigaPermission.ADMIN_PROJECT_VALUES
        TaigaPermissionDTO.ADMIN_ROLES -> TaigaPermission.ADMIN_ROLES
    }
}
