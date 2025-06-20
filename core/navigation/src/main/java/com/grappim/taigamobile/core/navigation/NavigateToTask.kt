package com.grappim.taigamobile.core.navigation

import com.grappim.taigamobile.core.domain.CommonTaskType

@Deprecated("remove it eventually")
typealias NavigateToTask = (id: Long, type: CommonTaskType, ref: Int) -> Unit
