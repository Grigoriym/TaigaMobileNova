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

class FakeProjectsRepository : ProjectsRepository {

    var permissions: ImmutableList<TaigaPermission> = persistentListOf(TaigaPermission.MODIFY_PROJECT)

    override suspend fun fetchProjects(query: String): Flow<PagingData<Project>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMyProjects(): ImmutableList<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserProjects(userId: Long): ImmutableList<Project> {
        TODO("Not yet implemented")
    }

    override suspend fun saveProject(project: Project) {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentProjectSimple(): ProjectSimple {
        TODO("Not yet implemented")
    }

    override fun getCurrentProjectFlow(): Flow<ProjectSimple> {
        TODO("Not yet implemented")
    }

    override suspend fun getPermissions(): ImmutableList<TaigaPermission> = permissions

    var fetchAndSaveProjectInfoCalled = false
    var fetchAndSaveProjectInfoThrows: Throwable? = null

    override suspend fun fetchAndSaveProjectInfo() {
        fetchAndSaveProjectInfoCalled = true
        fetchAndSaveProjectInfoThrows?.let { throw it }
    }

    override suspend fun getTagsColors(): ImmutableList<Tag> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTag(tagName: String) {
        TODO("Not yet implemented")
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

    override suspend fun mixTags(fromTags: List<String>, toTag: String) {
        TODO("Not yet implemented")
    }
}