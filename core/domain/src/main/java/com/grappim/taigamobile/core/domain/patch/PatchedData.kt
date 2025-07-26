package com.grappim.taigamobile.core.domain.patch

import com.grappim.taigamobile.core.domain.DueDateStatus

data class PatchedData(val version: Long, val dueDateStatus: DueDateStatus?)
