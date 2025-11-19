package com.grappim.taigamobile.feature.workitem.domain

import kotlinx.collections.immutable.ImmutableList

/**
 * Sometimes we don't need a specific work item, e.g. issue, user story, etc.
 * because we need only some general info from the item not tailored to a
 * specific work item.
 *
 * Ideally it shouldn't be big, it should only include fields that are really required
 *
 * Initially it was created to be used in the delegates since there we need only
 * specific info from the work item
 *
 * In all other cases please use the specific work item.
 */
data class WorkItem(
    val watcherUserIds: ImmutableList<Long>
)
