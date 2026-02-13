package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

class WorkItemEntityMapper @Inject constructor(private val json: Json) {

    fun toDomain(entity: WorkItemEntity): WorkItem = WorkItem(
        id = entity.id,
        taskType = entity.taskType,
        createdDate = entity.createdDate,
        status = Status(
            color = entity.statusColor,
            id = entity.statusId,
            name = entity.statusName
        ),
        ref = entity.ref,
        title = entity.title,
        isBlocked = entity.isBlocked,
        tags = parseTagsJson(entity.tagsJson),
        isClosed = entity.isClosed,
        colors = parseColorsJson(entity.colorsJson),
        assignee = entity.assigneeId?.let { id ->
            entity.assigneeName?.let { name ->
                User(
                    id = id,
                    fullName = name,
                    photo = entity.assigneePhoto,
                    bigPhoto = null,
                    username = name // Use name as username fallback
                )
            }
        },
        blockedNote = entity.blockedNote,
        project = ProjectExtraInfo(
            id = entity.projectId,
            name = entity.projectName,
            slug = "", // Not stored in entity
            logoSmallUrl = null // Not stored in entity
        )
    )

    fun toEntity(workItem: WorkItem, sprintId: Long? = null): WorkItemEntity = WorkItemEntity(
        id = workItem.id,
        taskType = workItem.taskType,
        projectId = workItem.project.id,
        projectName = workItem.project.name,
        ref = workItem.ref,
        title = workItem.title,
        createdDate = workItem.createdDate,
        isClosed = workItem.isClosed,
        isBlocked = workItem.isBlocked,
        blockedNote = workItem.blockedNote,
        statusId = workItem.status.id,
        statusName = workItem.status.name,
        statusColor = workItem.status.color,
        assigneeId = workItem.assignee?.actualId,
        assigneeName = workItem.assignee?.displayName,
        assigneePhoto = workItem.assignee?.avatarUrl,
        tagsJson = encodeTagsJson(workItem.tags),
        colorsJson = encodeColorsJson(workItem.colors),
        sprintId = sprintId
    )

    fun toDomainList(entities: List<WorkItemEntity>): List<WorkItem> = entities.map { toDomain(it) }

    fun toEntityList(workItems: List<WorkItem>, sprintId: Long? = null): List<WorkItemEntity> =
        workItems.map { toEntity(it, sprintId) }

    // Tag JSON format: ["name|color", "name2|color2"]
    private fun parseTagsJson(tagsJson: String): ImmutableList<Tag> {
        if (tagsJson.isBlank() || tagsJson == "[]") return persistentListOf()
        return try {
            json.decodeFromString<List<String>>(tagsJson)
                .mapNotNull { encoded ->
                    val parts = encoded.split("|", limit = 2)
                    if (parts.size == 2) Tag(name = parts[0], color = parts[1]) else null
                }
                .toImmutableList()
        } catch (e: Exception) {
            Timber.e(e)
            persistentListOf()
        }
    }

    private fun encodeTagsJson(tags: List<Tag>): String {
        if (tags.isEmpty()) return "[]"
        return json.encodeToString(tags.map { "${it.name}|${it.color}" })
    }

    private fun parseColorsJson(colorsJson: String): ImmutableList<String> {
        if (colorsJson.isBlank() || colorsJson == "[]") return persistentListOf()
        return try {
            json.decodeFromString<List<String>>(colorsJson).toImmutableList()
        } catch (e: Exception) {
            Timber.e(e)
            persistentListOf()
        }
    }

    private fun encodeColorsJson(colors: List<String>): String {
        if (colors.isEmpty()) return "[]"
        return json.encodeToString(colors)
    }
}
