package com.grappim.taigamobile.core.domain.patch

import com.grappim.taigamobile.core.domain.DueDateStatus

data class PatchedData(val newVersion: Long, val dueDateStatus: DueDateStatus?)
