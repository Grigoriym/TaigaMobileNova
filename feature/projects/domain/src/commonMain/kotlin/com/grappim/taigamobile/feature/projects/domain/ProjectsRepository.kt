package com.grappim.taigamobile.feature.projects.domain

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

interface ProjectsRepository {
    suspend fun fetchProjects(query: String): Flow<PagingData<Project>>
    suspend fun getMyProjects(): ImmutableList<Project>
    suspend fun getUserProjects(userId: Long): ImmutableList<Project>

    suspend fun saveProject(project: Project)

    suspend fun getCurrentProjectSimple(): ProjectSimple

    fun getCurrentProjectFlow(): Flow<ProjectSimple>

    suspend fun getPermissions(): ImmutableList<TaigaPermission>

    suspend fun fetchAndSaveProjectInfo()
    suspend fun getProjectDetails(): ProjectDetails

    suspend fun updateProject(
        name: String,
        description: String,
        isPrivate: Boolean,
        isLookingForPeople: Boolean,
        lookingForPeopleNote: String,
        isContactActivated: Boolean
    )

    suspend fun getProjectModules(): ProjectModules
    suspend fun updateModules(
        isEpicsActivated: Boolean,
        isBacklogActivated: Boolean,
        isKanbanActivated: Boolean,
        isIssuesActivated: Boolean,
        isWikiActivated: Boolean,
        totalMilestones: Int?,
        totalStoryPoints: Double?
    )

    suspend fun getTagsColors(): ImmutableList<Tag>
    suspend fun deleteTag(tagName: String)
    suspend fun createTag(tagName: String, color: String)
    suspend fun editTag(fromTagName: String, toTagName: String?, color: String?)
    suspend fun mixTags(fromTags: List<String>, toTag: String)
}
