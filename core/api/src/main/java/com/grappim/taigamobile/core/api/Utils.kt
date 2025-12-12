package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.StatusOld
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.TagOld
import com.grappim.taigamobile.utils.ui.fixNullColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.time.LocalDate

@Deprecated("use mapper")
fun CommonTaskResponse.toCommonTask(commonTaskType: CommonTaskType) = CommonTask(
    id = id,
    createdDate = createdDate,
    title = subject,
    ref = ref,
    statusOld = StatusOld(
        id = status,
        name = statusExtraInfo.name,
        color = statusExtraInfo.color,
        type = StatusType.Status
    ),
    assignee = assignedToExtraInfo,
    projectDTOInfo = projectDTOExtraInfo,
    taskType = commonTaskType,
    colors = color?.let { listOf(it) } ?: epics.orEmpty().map { it.color },
    isClosed = isClosed,
    tagOlds = tags.orEmpty().map { TagOld(name = it[0]!!, color = it[1].fixNullColor()) },
    blockedNote = blockedNote.takeIf { isBlocked }
)

@Deprecated("for the most cases we don't need it")
suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T = withContext(Dispatchers.IO, block)

inline fun <T> handle404(action: () -> List<T>): List<T> = try {
    action()
} catch (e: HttpException) {
    // suppress error if page not found (maximum page was reached)
    e.takeIf { it.code() == 404 }?.let { emptyList() } ?: throw e
}

fun String.toLocalDate(): LocalDate = LocalDate.parse(this)
