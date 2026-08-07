package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectDetails
import com.grappim.taigamobile.feature.projects.domain.ProjectModules
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProjectsRepository : ProjectsRepository {

    var permissions: ImmutableList<TaigaPermission> = persistentListOf(TaigaPermission.MODIFY_PROJECT)
    var getPermissionsThrows: Throwable? = null

    var fetchProjectsResult: ImmutableList<Project> = persistentListOf()
    val fetchProjectsCalls: MutableList<String> = mutableListOf()

    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> {
        fetchProjectsCalls += query
        return if (fetchProjectsResult.isEmpty()) {
            flowOf(PagingData.empty())
        } else {
            flowOf(PagingData.from(fetchProjectsResult))
        }
    }

    override suspend fun getMyProjects(): ImmutableList<Project> = error("not used in this test")

    var getUserProjectsResult: ImmutableList<Project> = persistentListOf()
    var getUserProjectsThrows: Throwable? = null

    override suspend fun getUserProjects(userId: Long): ImmutableList<Project> {
        getUserProjectsThrows?.let { throw it }
        return getUserProjectsResult
    }

    var saveProjectCalled = false
    var saveProjectCalledWith: Project? = null

    override suspend fun saveProject(project: Project) {
        saveProjectCalled = true
        saveProjectCalledWith = project
    }

    var getCurrentProjectSimpleResult: ProjectSimple? = null
    var getCurrentProjectSimpleThrows: Throwable? = null

    override suspend fun getCurrentProjectSimple(): ProjectSimple {
        getCurrentProjectSimpleThrows?.let { throw it }
        return getCurrentProjectSimpleResult ?: error("getCurrentProjectSimpleResult not set")
    }

    var projectFlow: Flow<ProjectSimple> = flowOf()
    override fun getCurrentProjectFlow(): Flow<ProjectSimple> = projectFlow

    override suspend fun getPermissions(): ImmutableList<TaigaPermission> {
        getPermissionsThrows?.let { throw it }
        return permissions
    }

    var fetchAndSaveProjectInfoCalled = false
    var fetchAndSaveProjectInfoThrows: Throwable? = null

    override suspend fun fetchAndSaveProjectInfo() {
        fetchAndSaveProjectInfoCalled = true
        fetchAndSaveProjectInfoThrows?.let { throw it }
    }

    var getProjectDetailsResult: ProjectDetails? = null
    var getProjectDetailsThrows: Throwable? = null

    override suspend fun getProjectDetails(): ProjectDetails {
        getProjectDetailsThrows?.let { throw it }
        return getProjectDetailsResult ?: error("getProjectDetailsResult not set")
    }

    var getProjectModulesResult: ProjectModules? = null
    var getProjectModulesThrows: Throwable? = null

    override suspend fun getProjectModules(): ProjectModules {
        getProjectModulesThrows?.let { throw it }
        return getProjectModulesResult ?: error("getProjectModulesResult not set")
    }

    data class UpdateModulesCall(
        val isEpicsActivated: Boolean,
        val isBacklogActivated: Boolean,
        val isKanbanActivated: Boolean,
        val isIssuesActivated: Boolean,
        val isWikiActivated: Boolean,
        val totalMilestones: Int?,
        val totalStoryPoints: Double?
    )

    var updateModulesCalled = false
    var updateModulesThrows: Throwable? = null
    val updateModulesCalls: MutableList<UpdateModulesCall> = mutableListOf()

    override suspend fun updateModules(
        isEpicsActivated: Boolean,
        isBacklogActivated: Boolean,
        isKanbanActivated: Boolean,
        isIssuesActivated: Boolean,
        isWikiActivated: Boolean,
        totalMilestones: Int?,
        totalStoryPoints: Double?
    ) {
        updateModulesCalled = true
        updateModulesCalls += UpdateModulesCall(
            isEpicsActivated = isEpicsActivated,
            isBacklogActivated = isBacklogActivated,
            isKanbanActivated = isKanbanActivated,
            isIssuesActivated = isIssuesActivated,
            isWikiActivated = isWikiActivated,
            totalMilestones = totalMilestones,
            totalStoryPoints = totalStoryPoints
        )
        updateModulesThrows?.let { throw it }
    }

    data class UpdateProjectCall(
        val name: String,
        val description: String,
        val isPrivate: Boolean,
        val isLookingForPeople: Boolean,
        val lookingForPeopleNote: String,
        val isContactActivated: Boolean
    )

    var updateProjectCalled = false
    var updateProjectThrows: Throwable? = null
    val updateProjectCalls: MutableList<UpdateProjectCall> = mutableListOf()

    override suspend fun updateProject(
        name: String,
        description: String,
        isPrivate: Boolean,
        isLookingForPeople: Boolean,
        lookingForPeopleNote: String,
        isContactActivated: Boolean,
    ) {
        updateProjectCalled = true
        updateProjectCalls += UpdateProjectCall(
            name = name,
            description = description,
            isPrivate = isPrivate,
            isLookingForPeople = isLookingForPeople,
            lookingForPeopleNote = lookingForPeopleNote,
            isContactActivated = isContactActivated
        )
        updateProjectThrows?.let { throw it }
    }

    var getTagsColorsResult: ImmutableList<Tag>? = null
    var getTagsColorsThrows: Throwable? = null

    override suspend fun getTagsColors(): ImmutableList<Tag> {
        getTagsColorsThrows?.let { throw it }
        return getTagsColorsResult ?: error("getTagsColorsResult not set")
    }

    var deleteTagCalled = false
    var deleteTagTagName: String? = null
    var deleteTagThrows: Throwable? = null

    override suspend fun deleteTag(tagName: String) {
        deleteTagCalled = true
        deleteTagTagName = tagName
        deleteTagThrows?.let { throw it }
    }

    var createTagCalled = false
    var createTagThrows: Throwable? = null

    override suspend fun createTag(tagName: String, color: String) {
        createTagCalled = true
        createTagThrows?.let { throw it }
    }

    var editTagFromTagName: String? = null
    var editTagToTagName: String? = "sentinel"
    var editTagThrows: Throwable? = null

    override suspend fun editTag(fromTagName: String, toTagName: String?, color: String?) {
        editTagFromTagName = fromTagName
        editTagToTagName = toTagName
        editTagThrows?.let { throw it }
    }

    var mixTagsCalled = false
    var mixTagsFromTags: List<String>? = null
    var mixTagsToTag: String? = null
    var mixTagsThrows: Throwable? = null

    override suspend fun mixTags(fromTags: List<String>, toTag: String) {
        mixTagsCalled = true
        mixTagsFromTags = fromTags
        mixTagsToTag = toTag
        mixTagsThrows?.let { throw it }
    }
}