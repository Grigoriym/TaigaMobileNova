package com.grappim.taigamobile.core.nav

import com.grappim.taigamobile.core.domain.CommonTaskType

typealias NavigateToTask = (id: Long, type: CommonTaskType, ref: Int) -> Unit
