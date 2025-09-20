package com.grappim.taigamobile.feature.workitem.domain

import com.grappim.taigamobile.core.domain.Comment
import kotlinx.collections.immutable.ImmutableList

data class CreatedCommentData(val newVersion: Long, val comments: ImmutableList<Comment>)
