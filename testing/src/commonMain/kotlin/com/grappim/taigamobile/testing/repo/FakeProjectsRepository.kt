package com.grappim.taigamobile.testing.repo

import androidx.paging.PagingData
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.Project
import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProjectsRepository : ProjectsRepository {

    var permissions: ImmutableList<TaigaPermission> = persistentListOf(TaigaPermission.MODIFY_PROJECT)

    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> =
        flowOf(PagingData.empty())

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

    override suspend fun getCurrentProjectSimple(): ProjectSimple = error("not used in this test")

    var projectFlow: Flow<ProjectSimple> = flowOf()
    override fun getCurrentProjectFlow(): Flow<ProjectSimple> = projectFlow

    override suspend fun getPermissions(): ImmutableList<TaigaPermission> = permissions

    var fetchAndSaveProjectInfoCalled = false
    var fetchAndSaveProjectInfoThrows: Throwable? = null

    override suspend fun fetchAndSaveProjectInfo() {
        fetchAndSaveProjectInfoCalled = true
        fetchAndSaveProjectInfoThrows?.let { throw it }
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