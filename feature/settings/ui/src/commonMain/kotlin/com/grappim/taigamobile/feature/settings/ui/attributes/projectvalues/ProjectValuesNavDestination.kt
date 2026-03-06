package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.navigation.NavController
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.project_values_epic_statuses
import com.grappim.taigamobile.strings.generated.resources.project_values_issue_due_dates
import com.grappim.taigamobile.strings.generated.resources.project_values_issue_statuses
import com.grappim.taigamobile.strings.generated.resources.project_values_issue_types
import com.grappim.taigamobile.strings.generated.resources.project_values_points
import com.grappim.taigamobile.strings.generated.resources.project_values_priorities
import com.grappim.taigamobile.strings.generated.resources.project_values_severities
import com.grappim.taigamobile.strings.generated.resources.project_values_task_due_dates
import com.grappim.taigamobile.strings.generated.resources.project_values_task_statuses
import com.grappim.taigamobile.strings.generated.resources.project_values_us_due_dates
import com.grappim.taigamobile.strings.generated.resources.project_values_us_statuses
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

@Serializable
data class ProjectValuesNavDestination(val typeName: String)

fun NavController.navigateToProjectValues(type: ProjectValueType) {
    navigate(route = ProjectValuesNavDestination(typeName = type.name))
}

fun ProjectValueType.toTitleRes(): StringResource = when (this) {
    ProjectValueType.EPIC_STATUSES -> RString.project_values_epic_statuses
    ProjectValueType.US_STATUSES -> RString.project_values_us_statuses
    ProjectValueType.TASK_STATUSES -> RString.project_values_task_statuses
    ProjectValueType.ISSUE_STATUSES -> RString.project_values_issue_statuses
    ProjectValueType.ISSUE_TYPES -> RString.project_values_issue_types
    ProjectValueType.PRIORITIES -> RString.project_values_priorities
    ProjectValueType.SEVERITIES -> RString.project_values_severities
    ProjectValueType.POINTS -> RString.project_values_points
    ProjectValueType.US_DUE_DATES -> RString.project_values_us_due_dates
    ProjectValueType.TASK_DUE_DATES -> RString.project_values_task_due_dates
    ProjectValueType.ISSUE_DUE_DATES -> RString.project_values_issue_due_dates
}
