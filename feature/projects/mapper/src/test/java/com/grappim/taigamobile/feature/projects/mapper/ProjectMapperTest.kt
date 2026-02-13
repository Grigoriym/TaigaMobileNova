package com.grappim.taigamobile.feature.projects.mapper

import com.grappim.taigamobile.core.storage.db.entities.ProjectEntity
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.projects.dto.TaigaPermissionDTO
import com.grappim.taigamobile.testing.getProject
import com.grappim.taigamobile.testing.getProjectDTO
import com.grappim.taigamobile.testing.getProjectExtraInfoDTO
import com.grappim.taigamobile.testing.getRandomBoolean
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import kotlinx.collections.immutable.persistentListOf
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ProjectMapperTest {

    private lateinit var sut: ProjectMapper

    @Before
    fun setup() {
        sut = ProjectMapper()
    }

    @Test
    fun `toProject should map all fields correctly`() {
        val dto = getProjectDTO()

        val result = sut.toProject(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.slug, result.slug)
        assertEquals(dto.isMember, result.isMember)
        assertEquals(dto.isAdmin, result.isAdmin)
        assertEquals(dto.isOwner, result.isOwner)
        assertEquals(dto.description, result.description)
        assertEquals(dto.avatarUrl, result.avatarUrl)
        assertEquals(dto.members.size, result.members.size)
        assertEquals(dto.fansCount, result.fansCount)
        assertEquals(dto.watchersCount, result.watchersCount)
        assertEquals(dto.isPrivate, result.isPrivate)
        assertEquals(dto.isEpicsActivated, result.isEpicsActivated)
        assertEquals(dto.isBacklogActivated, result.isBacklogActivated)
        assertEquals(dto.isKanbanActivated, result.isKanbanActivated)
        assertEquals(dto.isIssuesActivated, result.isIssuesActivated)
        assertEquals(dto.isWikiActivated, result.isWikiActivated)
        assertEquals(dto.defaultSwimlane, result.defaultSwimlane)
    }

    @Test
    fun `toProject should map permissions correctly`() {
        val dto = getProjectDTO().copy(
            myPermissions = persistentListOf(
                TaigaPermissionDTO.VIEW_PROJECT,
                TaigaPermissionDTO.ADD_TASK,
                TaigaPermissionDTO.MODIFY_US
            )
        )

        val result = sut.toProject(dto)

        assertEquals(3, result.myPermissions.size)
        assertEquals(TaigaPermission.VIEW_PROJECT, result.myPermissions[0])
        assertEquals(TaigaPermission.ADD_TASK, result.myPermissions[1])
        assertEquals(TaigaPermission.MODIFY_US, result.myPermissions[2])
    }

    @Test
    fun `toProjectExtraInfo should map all fields correctly`() {
        val dto = getProjectExtraInfoDTO()

        val result = sut.toProjectExtraInfo(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.slug, result.slug)
        assertEquals(dto.logoSmallUrl, result.logoSmallUrl)
    }

    @Test
    fun `toProjectSimple should map entity to domain`() {
        val entity = getProjectEntity()

        val result = sut.toProjectSimple(entity)

        assertEquals(entity.id, result.id)
        assertEquals(entity.name, result.name)
        assertEquals(entity.slug, result.slug)
        assertEquals(entity.myPermissions.size, result.myPermissions.size)
        assertEquals(entity.isEpicsActivated, result.isEpicsActivated)
        assertEquals(entity.isBacklogActivated, result.isBacklogActivated)
        assertEquals(entity.isKanbanActivated, result.isKanbanActivated)
        assertEquals(entity.isIssuesActivated, result.isIssuesActivated)
        assertEquals(entity.isWikiActivated, result.isWikiActivated)
        assertEquals(entity.defaultSwimlane, result.defaultSwimlane)
    }

    @Test
    fun `toListDomain should map list of DTOs`() {
        val dto1 = getProjectDTO()
        val dto2 = getProjectDTO()

        val result = sut.toListDomain(listOf(dto1, dto2))

        assertEquals(2, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto2.id, result[1].id)
    }

    @Test
    fun `toListDomain should return empty list for empty input`() {
        val result = sut.toListDomain(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toEntity from Project should map all fields correctly`() {
        val project = getProject()

        val result = sut.toEntity(project)

        assertEquals(project.id, result.id)
        assertEquals(project.name, result.name)
        assertEquals(project.slug, result.slug)
        assertEquals(project.myPermissions.size, result.myPermissions.size)
        assertEquals(project.isEpicsActivated, result.isEpicsActivated)
        assertEquals(project.isBacklogActivated, result.isBacklogActivated)
        assertEquals(project.isKanbanActivated, result.isKanbanActivated)
        assertEquals(project.isIssuesActivated, result.isIssuesActivated)
        assertEquals(project.isWikiActivated, result.isWikiActivated)
        assertEquals(project.defaultSwimlane, result.defaultSwimlane)
    }

    @Test
    fun `toEntity from ProjectDTO should map all fields correctly`() {
        val dto = getProjectDTO()

        val result = sut.toEntity(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.slug, result.slug)
        assertEquals(dto.isEpicsActivated, result.isEpicsActivated)
        assertEquals(dto.isBacklogActivated, result.isBacklogActivated)
        assertEquals(dto.isKanbanActivated, result.isKanbanActivated)
        assertEquals(dto.isIssuesActivated, result.isIssuesActivated)
        assertEquals(dto.isWikiActivated, result.isWikiActivated)
        assertEquals(dto.defaultSwimlane, result.defaultSwimlane)
    }

    @Test
    fun `toEntity from ProjectDTO should map permissions correctly`() {
        val dto = getProjectDTO().copy(
            myPermissions = persistentListOf(
                TaigaPermissionDTO.DELETE_ISSUE,
                TaigaPermissionDTO.VIEW_WIKI_PAGES
            )
        )

        val result = sut.toEntity(dto)

        assertEquals(2, result.myPermissions.size)
        assertEquals(TaigaPermission.DELETE_ISSUE, result.myPermissions[0])
        assertEquals(TaigaPermission.VIEW_WIKI_PAGES, result.myPermissions[1])
    }

    private fun getProjectEntity(): ProjectEntity = ProjectEntity(
        id = getRandomLong(),
        name = getRandomString(),
        slug = getRandomString(),
        myPermissions = listOf(TaigaPermission.VIEW_PROJECT, TaigaPermission.ADD_US),
        isEpicsActivated = getRandomBoolean(),
        isBacklogActivated = getRandomBoolean(),
        isKanbanActivated = getRandomBoolean(),
        isIssuesActivated = getRandomBoolean(),
        isWikiActivated = getRandomBoolean(),
        defaultSwimlane = getRandomLong(),
        isMember = getRandomBoolean(),
        isAdmin = getRandomBoolean(),
        isOwner = getRandomBoolean(),
        description = getRandomString(),
        avatarUrl = getRandomString()
    )
}
