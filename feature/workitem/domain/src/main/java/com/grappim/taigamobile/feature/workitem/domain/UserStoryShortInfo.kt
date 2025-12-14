package com.grappim.taigamobile.feature.workitem.domain

import kotlinx.collections.immutable.ImmutableList

data class UserStoryShortInfo(val id: Long, val ref: Int, val title: String, val epics: ImmutableList<EpicShortInfo>)
