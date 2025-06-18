package com.grappim.taigamobile.core.nav

import com.grappim.taigamobile.domain.entities.CommonTaskType

typealias NavigateToTask = (id: Long, type: CommonTaskType, ref: Int) -> Unit
