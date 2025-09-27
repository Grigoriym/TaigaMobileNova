package com.grappim.taigamobile.feature.workitem.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap

interface PatchDataGenerator {
    fun getTitle(title: String): ImmutableMap<String, Any?>
    fun getWatchersPatchPayload(watchers: List<Long>): ImmutableMap<String, Any?>
    fun getAssignedToPatchPayload(assignee: Long?): ImmutableMap<String, Any?>
    fun getAssignedUsersPatchPayload(assignees: ImmutableList<Long>): ImmutableMap<String, Any?>

    fun getBlockedPatchPayload(isBlocked: Boolean, blockNote: String?): ImmutableMap<String, Any?>

    fun getDueDatePatchPayload(dueDate: String?): ImmutableMap<String, Any?>

    fun getTagsPatchPayload(tags: List<List<String>>): ImmutableMap<String, Any?>

    fun getDescriptionPatchPayload(description: String): ImmutableMap<String, Any?>

    fun getAttributesPatchPayload(attributes: Map<String, Any?>): ImmutableMap<String, Any?>

    fun getStatus(id: Long): ImmutableMap<String, Any?>
    fun getType(id: Long): ImmutableMap<String, Any?>
    fun getSeverity(id: Long): ImmutableMap<String, Any?>
    fun getPriority(id: Long): ImmutableMap<String, Any?>
}
