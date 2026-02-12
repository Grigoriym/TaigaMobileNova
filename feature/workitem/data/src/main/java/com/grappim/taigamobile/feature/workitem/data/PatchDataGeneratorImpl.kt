package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import javax.inject.Inject

class PatchDataGeneratorImpl @Inject constructor() : PatchDataGenerator {

    override fun getTitle(title: String): ImmutableMap<String, Any?> = persistentMapOf("subject" to title)

    override fun getWatchersPatchPayload(watchers: List<Long>): ImmutableMap<String, Any?> =
        persistentMapOf("watchers" to watchers)

    override fun getAssignedToPatchPayload(assignee: Long?): ImmutableMap<String, Any?> =
        persistentMapOf("assigned_to" to assignee)

    override fun getBlockedPatchPayload(isBlocked: Boolean, blockNote: String?): ImmutableMap<String, Any?> =
        persistentMapOf(
            "is_blocked" to isBlocked,
            "blocked_note" to blockNote.orEmpty()
        )

    override fun getAssignedUsersPatchPayload(assignees: ImmutableList<Long>): ImmutableMap<String, Any?> =
        persistentMapOf("assigned_users" to assignees)

    override fun getDueDatePatchPayload(dueDate: String?): ImmutableMap<String, Any?> =
        persistentMapOf("due_date" to dueDate)

    override fun getTagsPatchPayload(tags: List<List<String>>): ImmutableMap<String, Any?> =
        persistentMapOf("tags" to tags)

    override fun getDescriptionPatchPayload(description: String): ImmutableMap<String, Any?> =
        persistentMapOf("description" to description)

    override fun getAttributesPatchPayload(attributes: Map<String, Any?>): ImmutableMap<String, Any?> =
        persistentMapOf("attributes_values" to attributes)

    override fun getStatus(id: Long): ImmutableMap<String, Any?> = persistentMapOf("status" to id)
    override fun getType(id: Long): ImmutableMap<String, Any?> = persistentMapOf("type" to id)
    override fun getSeverity(id: Long): ImmutableMap<String, Any?> = persistentMapOf("severity" to id)

    override fun getPriority(id: Long): ImmutableMap<String, Any?> = persistentMapOf("priority" to id)

    override fun getColor(color: String): ImmutableMap<String, Any?> = persistentMapOf("color" to color)
    override fun getSprint(sprintId: Long?): ImmutableMap<String, Any?> = persistentMapOf("milestone" to sprintId)

    override fun getWikiContent(content: String): ImmutableMap<String, Any?> = persistentMapOf("content" to content)
    override fun getComment(comment: String): ImmutableMap<String, Any?> = persistentMapOf("comment" to comment)
}
