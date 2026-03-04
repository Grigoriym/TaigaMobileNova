package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

class FakePatchDataGenerator : PatchDataGenerator {
    override fun getTitle(title: String): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getWatchersPatchPayload(watchers: List<Long>): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getAssignedToPatchPayload(assignee: Long?): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getAssignedUsersPatchPayload(assignees: ImmutableList<Long>): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getBlockedPatchPayload(isBlocked: Boolean, blockNote: String?): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getDueDatePatchPayload(dueDate: String?): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getTagsPatchPayload(tags: List<List<String>>): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getDescriptionPatchPayload(description: String): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getAttributesPatchPayload(attributes: Map<String, Any?>): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getStatus(id: Long): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getType(id: Long): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getSeverity(id: Long): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getPriority(id: Long): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getColor(color: String): ImmutableMap<String, Any?> = persistentMapOf("color" to color)
    override fun getSprint(sprintId: Long?): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getWikiContent(content: String): ImmutableMap<String, Any?> = persistentMapOf()
    override fun getComment(comment: String): ImmutableMap<String, Any?> = persistentMapOf()
}
