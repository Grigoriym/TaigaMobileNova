package com.grappim.taigamobile.core.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.StatusType
import com.grappim.taigamobile.core.domain.Tag
import com.grappim.taigamobile.uikit.theme.taigaGray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

fun CommonTaskResponse.toCommonTask(commonTaskType: CommonTaskType) = CommonTask(
    id = id,
    createdDate = createdDate,
    title = subject,
    ref = ref,
    status = Status(
        id = status,
        name = statusExtraInfo.name,
        color = statusExtraInfo.color,
        type = StatusType.Status
    ),
    assignee = assignedToExtraInfo,
    projectInfo = projectExtraInfo,
    taskType = commonTaskType,
    colors = color?.let { listOf(it) } ?: epics.orEmpty().map { it.color },
    isClosed = isClosed,
    tags = tags.orEmpty().map { Tag(name = it[0]!!, color = it[1].fixNullColor()) },
    blockedNote = blockedNote.takeIf { isBlocked }
)

private val taigaGrayHex by lazy { taigaGray.toHex() }
fun String?.fixNullColor() =
    this ?: taigaGrayHex // gray, because api returns null instead of gray -_-

// TODO somehow remove it and use the general one
private fun Color.toHex() = "#%08X".format(toArgb()).replace("#FF", "#")

suspend fun <T> withIO(block: suspend CoroutineScope.() -> T): T =
    withContext(Dispatchers.IO, block)

inline fun <T> handle404(action: () -> List<T>): List<T> = try {
    action()
} catch (e: HttpException) {
    // suppress error if page not found (maximum page was reached)
    e.takeIf { it.code() == 404 }?.let { emptyList() } ?: throw e
}
