package io.eugenethedev.taigamobile.scrum

import androidx.annotation.StringRes
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.ui.components.containers.Tab

enum class ScrumTabs(@StringRes override val titleId: Int) : Tab {
    Backlog(R.string.backlog),
    Sprints(R.string.sprints_title)
}
