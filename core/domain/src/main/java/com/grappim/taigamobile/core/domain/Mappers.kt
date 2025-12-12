package com.grappim.taigamobile.core.domain

val nullOwnerError = IllegalArgumentException(
    "CommonTaskResponse requires not null 'owner' field"
)

@Deprecated("yep")
fun CommonTaskResponse.toCommonTaskExtended(
    commonTaskType: CommonTaskType,
    filters: FiltersDataDTO,
    swimlaneDTOS: List<SwimlaneDTO>,
    sprint: Sprint? = null,
    tagOlds: List<TagOld>,
    url: String
): CommonTaskExtended = CommonTaskExtended(
    id = id,
    statusOld = StatusOld(
        id = status,
        name = statusExtraInfo.name,
        color = statusExtraInfo.color,
        type = StatusType.Status
    ),
    taskType = commonTaskType,
    createdDateTime = createdDate,
    sprint = sprint,
    assignedIds = assignedUsers ?: listOfNotNull(assignedTo),
    watcherIds = watchers.orEmpty(),
    creatorId = owner ?: throw nullOwnerError,
    ref = ref,
    title = subject,
    isClosed = isClosed,
    description = description ?: "",
    epicsShortInfo = epics.orEmpty(),
    projectSlug = projectDTOExtraInfo.slug,
    tagOlds = tagOlds,
    swimlaneDTO = swimlaneDTOS.find { it.id == swimlane },
    dueDate = dueDate,
    dueDateStatusDTO = dueDateStatusDTO,
    userStoryShortInfo = userStoryExtraInfo,
    version = version,
    color = color,
    type = type?.let { id -> filters.types.find { it.id == id } }
        ?.toStatus(StatusType.Type),
    severity = severity?.let { id -> filters.severities.find { it.id == id } }
        ?.toStatus(StatusType.Severity),
    priority = priority?.let { id -> filters.priorities.find { it.id == id } }
        ?.toStatus(StatusType.Priority),
    url = url,
    blockedNote = blockedNote.takeIf { isBlocked }
)
