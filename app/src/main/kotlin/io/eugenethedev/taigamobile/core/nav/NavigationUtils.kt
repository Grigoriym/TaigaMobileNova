package io.eugenethedev.taigamobile.core.nav

import io.eugenethedev.taigamobile.domain.entities.CommonTaskType

typealias NavigateToTask = (id: Long, type: CommonTaskType, ref: Int) -> Unit
