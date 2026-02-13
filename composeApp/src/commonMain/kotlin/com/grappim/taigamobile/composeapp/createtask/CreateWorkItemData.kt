package com.grappim.taigamobile.composeapp.createtask

import com.grappim.taigamobile.core.domain.CommonTaskType

data class CreateWorkItemData(val id: Long, val type: CommonTaskType, val ref: Long)
