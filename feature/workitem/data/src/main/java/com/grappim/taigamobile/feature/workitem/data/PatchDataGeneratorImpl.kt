package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import javax.inject.Inject

class PatchDataGeneratorImpl @Inject constructor() : PatchDataGenerator {

    override fun getTitle(title: String): ImmutableMap<String, Any?> = persistentMapOf("subject" to title)

    override fun getWatchersPatchPayload(watchers: List<Long>): ImmutableMap<String, Any?> =
        mapOf("watchers" to watchers).toImmutableMap()

    override fun getAssignedToPatchPayload(assignee: Long?): ImmutableMap<String, Any?> =
        mapOf("assigned_to" to assignee).toImmutableMap()

    override fun getBlockedPatchPayload(isBlocked: Boolean, blockNote: String?): ImmutableMap<String, Any?> = mapOf(
        "is_blocked" to isBlocked,
        "blocked_note" to blockNote.orEmpty()
    ).toImmutableMap()

    override fun getAssignedUsersPatchPayload(assignees: ImmutableList<Long>): ImmutableMap<String, Any?> =
        mapOf("assigned_users" to assignees).toImmutableMap()

    override fun getDueDatePatchPayload(dueDate: String?): ImmutableMap<String, Any?> =
        mapOf("due_date" to dueDate).toImmutableMap()

    override fun getTagsPatchPayload(tags: List<List<String>>): ImmutableMap<String, Any?> =
        mapOf("tags" to tags).toImmutableMap()

    override fun getDescriptionPatchPayload(description: String): ImmutableMap<String, Any?> =
        mapOf("description" to description).toImmutableMap()

    override fun getAttributesPatchPayload(attributes: Map<String, Any?>): ImmutableMap<String, Any?> =
        mapOf("attributes_values" to attributes).toImmutableMap()

    override fun getStatus(id: Long): ImmutableMap<String, Any?> = persistentMapOf("status" to id)
    override fun getType(id: Long): ImmutableMap<String, Any?> = persistentMapOf("type" to id)
    override fun getSeverity(id: Long): ImmutableMap<String, Any?> = persistentMapOf("severity" to id)

    override fun getPriority(id: Long): ImmutableMap<String, Any?> = persistentMapOf("priority" to id)
}
