package com.grappim.taigamobile.createtask

import com.grappim.taigamobile.core.domain.CommonTaskType

data class CreateWorkItemData(val id: Long, val type: CommonTaskType, val ref: Int)
