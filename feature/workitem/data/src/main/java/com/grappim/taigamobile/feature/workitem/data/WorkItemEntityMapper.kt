package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.storage.db.entities.WorkItemEntity
import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.feature.filters.domain.model.Tag
import com.grappim.taigamobile.feature.projects.domain.ProjectExtraInfo
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/**
 * Convert WorkItemEntity (cached) to WorkItem (domain).
 */
fun WorkItemEntity.toDomain(): WorkItem = WorkItem(
    id = id,
    taskType = taskType,
    createdDate = createdDate,
    status = Status(
        color = statusColor,
        id = statusId,
        name = statusName
    ),
    ref = ref,
    title = title,
    isBlocked = isBlocked,
    tags = parseTagsJson(tagsJson),
    isClosed = isClosed,
    colors = parseColorsJson(colorsJson),
    assignee = assigneeId?.let { id ->
        assigneeName?.let { name ->
            User(
                id = id,
                fullName = name,
                photo = assigneePhoto,
                bigPhoto = null,
                username = name // Use name as username fallback
            )
        }
    },
    blockedNote = blockedNote,
    project = ProjectExtraInfo(
        id = projectId,
        name = projectName,
        slug = "", // Not stored in entity
        logoSmallUrl = null // Not stored in entity
    )
)

/**
 * Convert WorkItem (domain) to WorkItemEntity (for caching).
 */
fun WorkItem.toEntity(sprintId: Long? = null): WorkItemEntity = WorkItemEntity(
    id = id,
    taskType = taskType,
    projectId = project.id,
    projectName = project.name,
    ref = ref,
    title = title,
    createdDate = createdDate,
    isClosed = isClosed,
    isBlocked = isBlocked,
    blockedNote = blockedNote,
    statusId = status.id,
    statusName = status.name,
    statusColor = status.color,
    assigneeId = assignee?.actualId,
    assigneeName = assignee?.displayName,
    assigneePhoto = assignee?.avatarUrl,
    tagsJson = encodeTagsJson(tags),
    colorsJson = encodeColorsJson(colors),
    sprintId = sprintId
)

fun List<WorkItemEntity>.toDomainList(): List<WorkItem> = map { it.toDomain() }

fun List<WorkItem>.toEntityList(sprintId: Long? = null): List<WorkItemEntity> = map { it.toEntity(sprintId) }

// Tag JSON format: ["name|color", "name2|color2"]
private fun parseTagsJson(tagsJson: String): kotlinx.collections.immutable.ImmutableList<Tag> {
    if (tagsJson.isBlank() || tagsJson == "[]") return persistentListOf()
    return try {
        json.decodeFromString<List<String>>(tagsJson)
            .mapNotNull { encoded ->
                val parts = encoded.split("|", limit = 2)
                if (parts.size == 2) Tag(name = parts[0], color = parts[1]) else null
            }
            .toImmutableList()
    } catch (e: Exception) {
        persistentListOf()
    }
}

private fun encodeTagsJson(tags: List<Tag>): String {
    if (tags.isEmpty()) return "[]"
    return json.encodeToString(tags.map { "${it.name}|${it.color}" })
}

private fun parseColorsJson(colorsJson: String): kotlinx.collections.immutable.ImmutableList<String> {
    if (colorsJson.isBlank() || colorsJson == "[]") return persistentListOf()
    return try {
        json.decodeFromString<List<String>>(colorsJson).toImmutableList()
    } catch (e: Exception) {
        persistentListOf()
    }
}

private fun encodeColorsJson(colors: List<String>): String {
    if (colors.isEmpty()) return "[]"
    return json.encodeToString(colors)
}
